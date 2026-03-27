(ns portfolio.scenes.runs-table-scenes
  (:require
   [portfolio.replicant :refer-macros [defscene]]
   [portfolio.scenes.data :as data]
   [app.selectors :as selectors]
   [app.views.runs-table :as table]))


(defscene table-loading
  (table/view (selectors/table-props (assoc data/base-state :runs/all [] :runs/loading? true))))


(defscene table-error
  (table/view (selectors/table-props (assoc data/base-state
                                            :runs/all []
                                            :runs/error "Failed to fetch pipeline runs: connection timeout"))))


(defscene table-empty-no-filter
  (table/view (selectors/table-props (assoc data/base-state :runs/all []))))


(defscene table-empty-filtered
  (table/view (selectors/table-props (assoc data/base-state :runs/all [] :runs/status-filter "failed"))))


(defscene table-all-runs
  (table/view (selectors/table-props data/base-state)))


(defscene table-with-selected
  (table/view (selectors/table-props (assoc data/base-state :runs/selected-id "r1"))))
