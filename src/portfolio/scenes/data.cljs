(ns portfolio.scenes.data)


(def runs
  [{:id            "r1"
    :pipeline-name "partner_order_ingest"
    :status        "failed"
    :started-at    "2026-03-15T08:37:00Z"
    :finished-at   "2026-03-15T08:40:30Z"
    :duration-secs 210
    :owner         "data@co"
    :trigger-type  "manual"
    :error-message "Schema drift detected: column 'amount' changed type from FLOAT to STRING"
    :steps         [{:name "extract"   :status "success"}
                    {:name "transform" :status "failed"}
                    {:name "load"      :status "queued"}]}
   {:id            "r2"
    :pipeline-name "billing_event_normaliser"
    :status        "success"
    :started-at    "2026-03-15T09:14:00Z"
    :finished-at   "2026-03-15T09:15:35Z"
    :duration-secs 95
    :owner         "ops@co"
    :trigger-type  "api"
    :error-message nil
    :steps         [{:name "extract"   :status "success"}
                    {:name "transform" :status "success"}
                    {:name "load"      :status "success"}]}
   {:id            "r3"
    :pipeline-name "daily_customer_sync"
    :status        "running"
    :started-at    "2026-03-15T09:51:00Z"
    :finished-at   nil
    :duration-secs nil
    :owner         "platform@co"
    :trigger-type  "manual"
    :error-message nil
    :steps         [{:name "extract"   :status "success"}
                    {:name "transform" :status "running"}
                    {:name "load"      :status "queued"}]}
   {:id            "r4"
    :pipeline-name "nightly_usage_rollup"
    :status        "queued"
    :started-at    "2026-03-15T10:28:00Z"
    :finished-at   nil
    :duration-secs nil
    :owner         "analytics@co"
    :trigger-type  "schedule"
    :error-message nil
    :steps         [{:name "extract"   :status "queued"}
                    {:name "transform" :status "queued"}
                    {:name "load"      :status "queued"}]}])


(def by-status
  (reduce (fn [m r] (assoc m (:status r) r)) {} runs))


(def base-state
  {:runs/all           runs
   :runs/loaded?       true
   :runs/loading?      false
   :runs/error         nil
   :runs/status-filter nil
   :runs/search-query  ""
   :runs/selected-id   nil
   :runs/retry         {}})
