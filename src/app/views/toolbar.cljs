(ns app.views.toolbar)


(defn- filter-tab
  [{:keys [status label active?]}]
  [:button.toolbar__tab
   {:class [(when active? "toolbar__tab--active")]
    :on    {:click [[:runs/set-status-filter status]]}}
   label])


(defn view
  [{:keys [filters search-query summary-label]}]
  [:div.toolbar
   [:div.toolbar__summary
    [:span.toolbar__kicker "Filters"]
    [:p.toolbar__description summary-label]]
   [:div.toolbar__controls
    [:div.toolbar__filters
     (for [tab filters]
       (filter-tab tab))]
    [:label.toolbar__search-shell
     [:span.toolbar__search-label "Search"]
     [:input.toolbar__search
      {:type "text"
       :placeholder "Filter by pipeline name"
       :value search-query
       :on {:input [[:runs/set-search [:event.target/value]]]}}]]]])
