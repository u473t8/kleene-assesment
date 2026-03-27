(ns portfolio.scenes.status-badge-scenes
  (:require
   [portfolio.replicant :refer-macros [defscene]]
   [app.views.status-badge :as badge]))


(defscene all-badges
  [:div {:style {:display "flex" :gap "8px" :padding "12px"}}
   (badge/view "failed")
   (badge/view "success")
   (badge/view "running")
   (badge/view "queued")])
