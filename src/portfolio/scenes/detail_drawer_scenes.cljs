(ns portfolio.scenes.detail-drawer-scenes
  (:require
   [portfolio.replicant :refer-macros [defscene]]
   [portfolio.scenes.data :as data]
   [app.selectors :as selectors]
   [app.views.detail-drawer :as drawer]))


(def ^:private failed-run  (selectors/present-run-detail (get data/by-status "failed")))
(def ^:private success-run (selectors/present-run-detail (get data/by-status "success")))
(def ^:private running-run (selectors/present-run-detail (get data/by-status "running")))


(defscene drawer-failed-idle
  (drawer/view {:run         failed-run
                :retry-state nil
                :show-error? true
                :show-retry? true}))


(defscene drawer-failed-retrying
  (drawer/view {:run         failed-run
                :retry-state {:status :in-progress}
                :show-error? true
                :show-retry? true}))


(defscene drawer-retry-success
  (drawer/view {:run         failed-run
                :retry-state {:status :success :message "Retry accepted"}
                :show-error? true
                :show-retry? true}))


(defscene drawer-retry-error
  (drawer/view {:run         failed-run
                :retry-state {:status :error
                              :message "Retry not allowed while another run is active"}
                :show-error? true
                :show-retry? true}))


(defscene drawer-success-run
  (drawer/view {:run         success-run
                :retry-state nil
                :show-error? false
                :show-retry? false}))


(defscene drawer-running-run
  (drawer/view {:run         running-run
                :retry-state nil
                :show-error? false
                :show-retry? false}))
