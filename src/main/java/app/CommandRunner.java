package app;

import exception.JobPilotException;
import parser.ParsedCommand;
import parser.CommandType;
import task.Application;
import task.Editor;
import task.Filterer;
import task.IndustryTag;
import ui.Ui;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Executes parsed commands and manages application flow.
 */
public class CommandRunner {

    private final ArrayList<Application> applications;

    public CommandRunner(ArrayList<Application> applications) {
        this.applications = applications;
    }

    public boolean run(ParsedCommand cmd) {
        if (cmd == null) {
            Ui.showError("Invalid command!");
            return true;
        }

        switch (cmd.getType()) {

            case BYE:
                Ui.showGoodbye(applications.size());
                Ui.close();
                return false;

            case HELP:
                Ui.showHelp();
                break;

            case ADD:
                try {
                    Application newApp = new Application(cmd.getCompany(), cmd.getPosition(), cmd.getDate());
                    applications.add(newApp);
                    Ui.showApplicationAdded(newApp);
                } catch (DateTimeParseException e) {
                    Ui.showError("Invalid date format! Use: yyyy-MM-dd");
                }
                break;

            case LIST:
                Ui.showApplicationList(applications);
                break;

            case DELETE:
                try {
                    Application removed = Editor.deleteApplication(applications, cmd.getIndex());
                    Ui.showApplicationDeleted(removed, applications.size());
                } catch (JobPilotException e) {
                    Ui.showError(e.getMessage());
                }
                break;

            case EDIT:
                try {
                    Editor.editApplication(applications, cmd.getIndex(),
                            cmd.getNewCompany(), cmd.getNewPosition(),
                            cmd.getNewDate(), cmd.getNewStatus());
                    Ui.showApplicationEdited(applications.get(cmd.getIndex() - 1));
                } catch (JobPilotException e) {
                    Ui.showError(e.getMessage());
                }
                break;

            case FILTER:
                try {
                    Filterer.filterByStatus(applications, cmd.getSearchTerm(), Ui.getInstance());
                } catch (JobPilotException e) {
                    Ui.showError(e.getMessage());
                }
                break;

            case SORT:
                if (applications.isEmpty()) {
                    Ui.showError("No applications to sort!");
                    break;
                }

                String sortType = cmd.getSearchTerm() != null ? cmd.getSearchTerm().trim().toLowerCase() : "";
                boolean reverse = sortType.contains("reverse");

                if (sortType.isEmpty() || sortType.startsWith("date")) {
                    if (reverse) {
                        applications.sort(Collections.reverseOrder());
                    } else {
                        Collections.sort(applications);
                    }
                } else if (sortType.startsWith("company")) {
                    if (reverse) {
                        applications.sort((a, b) -> b.getCompany().compareTo(a.getCompany()));
                    } else {
                        applications.sort((a, b) -> a.getCompany().compareTo(b.getCompany()));
                    }
                } else if (sortType.startsWith("status")) {
                    if (reverse) {
                        applications.sort((a, b) -> b.getStatus().compareTo(a.getStatus()));
                    } else {
                        applications.sort((a, b) -> a.getStatus().compareTo(b.getStatus()));
                    }
                } else {
                    Ui.showError("Invalid sort type! Use: sort date/company/status [reverse]");
                    break;
                }

                Ui.showSortedMessage();
                Ui.showApplicationList(applications);
                break;

            case SEARCH:
                if (applications.isEmpty()) {
                    Ui.showError("No applications to search!");
                    break;
                }

                String rawSearchTerm = cmd.getSearchTerm() != null ? cmd.getSearchTerm().trim() : "";
                if (rawSearchTerm.isEmpty()) {
                    Ui.showError("Please enter a valid search term.");
                    break;
                }

                boolean isExactMatch = rawSearchTerm.startsWith("exact:");
                boolean isNegativeSearch = rawSearchTerm.startsWith("!");
                String[] searchKeywords;

                if (isExactMatch) {
                    searchKeywords = new String[]{rawSearchTerm.substring("exact:".length()).trim().toLowerCase()};
                } else if (isNegativeSearch) {
                    searchKeywords = new String[]{rawSearchTerm.substring(1).trim().toLowerCase()};
                } else {
                    searchKeywords = rawSearchTerm.split("\\s+");
                    for (int i = 0; i < searchKeywords.length; i++) {
                        searchKeywords[i] = searchKeywords[i].trim().toLowerCase();
                    }
                }

                ArrayList<Application> results = new ArrayList<>();
                for (Application app : applications) {
                    String companyLower = app.getCompany().toLowerCase();
                    boolean matches = true;

                    for (String keyword : searchKeywords) {
                        if (keyword.isEmpty()) continue;

                        if (isExactMatch) {
                            if (!companyLower.equals(keyword)) {
                                matches = false;
                                break;
                            }
                        } else if (isNegativeSearch) {
                            if (companyLower.contains(keyword)) {
                                matches = false;
                                break;
                            }
                        } else {
                            if (!companyLower.contains(keyword)) {
                                matches = false;
                                break;
                            }
                        }
                    }

                    if (matches) {
                        results.add(app);
                    }
                }

                Collections.sort(results);
                Ui.showSearchResults(results, rawSearchTerm);
                break;

            case STATUS:
                try {
                    Editor.updateStatus(applications, cmd.getIndex(), cmd.getStatus(), cmd.getNote());
                    Ui.showStatusUpdated(applications.get(cmd.getIndex() - 1));
                } catch (JobPilotException e) {
                    Ui.showError(e.getMessage());
                }
                break;

            case TAG:
                try {
                    String action = cmd.getAction();
                    String tagName = cmd.getTag();
                    int index = cmd.getIndex();

                    if (index < 1 || index > applications.size()) {
                        Ui.showError("Invalid application number! You have " + applications.size() + " application(s).");
                        break;
                    }

                    Application app = applications.get(index - 1);
                    IndustryTag industryTag = new IndustryTag(tagName);

                    if ("add".equals(action)) {
                        app.addIndustryTag(industryTag);
                        Ui.showTagAdded(industryTag, app);  // ← 参数顺序正确
                    } else if ("remove".equals(action)) {
                        app.removeIndustryTag(industryTag);
                        Ui.showTagRemoved(industryTag, app);  // ← 参数顺序正确
                    } else {
                        Ui.showError("Invalid tag action! Use: add/TAG or remove/TAG");
                    }
                } catch (AssertionError e) {
                    Ui.showError(e.getMessage());
                } catch (Exception e) {
                    Ui.showError("Invalid tag command! Use: tag INDEX add/TAG or tag INDEX remove/TAG");
                }
                break;

            case UNKNOWN:
                Ui.showError("Unknown command. Type 'help' to see available commands.");
                break;

            case ERROR:
                Ui.showError(cmd.getErrorMessage());
                break;

            default:
                Ui.showError("Unknown command. Type 'help' to see available commands.");
        }

        return true;
    }
}