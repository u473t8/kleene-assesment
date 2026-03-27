(ns app.views.status-badge)


(defn view
  [status]
  [:span.status-badge
   {:class (case status
             "failed" ["status-badge--failed"]
             "success" ["status-badge--success"]
             "running" ["status-badge--running"]
             "queued" ["status-badge--queued"]
             [])}
   status])
