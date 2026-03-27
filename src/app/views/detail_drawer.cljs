(ns app.views.detail-drawer
  (:require
   [app.views.status-badge :as badge]))


(def ^:private spinner [:span.spinner])


(defn- shell
  [{:keys [title on-close]} & body]
  [:aside.detail-drawer.detail-drawer--open
   [:div.detail-drawer__header
    [:div.detail-drawer__title-group
     [:div.detail-drawer__eyebrow "Run Overview"]
     [:h2.detail-drawer__title title]]
    [:button.detail-drawer__close {:on {:click on-close}} "×"]]
   (into [:div.detail-drawer__body] body)])


(defn view
  [{:keys [run retry-state show-error? show-retry?]}]
  (let [{:keys [id pipeline-name status
                started-at-label finished-at-label duration-label
                owner trigger-type error-message steps]}
        run]
    (shell
     {:title pipeline-name :on-close [[:runs/deselect-run]]}

     [:dl.detail-drawer__meta
      [:dt "Run ID"]   [:dd id]
      [:dt "Status"]   [:dd (badge/view status)]
      [:dt "Started"]  [:dd started-at-label]
      [:dt "Finished"] [:dd finished-at-label]
      [:dt "Duration"] [:dd duration-label]
      [:dt "Owner"]    [:dd owner]
      [:dt "Trigger"]  [:dd trigger-type]]

     (when show-error?
       [:div.detail-drawer__error error-message])

     [:div.detail-drawer__section
      [:div.detail-drawer__section-title
       "Execution Steps"]
      [:ol.detail-drawer__steps
       (for [{:keys [name status]} steps]
         [:li.detail-drawer__step
          {:class [(case status
                     "failed"  "detail-drawer__step--failed"
                     "success" "detail-drawer__step--success"
                     "running" "detail-drawer__step--running"
                     "queued"  "detail-drawer__step--queued"
                     nil)]}
          [:span.detail-drawer__step-name name]
          (badge/view status)])]]

     (when show-retry?
       [:div.detail-drawer__retry
        (case (:status retry-state)
          :in-progress
          [:button.retry-button.retry-button--in-progress {:disabled true}
           spinner
           "Retrying…"]

          :success
          [:div.retry-result.retry-result--success
           (:message retry-state)]

          :error
          [:div.retry-result.retry-result--error
           (:message retry-state)]

          [:button.retry-button {:on {:click [[:runs/retry-run id]]}}
           "Retry"])]))))


(def placeholder [:aside.detail-drawer])
