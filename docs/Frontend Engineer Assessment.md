**Frontend Engineer Assessment Pack**

| Role focus        | Frontend engineer with ClojureScript as a key development requirement                   |
|:------------------|:----------------------------------------------------------------------------------------|
| Assessment format | Candidate completes a practical task in 2–3 hours, then demos and explains the solution |

**Candidate Brief**

**Goal:** Assess product thinking, frontend implementation quality, and the ability to explain decisions clearly.

**•** The candidate should implement the solution in ClojureScript.

**•** The exercise should stay within a 2–3 hour timebox.

**•** The final session should include a demo and a walkthrough of design choices, trade-offs, and next steps.

**Scenario**

**Build a** Pipeline Runs Dashboard for an internal data platform team.

**Candidate task**

**•** Display a list of pipeline runs.

**•** Filter runs by status.

**•** Search by pipeline name.

**•** Open a detail view for a selected run.

**•** Allow a failed run to be retried.

**•** Handle loading, empty, and error states clearly.

**What we care about**

**•** Clear structure and readable code.

**•** Comfort working in ClojureScript.

**•** Sensible state management and async handling.

**•** Practical UX decisions within a short timebox.

**•** Ability to explain trade-offs and assumptions.

**2\. Functional Requirements**

| Area         | Minimum requirement                                              | What good looks like                                                  |
|:-------------|:-----------------------------------------------------------------|:----------------------------------------------------------------------|
| Runs list    | Show run ID, pipeline name, status, started at, duration, owner. | Readable layout, clear status presentation, sensible formatting.      |
| Filtering    | Filter by status: all, success, failed, running, queued.         | Filter state is obvious and easy to change.                           |
| Search       | Search by pipeline name.                                         | Works cleanly with filters and responds quickly.                      |
| Run detail   | Open a selected run and show richer information.                 | Simple detail pattern such as panel, drawer, or route.                |
| Retry action | Retry a failed run using a mocked POST action.                   | Shows in-progress state, success path, and graceful failure handling. |
| App states   | Show loading, empty, and error states.                           | States are clear and not an afterthought.                             |

**3\. Technical Expectations**

| Required                                      | Nice to have                        |
|:----------------------------------------------|:------------------------------------|
| Core implementation in ClojureScript          | Lightweight tests                   |
| README with setup and run steps               | Basic routing                       |
| Project structure that is easy to follow      | Reusable components                 |
| A solution the candidate can demo and explain | Accessibility considerations        |
|                                               | Responsive layout                   |
|                                               | Well-separated UI and data concerns |

**4\. Instructions**

| Timebox: Spend no more than 2–3 hours on this exercise. Build: A dashboard that lets a user view runs, filter/search, open run details, and retry a failed run. Technology: Use ClojureScript for the main implementation. Supporting libraries are allowed. Submission: Share source code, a short README, and any assumptions you made. Interview: Be ready to run the app, explain your structure and state choices, discuss trade-offs, and describe what you would improve next. |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Hints you may optionally include**

**•** Mocking the API layer is acceptable.

**•** A simple, well-structured solution is better than an overbuilt one.

**•** Think about loading, empty, error, and action-in-progress states early.

**•** If time runs short, prioritise list view, filter/search, details, then retry flow.

**•** Explain the choices made.
