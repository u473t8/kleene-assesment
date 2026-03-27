(ns app.scenarios-test
  (:require
   [cljs.test :refer [deftest testing is]]
   [harness :as harness]))


;; ---------------------------------------------------------------------------
;; App states
;; ---------------------------------------------------------------------------

(deftest app-states-test

  (testing "initial load → loading spinner, no rows, drawer closed"
    (let [ctx (harness/fresh)]
      (harness/fetch! ctx)
      (is (:show-loading? (harness/table ctx)))
      (is (zero? (harness/row-count ctx)))
      (is (not (harness/drawer-open? ctx)))))

  (testing "runs arrive → table shows all four runs"
    (let [ctx (harness/fresh)]
      (harness/fetch! ctx)
      (harness/fetch-ok! ctx harness/all-runs)
      (is (not (:show-loading? (harness/table ctx))))
      (is (= 4 (harness/row-count ctx)))))

  (testing "runs arrive empty → 'No pipeline runs found.'"
    (let [ctx (harness/fresh)]
      (harness/fetch! ctx)
      (harness/fetch-ok! ctx [])
      (is (:show-empty? (harness/table ctx)))
      (is (= "No pipeline runs found." (:empty-message (harness/table ctx))))))

  (testing "fetch fails → error message shown"
    (let [ctx (harness/fresh)]
      (harness/fetch! ctx)
      (harness/fetch-fail! ctx "Connection refused")
      (is (:show-error? (harness/table ctx)))
      (is (= "Connection refused" (:error-message (harness/table ctx)))))))


;; ---------------------------------------------------------------------------
;; Filter
;; ---------------------------------------------------------------------------

