(ns portfolio.scenes.toolbar-scenes
  (:require
   [portfolio.replicant :refer-macros [defscene]]
   [portfolio.scenes.data :as data]
   [app.selectors :as selectors]
   [app.views.toolbar :as toolbar]))


(defscene toolbar-default
  (toolbar/view (selectors/toolbar-props data/base-state)))


(defscene toolbar-filter-failed
  (toolbar/view (selectors/toolbar-props (assoc data/base-state :runs/status-filter "failed"))))


(defscene toolbar-filter-running
  (toolbar/view (selectors/toolbar-props (assoc data/base-state :runs/status-filter "running"))))


(defscene toolbar-search-active
  (toolbar/view (selectors/toolbar-props (assoc data/base-state :runs/search-query "crm"))))


(defscene toolbar-filter-and-search
  (toolbar/view (selectors/toolbar-props (assoc data/base-state
                                                :runs/status-filter "failed"
                                                :runs/search-query "partner"))))
