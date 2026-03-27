# Tests

Create test scenarios based on description in docs/Frontend\ Engineer\ Assessment.md

Create additional test scenarios for routes.

---

## Runs list

- Fetching runs shows loading state; on success, table renders with all runs
- Each row shows: run ID, pipeline name, status badge, started-at, duration, owner
- On fetch error, error state is shown instead of the table

## App states

- Initial load → loading state visible, no rows, no drawer
- Successful fetch with data → table shown, loading gone
- Successful fetch with no data → empty state shown ("No pipeline runs found.")
- Fetch error → error state shown with message

## Filter

- No filter active → all runs shown
- Selecting "success" → only successful runs shown
- Selecting "failed" → only failed runs shown
- Selecting "running" → only running runs shown
- Selecting "queued" → only queued runs shown
- Selecting a filter then selecting "All" → all runs shown again
- Filter buttons reflect active state visually (active? flag)

## Search

- Empty search → all runs shown (subject to active filter)
- Typing a name → only matching runs shown (case-insensitive, substring)
- Clearing search → returns to unfiltered result
- Search works in combination with active status filter
- Changing filter does not reset search input

## Filter + search combination

- Filter "failed" + search "partner" → only failed runs matching "partner"
- Filter "all" + search "billing" → all runs matching "billing" regardless of status
- Filter + search with no matches → empty state shown ("No runs match the current filter.")

## Run selection and drawer

- Clicking a run row → drawer opens showing run detail
- Drawer shows: run ID, pipeline name, status, started-at, finished-at, duration, owner, trigger type
- Drawer shows execution steps list with per-step status
- Failed run drawer shows error message
- Non-failed run drawer does not show error message
- Clicking close (×) → drawer closes, selected-id cleared
- Clicking a different row → drawer updates to new run
- Selected row is visually highlighted

## Filter interaction with selected run

- Selected run visible under new filter → drawer stays open, selection preserved
- Selected run hidden by new filter → drawer closes, selected-id cleared
- Switching from narrowing filter to "All" with a run selected → selection preserved if run is still visible

## Retry flow

- Drawer for a failed run shows Retry button
- Drawer for a non-failed run does not show Retry button
- Clicking Retry → button changes to in-progress spinner, disabled
- Retry success → success message shown in place of button
- Retry error → error message shown in place of button
- Clicking Retry while already in-progress → no-op (idempotent)

## URL → view (on load / direct navigation)

- `/?status=failed` → failed filter active
- `/?status=running` → running filter active
- `/?status=bogus` → no filter active (treated as nil)
- `/?q=billing` → search input pre-filled with "billing"
- `/?run=run_1001` → drawer opens for run_1001
- `/?run=run_1001&status=failed` → failed filter active + drawer open (if run_1001 is failed)
- `/?run=run_1001&status=success` → success filter active, run_1001 not visible → drawer closed, run cleared from URL
- `/?run=nonexistent` → no drawer, run param cleared from URL

## View → URL (bidirectional sync)

- Selecting a filter updates URL with `?status=<value>` (replace, no new history entry)
- Clearing filter to "All" removes `status` from URL
- Typing in search updates URL with `?q=<value>` (replace)
- Clearing search removes `q` from URL
- Clicking a run adds `?run=<id>` to URL (push, new history entry)
- Closing drawer removes `run` from URL (push)
- Navigating back (browser back) restores previous run selection

## Unrepresentable states

- URL contains `run` id that is not in the current run list → no drawer, id dropped
- URL contains `run` id visible only under a different filter than the one in URL → id dropped, URL corrected
- Navigating to a URL that requires correction → URL silently replaced (no extra history entry)
