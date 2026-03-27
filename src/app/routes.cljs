(ns app.routes
  (:require
   [clojure.string :as str]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]))


(def ^:private route-name :route/dashboard)


(def ^:private router
  (rf/router
   [["/" {:name route-name}]]))


(defn- query-param
  [query key]
  (let [v (get query key)]
    (if (sequential? v) (first v) v)))


(def ^:private valid-statuses
  #{"success" "failed" "running" "queued"})


(defn parse-ui-state
  "Decodes URL query params into a UI state map. Unknown or invalid status values are dropped."
  [query]
  (let [query  (query-param query :q)
        run-id (query-param query :run)]
    {:status-filter (valid-statuses (query-param query :status))
     :search-query  (if (str/blank? query) "" query)
     :selected-id   (when-not (str/blank? run-id) run-id)}))


(defn ui-state->query
  "Encodes UI state into URL query params, omitting default/empty values."
  [{:keys [status-filter search-query selected-id]}]
  (cond-> {}
    (some? status-filter)
    (assoc :status status-filter)

    (not (str/blank? search-query))
    (assoc :q search-query)

    (some? selected-id)
    (assoc :run selected-id)))


(defn- current-path
  []
  (str (.-pathname js/location) (.-search js/location)))


(defn- navigate!
  [history-fn query]
  (let [target-path (rfe/href route-name nil query)]
    (when (not= target-path (current-path))
      (history-fn route-name nil query))))


(defn push-ui-state!
  [query]
  (navigate! rfe/push-state query))


(defn replace-ui-state!
  [query]
  (navigate! rfe/replace-state query))


(defn start!
  [dispatch!]
  (rfe/start!
   router
   (fn [match _history]
     (dispatch! {}
                [[:route/navigated
                  (parse-ui-state
                   (or (:query-params match) {}))]]))
   {:use-fragment false}))