(deftest filter-test

  (testing "no filter → all runs visible"
    (let [ctx (harness/loaded)]
      (is (= 4 (harness/row-count ctx)))))

  (testing "filter by each status → only matching runs"
    (doseq [status ["success" "failed" "running" "queued"]]
      (let [ctx (harness/loaded)]
        (harness/filter-by! ctx status)
        (is (= 1 (harness/row-count ctx))
            (str "expected 1 run for status " status))
        (is (every? #(= status (:status %)) (harness/rows ctx))))))

  (testing "clear filter → all runs visible again"
    (let [ctx (harness/loaded)]
      (harness/filter-by! ctx "failed")
      (harness/filter-by! ctx nil)
      (is (= 4 (harness/row-count ctx)))))

  (testing "active filter is highlighted in toolbar"
    (let [ctx (harness/loaded)]
      (harness/filter-by! ctx "failed")
      (is (= "failed" (harness/active-filter ctx))))))


;; ---------------------------------------------------------------------------
;; Search
;; ---------------------------------------------------------------------------

(deftest search-test

  (testing "empty search → all runs visible"
    (let [ctx (harness/loaded)]
      (is (= 4 (harness/row-count ctx)))))

  (testing "type a name → only matching runs"
    (let [ctx (harness/loaded)]
      (harness/search! ctx "partner")
      (is (= 1 (harness/row-count ctx)))
      (is (= "partner_order_ingest" (:pipeline-name (first (harness/rows ctx)))))))

  (testing "search is case-insensitive"
    (let [ctx (harness/loaded)]
      (harness/search! ctx "PARTNER")
      (is (= 1 (harness/row-count ctx)))))

  (testing "clear search → all runs again"
    (let [ctx (harness/loaded)]
      (harness/search! ctx "partner")
      (harness/search! ctx "")
      (is (= 4 (harness/row-count ctx)))))

  (testing "changing filter does not reset search"
    (let [ctx (harness/loaded)]
      (harness/search! ctx "billing")
      (harness/filter-by! ctx "success")
      (is (= "billing" (harness/search-value ctx))))))


;; ---------------------------------------------------------------------------
;; Filter + search
;; ---------------------------------------------------------------------------

(deftest filter-and-search-test

  (testing "filter 'failed' + search 'partner' → only matching failed run"
    (let [ctx (harness/loaded)]
      (harness/filter-by! ctx "failed")
      (harness/search! ctx "partner")
      (is (= 1 (harness/row-count ctx)))
      (is (= "failed" (:status (first (harness/rows ctx)))))))

  (testing "no filter + search 'billing' → matches across all statuses"
    (let [ctx (harness/loaded)]
      (harness/search! ctx "billing")
      (is (= 1 (harness/row-count ctx)))
      (is (= "billing_event_normaliser" (:pipeline-name (first (harness/rows ctx)))))))

  (testing "filter + search with no matches → empty state"
    (let [ctx (harness/loaded)]
      (harness/filter-by! ctx "queued")
      (harness/search! ctx "partner")
      (is (:show-empty? (harness/table ctx)))
      (is (= "No runs match the current filter." (:empty-message (harness/table ctx)))))))


;; ---------------------------------------------------------------------------
;; Run selection and drawer
;; ---------------------------------------------------------------------------

(deftest selection-and-drawer-test

  (testing "click a run → drawer opens with full detail"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (is (harness/drawer-open? ctx))
      (let [run (harness/drawer-run ctx)]
        (is (= "r1" (:id run)))
        (is (= "partner_order_ingest" (:pipeline-name run)))
        (is (some? (:started-at-label run)))
        (is (some? (:finished-at-label run)))
        (is (some? (:duration-label run)))
        (is (= "data@co" (:owner run)))
        (is (= "manual" (:trigger-type run))))))

  (testing "failed run → drawer shows steps and error"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (is (= 3 (count (:steps (harness/drawer-run ctx)))))
      (is (:show-error? (harness/drawer ctx)))
      (is (= "Schema drift detected" (:error-message (harness/drawer-run ctx))))))

  (testing "successful run → no error in drawer"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r2")
      (is (not (:show-error? (harness/drawer ctx))))))

  (testing "close drawer → selection cleared"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/close-drawer! ctx)
      (is (not (harness/drawer-open? ctx)))))

  (testing "click different run → drawer updates"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/click-run! ctx "r2")
      (is (= "r2" (:id (harness/drawer-run ctx))))))

  (testing "selected row is highlighted in table"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (is (= 1 (count (harness/selected-rows ctx))))
      (is (= "r1" (:id (first (harness/selected-rows ctx))))))))


;; ---------------------------------------------------------------------------
;; Filter interaction with selected run
;; ---------------------------------------------------------------------------

(deftest filter-selection-interaction-test

  (testing "selected run stays open when visible under new filter"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/filter-by! ctx "failed")
      (is (harness/drawer-open? ctx))
      (is (= "r1" (:id (harness/drawer-run ctx))))))

  (testing "selected run deselected when hidden by filter"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/filter-by! ctx "success")
      (is (not (harness/drawer-open? ctx)))))

  (testing "switch to 'all' → selection preserved"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/filter-by! ctx "failed")
      (harness/filter-by! ctx nil)
      (is (harness/drawer-open? ctx))
      (is (= "r1" (:id (harness/drawer-run ctx)))))))


;; ---------------------------------------------------------------------------
;; Retry flow
;; ---------------------------------------------------------------------------

(deftest retry-test

  (testing "failed run → retry button visible"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (is (:show-retry? (harness/drawer ctx)))))

  (testing "non-failed run → no retry button"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r2")
      (is (not (:show-retry? (harness/drawer ctx))))))

  (testing "click retry → in-progress state"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/retry! ctx "r1")
      (is (= :in-progress (get-in (harness/drawer ctx) [:retry-state :status])))))

  (testing "retry succeeds → success message"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/retry! ctx "r1")
      (harness/retry-ok! ctx "r1" "Retry accepted")
      (let [retry (:retry-state (harness/drawer ctx))]
        (is (= :success (:status retry)))
        (is (= "Retry accepted" (:message retry))))))

  (testing "retry fails → error message"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/retry! ctx "r1")
      (harness/retry-fail! ctx "r1" "Retry not allowed")
      (let [retry (:retry-state (harness/drawer ctx))]
        (is (= :error (:status retry)))
        (is (= "Retry not allowed" (:message retry))))))

  (testing "retry while already in-progress → no-op"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/retry! ctx "r1")
      (harness/retry! ctx "r1")
      (is (empty? (harness/side-effects ctx))))))


