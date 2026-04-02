# Guo Zicheng's Project Portfolio Page

## Overview
**JobPilot** is a command-line application designed to help
computing students efficiently manage their job applications.

Users can add, edit, delete, tag, and search applications,
while ensuring persistent storage and a clean CLI interface.

## Summary of Contributions

### Code Contributed
*[Link to code on tP Code Dashboard.](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=gzc-ceg&breakdown=true&sort=groupTitle%20dsc&sortWithin=title&since=2026-02-20T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=gzc-ceg&tabRepo=AY2526S2-CS2113-W13-3%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=docs~functional-code~test-code~other&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false)

### Enhancements Implemented
#### List All Applications Feature
- Implemented the functionality to list all stored job applications in a readable and organized format.
- Added handling for empty application lists to avoid crashes and provide informative messages to the user.
- Implementation was moderately challenging because it required integrating with persistent storage and ensuring formatting consistency.
#### Search Feature
- Implemented search functionality that supports queries by company name, position, and optional tag filtering.
- Added partial match support and case-insensitive search for better user experience.
- Ensured proper handling of edge cases, such as empty lists and queries with no matches.
- This feature is complete as it allows users to efficiently locate applications and supports all valid search scenarios.
#### CommandRunner
- Designed and implemented the main command execution engine that delegates commands to corresponding feature classes.
- Handles parsing validation, coordination between backend components, and sending results to `UI`.
- Implementation ensures separation of concerns: CommandRunner does not directly handle UI formatting or storage logic.
- This component is critical for modularity and scalability, as all new commands interact through this centralized handler.
#### List Tests
- Created unit tests for the list feature, covering both normal and edge cases (e.g., empty lists, multiple applications).
- Verified that the output matches expected formatting and includes all application data.
#### Search Tests
- Designed comprehensive tests for the search functionality to cover partial matches, case insensitivity, and tag filters.
- Included scenarios with empty application lists and queries with no matching results.
#### Refactoring of ParsedCommand
- Refactored the `ParsedCommand` class to better align with Java coding conventions and improve maintainability.
- Simplified field access, improved encapsulation, and added clear documentation for constructors and methods.
- Refactoring made it easier to integrate with CommandRunner and reduced potential bugs in command delegation.

### User Guide Contributions
- Add documentation, format and examples for `list` and `search`.

### Developer Guide Contributions
#### Multi-Type Search Feature Documentation
- Documented architecture, methods, and responsibilities for searching applications by company, position, or status.
- Illustrated step-by-step usage scenario of `JobPilot#search()` method, including parsing input, performing the search, and displaying results.
- Included sequence diagrams for normal flow, empty search, and no matches.
- Covered error handling: empty input, no applications, invalid format, no matches.
- Explained rationale: multiple search types, partial matching, direct result printing, linear search on `ArrayList`.
- Mentioned future improvements: multi-field search, fuzzy search, indexing, modularizing search logic.

### Contributions to team-based tasks
- New issues for v1.0, v2.0 and make sure issues are completed by deadline
- Help add `sort` feature to UG and DG
- Revise the input.txt and EXPECTED.txt to make sure the snapshot test pass
- Help reconstruct the overall code structure by splitting the code into Ui, CommandRunner, and Parser classes
- Extract `CommandRunner` from the previous version of our code

### Review/Mentoring contributions:
- Help teammates merge pull requests
- Help teammates deal with CI failure during pull requests

## Contributions to the Developer Guide (Extracts)
### Multi-Type Search Feature

#### Implementation Details

The **Multi-Type Search** feature allows users to retrieve job applications by matching **company name, position, or status** using a **case-insensitive partial search**. This feature is implemented directly within the `JobPilot` class through the method:

* `JobPilot#search(ArrayList<Application>, String type, String query)`

The application's data is stored in a central `ArrayList<Application>` named `applications`, where each `Application` object represents a job application.

The search operation works by iterating through the list and checking whether each application's relevant field contains the user-provided search keyword:

* Type `"c"` → company name
* Type `"p"` → position
* Type `"s"` → status

---

Given below is an example usage scenario demonstrating how the search mechanism behaves at each step.

**Step 1.** The user executes one of the following commands:

```text
search c/google
search p/software engineer
search s/interviewed
```

The Scanner inside the JobPilot.main() loop reads the raw input string.

**Step 2.** The if-else execution block in JobPilot.main() recognizes the search command and routes execution to the JobPilot#search() method.

**Step 3.** Inside search, the system extracts the search type and keyword:
```text
String type = input.substring("search ".length(), input.indexOf("/")).trim();
String query = input.substring(input.indexOf("/") + 1).trim();
```
If the search type or query is empty, an error message is shown. If the application list is empty, the system informs the user that there are no applications to search.

