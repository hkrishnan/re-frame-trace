(ns day8.re-frame.trace.view.epoch
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.re-com :as rc]
            [reagent.core :as r]
            [garden.units :refer [em px percent]]
            [day8.re-frame.trace.common-styles :as common]
            [day8.re-frame.trace.view.components :as data-browser]))

(rf/reg-sub
  :epoch/root
  (fn [db _]
    (:epoch db)))

(rf/reg-sub
  :epoch/epochs
  :<- [:epoch/root]
  (fn [root _]
    [{:epoch-number 1
      :epoch-title  [:add-todo]
      :epoch-type   :event
      :elapsed-time 50
      :time-ago     "5 s ago"
      :summary      {:render {:elapsed-time 20}
                     :subs   {:elapsed-time 20
                              :lifecycle    {:created   0
                                             :destroyed 0
                                             :ran       6}}
                     :event  {:elapsed-time 10
                              :db           {:before {:navigation/page :main/define}
                                             :after  {:navigation/page :main/demand}}}}
      }
     {:epoch-number 2
      :epoch-title  "micro-epoch"
      :epoch-type   :micro-epoch
      :time-ago     "30 s ago"}
     {:epoch-number 3
      :epoch-title  [:remove-todo]
      :epoch-type   :event
      :elapsed-time 8}
     {:epoch-number 4
      :epoch-title  [:clear-todos]
      :epoch-type   :event
      :elapsed-time 7}
     {:epoch-number 5
      :epoch-title  [:add-todo]
      :epoch-type   :event
      :elapsed-time 20}
     {:epoch-number 6
      :epoch-title  [:add-todo]
      :epoch-type   :event
      :elapsed-time 5}]))

(rf/reg-event-db
  :epoch/add-new-epoch
  (fn [db _]
    ()))

(defn elapsed-time [ms]
  [:span.elapsed-time ms " ms"])

(defn subs-lifecycle [{:keys [created destroyed ran]}]
  [:span created " created, " ran " ran, " destroyed " destroyed"])

(def css
  [:.epoch-panel
   [:.epoch-container
    {:border [[(px 1) "solid" common/light-gray]]
     :margin (px 5)}]
   [:.epoch-header {:padding (px 10)
                    :cursor  "pointer"}]
   [:.epoch-details
    {:padding [[(px 10) (px 10) (px 10) (px 40)]]}]
   [:.event-epoch
    {:color common/event-color}]
   [:span.expansion
    {:color       common/medium-gray
     :padding     "0 2px"
     :width       "10px"
     :user-select "none"
     :font-family "sans-serif"}]
   [:span.time-ago
    {:color common/medium-gray}]
   [:.separator
    {:color  common/light-gray
     :margin "0px 10px"}]
   [:span.event-details {:color common/event-color}]
   [:span.render-details {:color common/render-color}]
   [:span.subs-details {:color common/subs-color}]])

(defn single-epoch [epoch]
  (let [expanded? (if (= 1 (epoch :epoch-number))
                    (r/atom true)
                    (r/atom false))]
    (fn []
      [rc/v-box
       :class "epoch-container"
       :children [[rc/h-box
                   :attr {:on-click #(swap! expanded? not)}
                   :class "epoch-header"
                   :justify :between
                   :gap "7px"
                   :children [[rc/h-box
                               :gap "7px"
                               :children [[:span.expansion (if @expanded? "▼" "▶")]
                                          [:span {:class (when (= :event (:epoch-type epoch))
                                                           "event-epoch")}
                                           (prn-str (:epoch-title epoch))]]]
                              [rc/h-box
                               :gap "10px"
                               :children [[:span.time-ago (:time-ago epoch)]
                                          (when-let [epoch-time (:elapsed-time epoch)]
                                            [elapsed-time epoch-time])]]]]
                  (when @expanded?
                    [rc/line
                     :class "separator"
                     :color common/light-gray])
                  (when @expanded?
                    (when-let [summary (:summary epoch)]
                      [rc/v-box
                       :class "epoch-details"
                       :gap "5px"
                       :children [[rc/h-box :gap "15px" :children [[rc/h-box :size "0 0 55px" :children [[:span.event-details "Event"]]] [elapsed-time (get-in summary [:event :elapsed-time])]]]
                                  [rc/h-box :gap "15px" :children [[rc/h-box :size "0 0 55px" :children [[:span.render-details "Render"]]] [elapsed-time (get-in summary [:render :elapsed-time])]]]
                                  [rc/h-box :gap "15px"
                                   :children [[rc/h-box :size "0 0 55px" :children [[:span.subs-details "Subs"]]]
                                              [elapsed-time (get-in summary [:subs :elapsed-time])]
                                              [subs-lifecycle (get-in summary [:subs :lifecycle])]]]
                                  [rc/h-box :gap "15px"
                                   :children [[rc/h-box :size "0 0 55px" :children [[:span.db-details "App DB"]]]
                                              [data-browser/simple-render (get-in summary [:event :db])]]]
                                  ]]))]])))

(defn render []
  [:div.epoch-panel
   [:h1 "Epochs"]
   [:button {:on-click #(rf/dispatch [:epoch/add-new-epoch])}]
   (for [epoch @(rf/subscribe [:epoch/epochs])]
     ^{:key (:epoch-number epoch)}
     [single-epoch epoch])

   ;; Show everything within the current epoch/list of epochs

   ])
