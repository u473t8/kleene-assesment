(ns app.core
  (:require
   [replicant.dom :as r]
   [nexus.core :as nexus]
   [app.routes :as routes]
   [app.state :as state]
   [app.selectors :as selectors]
   [app.views.root :as root]))


(def ^:private el (js/document.getElementById "app"))


(defn dispatch!
  [dispatch-data actions]
  (nexus/dispatch state/nexus-map state/store dispatch-data actions))


(defn ^:dev/after-load start!
  []
  (r/set-dispatch! dispatch!)
  (routes/start! dispatch!)
  (remove-watch state/store ::render)
  (add-watch state/store
             ::render
             (fn [_ _ _ new-state]
               (r/render el (root/view (selectors/dashboard-view-model new-state)))))
  (r/render el (root/view (selectors/dashboard-view-model @state/store)))
  (dispatch! {} [[:app/init]]))


(defn init
  []
  (start!))
