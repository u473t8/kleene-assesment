(ns app.views.run-row
  (:require
   [app.views.status-badge :as badge]))


(defn view
  [{:keys [id pipeline-name status selected? started-at-label duration-label owner]}]
  [:tr.runs-table__row
   {:replicant/key id

    :class [(case status
              "failed" "runs-table__row--failed"
              "success" "runs-table__row--success"
              "running" "runs-table__row--running"
              "queued" "runs-table__row--queued"
              nil)
            (when selected?
              "runs-table__row--selected")]
    :on {:click [[:runs/select-run id]]}}
   [:td.runs-table__cell.runs-table__cell--id id]
   [:td.runs-table__cell.runs-table__cell--name pipeline-name]
   [:td.runs-table__cell.runs-table__cell--status
    (badge/view status)]
   [:td.runs-table__cell.runs-table__cell--started started-at-label]
   [:td.runs-table__cell.runs-table__cell--duration duration-label]
   [:td.runs-table__cell.runs-table__cell--owner owner]])
