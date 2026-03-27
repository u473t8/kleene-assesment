(ns portfolio.core
  (:require
   [portfolio.ui :as ui]
   [replicant.dom :as r]
   portfolio.scenes.status-badge-scenes
   portfolio.scenes.run-row-scenes
   portfolio.scenes.runs-table-scenes
   portfolio.scenes.toolbar-scenes
   portfolio.scenes.detail-drawer-scenes))


(r/set-dispatch! (fn [_ _]))

(ui/start!
 {:config {:css-paths ["/css/app.css"]
           :viewport/defaults {:viewport/height "calc(100vh - 100px)"}}})
