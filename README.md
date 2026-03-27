# Pipeline Runs Dashboard

A Pipeline Runs Dashboard built in ClojureScript as part of a frontend engineering assessment.

## Stack

| Tool | Purpose |
|------|---------|
| [shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html) | ClojureScript build tooling |
| [replicant](https://github.com/cjohansen/replicant) | Data-driven DOM rendering (hiccup) |
| [nexus](https://github.com/cjohansen/nexus) | State management (pure actions + effects) |
| [portfolio](https://github.com/cjohansen/portfolio) | Component workbench |
| [babashka](https://babashka.org/) | Task runner |
| BEM CSS | Styling (no utility frameworks) |

## Prerequisites

- [Node.js](https://nodejs.org/) ≥ 18
- [Babashka](https://babashka.org/) ≥ 0.9.161
- Java ≥ 11 (for ClojureScript compilation)

## Setup

```bash
bb install
```

## Running

```bash
# Start the app (hot reload on http://localhost:3000)
bb dev

# Start the Portfolio component workbench (http://localhost:3001)
bb portfolio
```

## Other tasks

```bash
bb release   # production build
bb clean     # remove compiled output
```

## Features

- **Runs list** — table of all pipeline runs with ID, pipeline name, status, started time, duration, owner
- **Status filter** — filter runs by: All / Success / Failed / Running / Queued
- **Search** — live search by pipeline name, works in combination with status filter
- **Detail drawer** — click any row to open a slide-in panel with full run details and step breakdown
- **Shareable URL state** — current filter, search query, and open drawer are reflected in the URL and restored on load/back/forward navigation
- **Retry** — retry a failed run with visible in-progress state, success, and failure handling
- **App states** — loading skeleton, empty state (all runs / filtered), error banner

## Architecture

### State

A single `defonce` atom in `app.state` holds all app state. Nexus pure actions receive an immutable state snapshot and return a list of effects; effects perform side effects (API calls, state mutations). State survives hot reloads.

```
{:runs/all           []        ; all loaded runs
 :runs/loading?      false
 :runs/error         nil
 :runs/status-filter :all
 :runs/search-query  ""
 :runs/selected-id   nil       ; open drawer
 :runs/retry         {}}       ; per-run retry state
```

### Render loop

Replicant renders the full hiccup tree on every state change via `add-watch`. Components are pure functions — no local state, no lifecycle hooks. Screen-level derived data is prepared in `app.selectors` before being passed into `app.views`.

### Routing

The app uses `reitit-frontend` for a single dashboard route. Shareable UI state lives in query params:

```text
/?status=failed&q=billing&run=run_1002
```

The URL is a transport layer for three existing UI state values:

- `status` -> `:runs/status-filter`
- `q` -> `:runs/search-query`
- `run` -> `:runs/selected-id`

Filter and search changes replace the current history entry to avoid noisy Back-button behavior. Opening or closing the drawer pushes a new history entry so selected-run links are shareable and browser navigation feels natural.

### Project rules

- `app.views` is presentation-only.
- Derived UI data belongs in `app.selectors`, which plays the role closest to re-frame subscriptions in this project.
- Props passed into views should already be presentation-ready.
- Booleans used by `if`/`when`/`cond` in views are prepared in `app.selectors` and passed down as props.
- Formatted display values like time labels also belong in `app.selectors`, not in view functions.
- Child views should receive fully prepared child props, not raw state plus inline derived flags.
- Nexus actions stay pure; Nexus effects handle side effects.
- Prefer hiccup class shorthand for static classes.
- Use collections in `:class` for computed or conditional classes instead of space-joined strings.
- For finite UI states like statuses, prefer explicit `case`-based class mapping over synthesizing class names from domain values.
- Keep the interface visually stable: avoid layout shift, jitter, scrollbar pop-in, column width changes, and transitions that move primary content unexpectedly.
- Prefer stable geometry for dense UI like tables: fixed column widths, reserved scrollbar gutter, and overlays that do not push surrounding content around.
- Motion should explain state changes, not decorate them. Favor restrained transitions and avoid noisy animation on data-heavy screens.
- Run `bb format` after editing Clojure/ClojureScript files.

References:
- Nexus docs: https://github.com/cjohansen/nexus
- Replicant docs: https://replicant.fun/
- Replicant hiccup reference: https://replicant.fun/hiccup/

### Mock API

`app.api` inlines all 16 mock runs. `fetch-runs!` resolves after 600ms; `retry-run!` resolves after 1200ms (fails if any run is currently `running`, matching the spec).

## Assumptions & trade-offs

- **Retry always fails in this dataset** — since two `running` runs exist in the mock data, `retry-run!` will always return an error. This correctly matches the API spec ("Retry not allowed while another run is active"). To test the success path, temporarily comment out runs with `"running"` status in `api.cljs`.
- **No routing** — the detail panel is a slide-in drawer toggled by state. A real app would benefit from URL-addressable detail views.
- **Derived UI data lives in selectors** — screen-specific shaping, filtering, counting, and selection are computed in `app.selectors`, keeping `app.views` presentational.
- **BEM CSS, no framework** — keeps the bundle minimal and demonstrates structural CSS thinking.

## What I'd do next

- Add keyboard navigation (arrow keys through rows, Escape to close drawer)
- URL-based routing for deep-linkable run detail views
- Optimistic UI for retry (immediately update the run status in the list)
- Pagination or virtual scrolling for large run lists
- Lightweight tests for pure actions in `app.actions`
