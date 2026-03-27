(ns app.state
  (:require [app.actions :as actions]))


(defn initial-state
  []
  {:runs/all []
   :runs/loaded? false
   :runs/loading? false
   :runs/error nil
   :runs/status-filter nil
   :runs/search-query ""
   :runs/selected-id nil
   :runs/retry {}})


(defonce store
  (atom (initial-state)))


(def nexus-map
  {:nexus/system->state deref

   :nexus/placeholders
   {:event.target/value
    (fn [dispatch-data]
      (some-> (or (:replicant/dom-event dispatch-data)
                  (:dom-event dispatch-data))
              .-target
              .-value))}

   :nexus/actions actions/all-actions
   :nexus/effects actions/all-effects})