;; ---------------------------------------------------------------------------
;; URL → view (user navigates directly to a URL)
;; ---------------------------------------------------------------------------

(deftest url-to-view-test

  (testing "?status=failed → failed filter active"
    (let [ctx (harness/loaded)]
      (harness/navigate! ctx {:status "failed"})
      (is (every? #(= "failed" (:status %)) (harness/rows ctx)))))

  (testing "?status=bogus → no filter, all runs visible"
    (let [ctx (harness/loaded)]
      (harness/navigate! ctx {:status "bogus"})
      (is (= 4 (harness/row-count ctx)))))

  (testing "?q=billing → search pre-filled, results filtered"
    (let [ctx (harness/loaded)]
      (harness/navigate! ctx {:q "billing"})
      (is (= "billing" (harness/search-value ctx)))
      (is (= 1 (harness/row-count ctx)))))

  (testing "?run=r1 → drawer opens for that run"
    (let [ctx (harness/loaded)]
      (harness/navigate! ctx {:run "r1"})
      (is (harness/drawer-open? ctx))
      (is (= "r1" (:id (harness/drawer-run ctx))))))

  (testing "?run=r1&status=success → run hidden by filter, URL corrected"
    (let [ctx (harness/loaded)]
      ;; r1 is "failed", filter "success" hides it → deselected
      (harness/navigate! ctx {:status "success" :run "r1"})
      (is (not (harness/drawer-open? ctx)))
      (is (harness/emitted? ctx :route/replace-ui-state)))))


;; ---------------------------------------------------------------------------
;; View → URL (user actions produce URL effects)
;; ---------------------------------------------------------------------------

(deftest view-to-url-test

  (testing "set filter → URL gets ?status=failed"
    (let [ctx (harness/loaded)]
      (harness/filter-by! ctx "failed")
      (is (= "failed" (:status (harness/emitted-query ctx :route/replace-ui-state))))))

  (testing "clear filter → URL loses ?status"
    (let [ctx (harness/loaded)]
      (harness/filter-by! ctx "failed")
      (harness/filter-by! ctx nil)
      (is (nil? (:status (harness/emitted-query ctx :route/replace-ui-state))))))

  (testing "type search → URL gets ?q=billing"
    (let [ctx (harness/loaded)]
      (harness/search! ctx "billing")
      (is (= "billing" (:q (harness/emitted-query ctx :route/replace-ui-state))))))

  (testing "click run → URL gets ?run=r1"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (is (= "r1" (:run (harness/emitted-query ctx :route/push-ui-state))))))

  (testing "close drawer → URL loses ?run"
    (let [ctx (harness/loaded)]
      (harness/click-run! ctx "r1")
      (harness/close-drawer! ctx)
      (is (nil? (:run (harness/emitted-query ctx :route/push-ui-state)))))))


;; ---------------------------------------------------------------------------
;; Unrepresentable states
;; ---------------------------------------------------------------------------

(deftest unrepresentable-states-test

  (testing "?run=nonexistent → no drawer, id dropped"
    (let [ctx (harness/loaded)]
      (harness/navigate! ctx {:run "nonexistent"})
      (is (not (harness/drawer-open? ctx)))))

  (testing "?run=r1&status=success → run hidden, URL corrected"
    (let [ctx (harness/loaded)]
      (harness/navigate! ctx {:status "success" :run "r1"})
      (is (not (harness/drawer-open? ctx)))
      (is (harness/emitted? ctx :route/replace-ui-state)))))