**Step 4.** The method iterates through all applications and performs a case-insensitive partial match:
```text
for (Application application : applications) {
    if (matches(application, type, query.toLowerCase())) {
        results.add(application);
    }
}
```
`matches()` checks the corresponding field:
- `"c"` → `application.getCompany().toLowerCase().contains(query)`
- `"p"`→ `application.getPosition().toLowerCase().contains(query)`
- `s` → `application.getStatus().toLowerCase().contains(query)`

**Step 5.** The results are displayed to the user. If no matches are found, the system prints a corresponding message. Otherwise, all matching applications are listed.

---

#### Sequence Diagrams
##### Main Success Flow
The following diagram illustrates the normal execution flow when a user performs a valid search:
![search_main.png](../diagrams/search/search_main.png)
##### Empty Search Term
The following diagram shows the system behavior when the user provides an empty search keyword:
![search_empty.png](../diagrams/search/search_empty.png)
##### No Match Found
The following diagram illustrates the case where no applications match the search keyword:
![search_nomatch.png](../diagrams/search/search_nomatch.png)

---
**Error Handling**

| Error Scenario       | Condition                                      | User Response                                                |
|---------------------|-----------------------------------------------|-------------------------------------------------------------|
| Empty Search Term     | User enters `search c/`, `p/`, or `s/` without keyword | "Search value cannot be empty!"                              |
| No Applications       | Application list is empty                     | "No applications to search!"                                 |
| No Match Found        | No application matches the keyword           | "No applications found for [type]: [keyword]"               |
| Invalid Format        | Input does not follow `search c/xxx`, `p/xxx`, or `s/xxx` | "Invalid search format! Use: search c/xxx or p/xxx or s/xxx"|
| Invalid Search Type   | Type is not `c`, `p`, or `s`                 | "Invalid search type! Use c/, p/, or s/"                     |

---

**Design Rationale**

| Decision                            | Rationale                                                                 |
|------------------------------------|---------------------------------------------------------------------------|
| Implement search in `JobPilot`      | Keeps implementation simple and avoids unnecessary abstraction           |
| Support multiple search types       | Improves usability by allowing field-specific searches                  |
| Case-insensitive matching           | Flexible input, user-friendly                                           |
| Partial matching using `contains()` | Allows users to search with incomplete input                             |
| Linear search on `ArrayList`        | Adequate for small datasets, simple to implement                         |
| Direct result printing               | Simplifies control flow without extra layers                             |

---

#### Design Considerations

**Aspect: Search logic placement**

* **Current Implementation:** The search logic is implemented directly inside the `JobPilot` class.
    * *Pros:* Simple and straightforward, easy to integrate with the main command loop.
    * *Cons:* Mixes UI and business logic, harder to test in isolation.

---

**Aspect: Matching strategy**

* **Current Implementation:** Case-insensitive partial matching via `toLowerCase().contains()`.
    * *Pros:* Flexible, supports partial input (e.g., "goo" matches "Google").
    * *Cons:* Less efficient for large datasets, limited to substring matching.

* **Alternative:** Exact match using `equalsIgnoreCase()`.
    * *Pros:* More precise, slightly more efficient.
    * *Cons:* Too strict, reduces usability.

---

#### Future Improvements

- Support multi-field search (e.g., `search c/google p/developer`)
- Implement fuzzy search for typo tolerance
- Introduce indexing for faster lookups in large datasets
- Extract search logic into a separate class for better modularity

## Contributions to the User Guide (Extracts)
### Listing
Lists all the applications

Format: `list`

Example: `list`

Example Output:
```text
Here are your applications:
1. Google | SE manager | 2025-03-10 | Pending
2. Amazon | Data Analyst | 2025-03-08 | Pending
3. Microsoft | SDE Intern | 2025-03-12 | Pending
___________________________________________________________________
```

### Searching Applications
- Searches job applications by matching the company name
  or the position or the current status
- Uses a **case-insensitive partial search**.

Format:
- `search c/COMPANY_NAME`
- `search p/POSITION`
- `search s/STATUS`

Examples:
- `search c/Google`
```text
Found 1 application(s) matching 'c/google':
1. Google | SE manager | 2025-03-10 | OFFER (Note: Negotiate salary) | Tags: [TECH]
___________________________________________________________________

```

- `search c/micro`
```text
Found 1 application(s) matching 'c/micro':
1. Microsoft | SDE Intern | 2025-03-12 | PENDING (Note: Waiting for reply) | Tags: [TECH, INTERN]
___________________________________________________________________
```

- `search p/intern`
```text
Found 1 application(s) matching 'p/intern':
1. Microsoft | SDE Intern | 2025-03-12 | PENDING (Note: Waiting for reply) | Tags: [TECH, INTERN]
___________________________________________________________________
```

- `search s/offer`
```text
Found 1 application(s) matching 's/offer':
1. Google | SE manager | 2025-03-10 | OFFER (Note: Negotiate salary) | Tags: [TECH]
___________________________________________________________________
```
