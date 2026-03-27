(ns app.util
  (:require [clojure.string :as str]))


(defn normalize-query
  "Trims and lowercases a query string for case-insensitive search matching."
  [query]
  (-> (or query "")
      str/trim
      str/lower-case))


(defn run-matches?
  "Returns true if `run` passes both the status filter and search query.
  `normalized-query` must already be normalised via `normalize-query`."
  [status-filter normalized-query run]
  (and (or (nil? status-filter)
           (= status-filter (:status run)))
       (or (str/blank? normalized-query)
           (str/includes? (str/lower-case (:pipeline-name run)) normalized-query))))


(defn format-datetime
  "Formats an ISO 8601 datetime string as \"15 Mar, 08:37\". Returns \"—\" for nil."
  [iso-string]
  (if (nil? iso-string)
    "—"
    (let [d (js/Date. iso-string)]
      (str (.toLocaleDateString d "en-GB" #js {:day "2-digit" :month "short"})
           ", "
           (.toLocaleTimeString d
                                "en-GB"
                                #js {:hour "2-digit"
                                     :minute "2-digit"
                                     :hour12 false})))))


(defn format-duration
  "Formats a duration in seconds as \"1h 30m\", \"3m 12s\", or \"45s\". Returns \"—\" for nil."
  [seconds]
  (if (nil? seconds)
    "—"
    (let [h (quot seconds 3600)
          m (quot (mod seconds 3600) 60)
          s (mod seconds 60)]
      (cond
        (pos? h) (str h "h " m "m")
        (pos? m) (str m "m " s "s")
        :else (str s "s")))))
