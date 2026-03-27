(ns portfolio.scenes.run-row-scenes
  (:require
   [portfolio.replicant :refer-macros [defscene]]
   [portfolio.scenes.data :as data]
   [app.selectors :as selectors]
   [app.views.run-row :as row]))


(defn- row-for [status]
  (->> (selectors/table-props (assoc data/base-state :runs/status-filter status))
       :rows
       first))


(defscene row-failed
  [:table.runs-table [:tbody (row/view (row-for "failed"))]])


(defscene row-success
  [:table.runs-table [:tbody (row/view (row-for "success"))]])


(defscene row-running
  [:table.runs-table [:tbody (row/view (row-for "running"))]])


(defscene row-queued
  [:table.runs-table [:tbody (row/view (row-for "queued"))]])


(defscene row-selected
  [:table.runs-table
   [:tbody (row/view (first (:rows (selectors/table-props
                                    (assoc data/base-state :runs/selected-id "r1")))))]])
