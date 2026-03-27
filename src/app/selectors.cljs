(ns app.selectors
  (:require
   [app.util :as util]))


(def ^:private toolbar-filters
  [[nil "All"]
   ["success" "Success"]
   ["failed" "Failed"]
   ["running" "Running"]
   ["queued" "Queued"]])


(defn runs
  [state]
  (:runs/all state))


(defn runs-loading?
  [state]
  (:runs/loading? state))


(defn runs-error
  [state]
  (:runs/error state))


(defn status-filter
  [state]
  (:runs/status-filter state))


(defn search-query
  [state]
  (:runs/search-query state))


(defn selected-id
  [state]
  (:runs/selected-id state))


(defn- count-status
  [runs status]
  (count (filter #(= status (:status %)) runs)))


(defn- find-run
  [runs id]
  (first (filter #(= id (:id %)) runs)))


(defn visible-runs
  "Returns runs that pass both the active status filter and search query."
  [state]
  (let [sf (status-filter state)
        query (util/normalize-query (search-query state))]
    (filterv #(util/run-matches? sf query %) (runs state))))


(defn selected-run
  "Returns the selected run, or nil if none is selected or the selected run is not currently visible."
  [state]
  (some->> (selected-id state)
           (find-run (visible-runs state))))


(defn filtered?
  "Returns true if a status filter or non-empty search query is active."
  [state]
  (or (some? (status-filter state))
      (seq (search-query state))))


(defn show-table-error?
  [state]
  (some? (runs-error state)))


(defn show-table-empty?
  "Returns true when runs are loaded with no error and no visible runs."
  [state]
  (and (not (runs-loading? state))
       (not (show-table-error? state))
       (empty? (visible-runs state))))


(defn- present-run-row
  [run selected-run-id]
  {:id (:id run)
   :pipeline-name (:pipeline-name run)
   :status (:status run)
   :selected? (= selected-run-id (:id run))
   :started-at-label (util/format-datetime (:started-at run))
   :duration-label (util/format-duration (:duration-secs run))
   :owner (:owner run)})


(defn present-run-rows
  "Returns view-model rows for all visible runs, with the selected run marked."
  [state]
  (mapv #(present-run-row % (selected-id state)) (visible-runs state)))


(defn present-run-detail
  [run]
  {:id (:id run)
   :pipeline-name (:pipeline-name run)
   :status (:status run)
   :started-at-label (util/format-datetime (:started-at run))
   :finished-at-label (util/format-datetime (:finished-at run))
   :duration-label (util/format-duration (:duration-secs run))
   :owner (:owner run)
   :trigger-type (:trigger-type run)
   :error-message (:error-message run)
   :steps (:steps run)})


(defn header-props
  "Returns the view model for the page header, including run count stats by status."
  [state]
  (let [all-runs (runs state)]
    {:title "Pipeline Runs"
     :eyebrow "Overview"
     :live "Demo data"
     :subtitle "Scan recent runs, filter quickly, and open details only when needed."
     :stats [{:label "Total Runs" :value (count all-runs) :modifier "neutral"}
             {:label "Failed" :value (count-status all-runs "failed") :modifier "failed"}
             {:label "Running" :value (count-status all-runs "running") :modifier "running"}
             {:label "Queued" :value (count-status all-runs "queued") :modifier "queued"}]}))


(defn toolbar-props
  "Returns the view model for the toolbar: filter buttons, search query, and visible/total count label."
  [state]
  (let [visible-count (count (visible-runs state))
        total (count (runs state))]
    {:filters (mapv (fn [[status label]]
                      {:status status
                       :label label
                       :active? (= (status-filter state) status)})
                    toolbar-filters)
     :search-query (search-query state)
     :summary-label (str visible-count " of " total " runs shown")}))


(defn table-props
  "Returns the view model for the runs table: rows plus loading/error/empty state flags."
  [state]
  {:rows (present-run-rows state)
   :show-loading? (runs-loading? state)
   :show-error? (show-table-error? state)
   :error-message (runs-error state)
   :show-empty? (show-table-empty? state)
   :empty-message (if (filtered? state)
                    "No runs match the current filter."
                    "No pipeline runs found.")})


(defn drawer-props
  "Returns the view model for the detail drawer. `:show?` is false when no run is selected or
  visible. Includes retry state and derived `:show-error?`/`:show-retry?` flags."
  [state]
  (let [run (selected-run state)
        id (selected-id state)]
    {:show? (some? run)
     :run (some-> run present-run-detail)
     :retry-state (get-in state [:runs/retry id])
     :show-error? (some? (:error-message run))
     :show-retry? (= "failed" (:status run))}))


(defn dashboard-view-model
  "Aggregates all section view models into a single map for the dashboard root."
  [state]
  {:header (header-props state)
   :toolbar (toolbar-props state)
   :table (table-props state)
   :drawer (drawer-props state)})
