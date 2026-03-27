# Project Rules

## Architecture

1. `app.views` is presentation-only.
2. Views may only assemble hiccup from already prepared props. They must not contain business logic, filtering, searching, counting, selection, sorting, formatting, normalization, or other derived-state computation.
3. If the UI needs derived data, add a pure function in `app.selectors` and pass the resulting view-model into the view layer.
4. Treat `app.selectors` as the closest analogue to re-frame subscriptions in this codebase: pure reads over immutable state snapshots, no side effects.
5. Keep domain transitions in `app.actions` and side effects in `:nexus/effects`.
6. Predicates used by `if`, `when`, or `cond` in views must arrive as named props from selectors/view-models. Views may branch on booleans, but must not compute those booleans themselves.
7. Prefer hiccup class shorthand such as `:div.foo.bar` over `{:class "foo bar"}` whenever the class list is static.
8. For computed or conditional classes, use a collection in `:class` instead of building a space-separated string with `str`.
9. For finite UI states like statuses, prefer explicit `case`-based class mapping near the view instead of synthesizing class names from domain values.
10. After editing Clojure/ClojureScript files, run `bb format`.
11. Interfaces must feel stable under interaction. Avoid layout shift, jitter, and surprise motion when filters change, drawers open, loading states resolve, or scrollbars appear.
12. Prefer stable dimensions, fixed table geometry, reserved scrollbar gutter, and overlay patterns that do not push core content around.
13. Motion should clarify state, not create drama. Avoid decorative animation in data-dense screens and do not let transitions move primary content unexpectedly.

## View Props

1. Props passed into a view should already be presentation-ready.
2. If the UI shows formatted text, labels, empty-state copy, visibility flags, active flags, selected flags, or CSS-variant identifiers, prepare those values in selectors before they reach the view.
3. Child views should receive fully prepared child props, not raw parent state plus extra booleans computed inline.
4. Collections rendered by a view should already be filtered, sorted, annotated, and enriched with any UI-specific flags they need.
5. Views may choose between already prepared alternatives, but must not derive those alternatives from domain state themselves.

Examples of values that belong in props, not in view logic:

- `:show-error?`
- `:show-empty?`
- `:show-retry?`
- `:selected?`
- `:active?`
- `:started-at-label`
- `:finished-at-label`
- `:duration-label`
- `:empty-message`
- `:row-class` or a finite-state variant token when that is clearer than recomputing it in the view

## Nexus

1. `:nexus/actions` are pure functions from state snapshot plus args to new actions/effects.
2. `:nexus/effects` are the only place that may touch mutable systems, network calls, timers, DOM event methods, or other side effects.
3. Use `:nexus/placeholders` for dispatch-time values such as DOM input values.
4. Nested `dispatch` inside effects must be called with actions first, then optional dispatch-data.

Official docs:
- Nexus README: https://github.com/cjohansen/nexus

## Replicant

1. Replicant components are plain pure functions that return hiccup.
2. Event handlers in hiccup remain declarative data and should dispatch actions instead of embedding imperative logic.
3. Keep rendering and domain logic separate: Replicant renders prepared data, it should not become the place where application logic accumulates.
4. Use `:replicant/key` for stable keyed collections in rendered lists.

Official docs:
- Replicant homepage and guides: https://replicant.fun/
- Replicant hiccup reference: https://replicant.fun/hiccup/

## Practical rule of thumb

If a line in a view answers one of these questions, it probably belongs outside the view:

- "Which items should be visible?"
- "How many items have status X?"
- "Which entity is selected?"
- "What should this screen-level summary card show?"
- "Should this block render?"
- "Should this tab be active?"
- "How should this value be formatted for display?"
- "Which CSS variant should this state map to?"
- "Will this interaction cause the layout to jump or the user to lose their place?"

Put that logic in `app.selectors`, then render the resulting data in `app.views`.
