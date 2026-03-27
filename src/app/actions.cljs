(ns app.actions
  (:require
   [app.api :as api]
   [app.routes :as routes]
   [app.util :as util]))


(defn- current-ui-state
  [state]
  {:status-filter (:runs/status-filter state)
   :search-query  (:runs/search-query state)
   :selected-id   (:runs/selected-id state)})


(defn- ui-state->patch
  [{:keys [status-filter search-query selected-id]}]
  {:runs/status-filter status-filter
   :runs/search-query  search-query
   :runs/selected-id   selected-id})


(defn- selected-run-visible?
  [state {:keys [selected-id status-filter search-query]}]
  (let [query (util/normalize-query search-query)]
    (some
     (fn [run]
       (and (= selected-id (:id run))
            (util/run-matches? status-filter query run)))
     (:runs/all state))))


(defn- canonical-ui-state
  "Enforces the invariant that a selected run must be visible. Clears `:selected-id` if the run
  is hidden by the current filter or search. Only applied after runs have loaded."
  [state ui-state]
  (cond-> ui-state
    (and (:runs/loaded? state)
         (:selected-id ui-state)
         (not (selected-run-visible? state ui-state)))
    (assoc :selected-id nil)))


(defn- sync-query-effect
  [effect ui-state]
  [effect (routes/ui-state->query ui-state)])


(defn- next-ui-state-update
  [state overrides]
  (let [ui-state (canonical-ui-state state
                                     (merge (current-ui-state state)
                                            overrides))]
    {:patch (ui-state->patch ui-state)
     :ui-state ui-state}))


(defn- fetched-state-update
  [state runs]
  (let [run-patch {:runs/all runs
                   :runs/loaded? true
                   :runs/loading? false
                   :runs/error nil}
        base-state (merge state run-patch)
        ui-state (canonical-ui-state base-state (current-ui-state base-state))]
    {:patch (merge run-patch (ui-state->patch ui-state))
     :ui-state ui-state}))


;; Action handlers: pure fns of [state & args] → vector of effect tuples.
(def all-actions
  {:app/init
   (fn [_state]
     [[:runs/fetch]])

   :runs/fetch
   (fn [_state]
     [[:state/merge
       {:runs/loading? true
        :runs/error nil
        :runs/loaded? false}]
      [:api/fetch-runs]])

   :runs/fetch-success
   (fn [state runs]
     (let [{:keys [patch ui-state]} (fetched-state-update state runs)]
       [[:state/merge patch]
        (sync-query-effect :route/replace-ui-state ui-state)]))

   :runs/fetch-error
   (fn [_state message]
     [[:state/merge {:runs/error message :runs/loading? false}]])

   :runs/set-status-filter
   (fn [state status]
     (let [{:keys [patch ui-state]} (next-ui-state-update state {:status-filter status})]
       [[:state/merge patch]
        (sync-query-effect :route/replace-ui-state ui-state)]))

   :runs/set-search
   (fn [state query]
     (let [{:keys [patch ui-state]} (next-ui-state-update state {:search-query query})]
       [[:state/merge patch]
        (sync-query-effect :route/replace-ui-state ui-state)]))

   :runs/select-run
   (fn [state run-id]
     (let [{:keys [patch ui-state]} (next-ui-state-update state {:selected-id run-id})]
       [[:state/merge patch]
        (sync-query-effect :route/push-ui-state ui-state)]))

   :runs/deselect-run
   (fn [state]
     (let [{:keys [patch ui-state]} (next-ui-state-update state {:selected-id nil})]
       [[:state/merge patch]
        (sync-query-effect :route/push-ui-state ui-state)]))

   :route/navigated
   (fn [state ui-state]
     (let [current-query (routes/ui-state->query ui-state)
           canonical-state (canonical-ui-state state ui-state)
           canonical-query (routes/ui-state->query canonical-state)]
       (cond-> [[:state/merge (ui-state->patch canonical-state)]]
         (not= canonical-query current-query)
         (conj [:route/replace-ui-state canonical-query]))))

   :runs/retry-run
   (fn [state run-id]
     (let [retry-status (get-in state [:runs/retry run-id :status])]
       (when (not= retry-status :in-progress)
         [[:state/assoc-in [:runs/retry run-id] {:status :in-progress :message nil}]
          [:api/post-retry run-id]])))

   :runs/retry-success
   (fn [_state run-id message]
     [[:state/assoc-in [:runs/retry run-id] {:status :success :message message}]])

   :runs/retry-error
   (fn [_state run-id message]
     [[:state/assoc-in [:runs/retry run-id] {:status :error :message message}]])})


;; Effect handlers: perform side effects (state mutation, routing, API calls).
(def all-effects
  {:state/merge
   (fn [_ctx store patch]
     (swap! store merge patch))

   :state/assoc-in
   (fn [_ctx store path val]
     (swap! store assoc-in path val))

   :route/push-ui-state
   (fn [_ctx _store query]
     (routes/push-ui-state! query))

   :route/replace-ui-state
   (fn [_ctx _store query]
     (routes/replace-ui-state! query))

   :api/fetch-runs
   (fn [{:keys [dispatch]} _store]
     (-> (api/fetch-runs!)
         (.then (fn [runs] (dispatch [[:runs/fetch-success runs]])))
         (.catch (fn [err] (dispatch [[:runs/fetch-error (str err)]])))))

   :api/post-retry
   (fn [{:keys [dispatch]} _store run-id]
     (-> (api/retry-run! run-id)
         (.then (fn [result]
                  (dispatch [[:runs/retry-success run-id (:message result)]])))
         (.catch (fn [err]
                   (dispatch [[:runs/retry-error run-id (str err)]])))))})
