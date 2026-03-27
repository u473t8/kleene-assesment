(ns app.views.runs-table
  (:require
   [app.views.run-row :as row]))


(def ^:private loading-view
  [:div.runs-table--loading
   (repeat 6 [:div.skeleton-row])])


(defn- error-view
  [message]
  [:div.runs-table--error
   [:span.runs-table__error-icon "!"]
   [:span message]])


(defn- empty-view
  [message]
  [:div.runs-table--empty message])


(def ^:private header
  [:thead.runs-table__head
   [:tr
    [:th.runs-table__th.runs-table__th--id "Run ID"]
    [:th.runs-table__th.runs-table__th--name "Pipeline"]
    [:th.runs-table__th.runs-table__th--status "Status"]
    [:th.runs-table__th.runs-table__th--started "Started"]
    [:th.runs-table__th.runs-table__th--duration "Duration"]
    [:th.runs-table__th.runs-table__th--owner "Owner"]]])


(defn view
  [{:keys [rows show-loading? show-error? error-message show-empty? empty-message]}]
  (cond
    show-loading? loading-view
    show-error? (error-view error-message)
    show-empty? (empty-view empty-message)
    :else
    [:table.runs-table
     [:colgroup
      [:col.runs-table__col.runs-table__col--id]
      [:col.runs-table__col.runs-table__col--name]
      [:col.runs-table__col.runs-table__col--status]
      [:col.runs-table__col.runs-table__col--started]
      [:col.runs-table__col.runs-table__col--duration]
      [:col.runs-table__col.runs-table__col--owner]]
     header
     [:tbody.runs-table__body
      (for [row rows]
        (row/view row))]]))
