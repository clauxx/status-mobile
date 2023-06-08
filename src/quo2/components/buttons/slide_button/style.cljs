(ns quo2.components.buttons.slide-button.style
  (:require
   [quo2.foundations.colors :as colors]
   [quo2.components.buttons.slide-button.consts
    :refer [track-padding]]
   [quo2.components.buttons.slide-button.animations
    :refer [clamp-track interpolate-track-cover]]
   [react-native.reanimated :as reanimated]
   [quo2.foundations.typography :as typography]))

(def slide-colors
  {:thumb (colors/custom-color-by-theme :blue 50 60)
   :text (colors/custom-color-by-theme :blue 50 60)
   :text-transparent colors/white-opa-40
   :track (colors/custom-color :blue 50 10)})

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn thumb-style
  [{:keys [x-pos]} track-width size]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (clamp-track x-pos track-width size)}]}
   {:width  size
    :height size
    :border-radius 12
    :align-items :center
    :justify-content :center
    :z-index 4
    :background-color (:thumb slide-colors)}))

(defn track-style
  [height]
  {:align-self       :stretch
   :align-items      :flex-start
   :justify-content  :center
   :padding          track-padding
   :height           height
   :border-radius    14
   :background-color (:track slide-colors)})

(defn track-cover-style [{:keys [x-pos]} track-width thumb-size]
  (reanimated/apply-animations-to-style
   {:left (interpolate-track-cover x-pos track-width thumb-size)}
   (merge
    {:z-index 3
     :overflow :hidden} absolute-fill)))

(defn track-cover-text-container-style
  [track-width] {:position :absolute
                 :right 0
                 :top 0
                 :bottom 0
                 :align-items :center
                 :justify-content :center
                 :flex-direction :row
                 :width @track-width})

(def track-text-style
  (merge {:color (:text slide-colors)}
         typography/paragraph-1
         typography/font-medium))


