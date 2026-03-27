(ns harness
  (:require
   [nexus.core :as nexus]
   [app.actions :as actions]
   [app.routes :as routes]
   [app.selectors :as sel]))

;; ---------------------------------------------------------------------------
;; Test data — one run per status
;; ---------------------------------------------------------------------------

(def failed-run
  {:id            "r1"
   :pipeline-name "partner_order_ingest"
   :status        "failed"
   :started-at    "2026-03-15T08:37:00Z"
   :finished-at   "2026-03-15T08:40:30Z"
   :duration-secs 210
   :owner         "data@co"
   :trigger-type  "manual"
   :error-message "Schema drift detected"
   :steps         [{:name "extract"   :status "success"}
                   {:name "transform" :status "failed"}
                   {:name "load"      :status "queued"}]})

(def success-run
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
                   {:name "load"      :status "success"}]})

(def running-run
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
                   {:name "load"      :status "queued"}]})

(def queued-run
  {:id            "r4"
   :pipeline-name "nightly_usage_rollup"
   :status        "queued"
   :started-at    "2026-03-15T10:28:00Z"
   :finished-at   nil
   :duration-secs nil
   :owner         "analytics@co"
   :trigger-type  "scheduled"
   :error-message nil
   :steps         [{:name "extract"   :status "queued"}
                   {:name "transform" :status "queued"}
                   {:name "load"      :status "queued"}]})

(def all-runs [failed-run success-run running-run queued-run])

;; ---------------------------------------------------------------------------
;; State presets
;; ---------------------------------------------------------------------------

(def initial-state
  {:runs/all           []
   :runs/loaded?       false
   :runs/loading?      false
   :runs/error         nil
   :runs/status-filter nil
   :runs/search-query  ""
   :runs/selected-id   nil
   :runs/retry         {}})

(def loaded-state
  (merge initial-state
         {:runs/all      all-runs
          :runs/loaded?  true
          :runs/loading? false}))

;; ---------------------------------------------------------------------------
;; Test nexus-map — real state effects, stubbed side effects
;; ---------------------------------------------------------------------------

(defn- make-test-effects
  [fx-log]
  (merge
   ;; Real state effects — same as production
   (select-keys actions/all-effects [:state/merge :state/assoc-in])
   ;; Side effects — just record them
   {:route/push-ui-state
    (fn [_ctx _store query]
      (swap! fx-log conj [:route/push-ui-state query]))

    :route/replace-ui-state
    (fn [_ctx _store query]
      (swap! fx-log conj [:route/replace-ui-state query]))

    :api/fetch-runs
    (fn [_ctx _store]
      (swap! fx-log conj [:api/fetch-runs]))

    :api/post-retry
    (fn [_ctx _store run-id]
      (swap! fx-log conj [:api/post-retry run-id]))}))

;; ---------------------------------------------------------------------------
;; Test context
;; ---------------------------------------------------------------------------

(defn make-ctx
  [state]
  (let [store  (atom state)
        fx-log (atom [])]
    {:store  store
     :fx-log fx-log
     :nmap   {:nexus/system->state deref
              :nexus/actions       actions/all-actions
              :nexus/effects       (make-test-effects fx-log)}}))

(defn fresh  [] (make-ctx initial-state))
(defn loaded [] (make-ctx loaded-state))

;; ---------------------------------------------------------------------------
;; Dispatch — delegates to real nexus
;; ---------------------------------------------------------------------------

(defn dispatch!
  [ctx action-vec]
  (reset! (:fx-log ctx) [])
  (nexus/dispatch (:nmap ctx) (:store ctx) {} [action-vec]))

;; ---------------------------------------------------------------------------
;; User actions — "I click / type / navigate"
;; ---------------------------------------------------------------------------

(defn fetch!        [ctx]        (dispatch! ctx [:runs/fetch]))
(defn fetch-ok!     [ctx runs]   (dispatch! ctx [:runs/fetch-success runs]))
(defn fetch-fail!   [ctx msg]    (dispatch! ctx [:runs/fetch-error msg]))

(defn click-run!    [ctx id]     (dispatch! ctx [:runs/select-run id]))
(defn close-drawer! [ctx]        (dispatch! ctx [:runs/deselect-run]))

(defn filter-by!    [ctx status] (dispatch! ctx [:runs/set-status-filter status]))
(defn search!       [ctx query]  (dispatch! ctx [:runs/set-search query]))

(defn retry!        [ctx id]     (dispatch! ctx [:runs/retry-run id]))
(defn retry-ok!     [ctx id msg] (dispatch! ctx [:runs/retry-success id msg]))
(defn retry-fail!   [ctx id msg] (dispatch! ctx [:runs/retry-error id msg]))

(defn navigate!     [ctx params]
  (dispatch! ctx [:route/navigated (routes/parse-ui-state params)]))

;; ---------------------------------------------------------------------------
;; Observations — "I see…"
;; ---------------------------------------------------------------------------

(defn vm      [ctx] (sel/dashboard-view-model @(:store ctx)))

(defn table   [ctx] (:table   (vm ctx)))
(defn toolbar [ctx] (:toolbar (vm ctx)))
(defn drawer  [ctx] (:drawer  (vm ctx)))

(defn rows      [ctx] (:rows (table ctx)))
(defn row-count [ctx] (count (rows ctx)))

(defn drawer-open? [ctx] (:show? (drawer ctx)))
(defn drawer-run   [ctx] (:run   (drawer ctx)))

(defn selected-rows [ctx] (filterv :selected? (rows ctx)))

(defn active-filter [ctx]
  (->> (:filters (toolbar ctx))
       (filter :active?)
       first
       :status))

(defn search-value [ctx] (:search-query (toolbar ctx)))

;; ---------------------------------------------------------------------------
;; Side-effect assertions — "what happened behind the scenes"
;; ---------------------------------------------------------------------------

(defn side-effects
  "All side effects recorded during the last dispatch."
  [ctx]
  @(:fx-log ctx))

(defn emitted?
  "Was an effect with this key emitted in the last dispatch?"
  [ctx effect-key]
  (boolean (some #(= effect-key (first %)) (side-effects ctx))))

(defn emitted-query
  "Returns the query map from the first matching route effect."
  [ctx effect-key]
  (some (fn [[k q]] (when (= k effect-key) q))
        (side-effects ctx)))
