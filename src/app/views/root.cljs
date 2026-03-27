(ns app.views.root
  (:require
   [app.views.toolbar :as toolbar]
   [app.views.runs-table :as runs-table]
   [app.views.detail-drawer :as drawer]))


(defn- stat-card
  [{:keys [label value modifier]}]
  [:div.dashboard__stat
   {:class [(case modifier
              "failed"  "dashboard__stat--failed"
              "running" "dashboard__stat--running"
              "queued"  "dashboard__stat--queued"
              "neutral" "dashboard__stat--neutral"
              nil)]}
   [:span.dashboard__stat-label label]
   [:strong.dashboard__stat-value value]])


(defn view
  [{header :header :as vm}]
  (let [{:keys [title eyebrow live subtitle stats]} header

        toolbar-props (:toolbar vm)
        table-props (:table vm)
        drawer-props (:drawer vm)
        {:keys [show?]} drawer-props]

    [:div.dashboard
     [:header.dashboard__header
      [:div.dashboard__masthead
       [:div.dashboard__intro
        [:div.dashboard__eyebrow eyebrow]
        [:div.dashboard__headline-row
         [:h1.dashboard__title title]
         [:div.dashboard__live
          [:span.dashboard__live-dot]
          live]]
        [:p.dashboard__subtitle subtitle]]
       [:div.dashboard__stats
        (for [stat stats]
          (stat-card stat))]]]
     [:main.dashboard__main
      (toolbar/view toolbar-props)
      (runs-table/view table-props)
      (if show?
        (drawer/view drawer-props)
        drawer/placeholder)]]))
