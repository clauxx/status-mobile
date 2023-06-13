(ns quo2.components.buttons.slide-button.style
  (:require
   [quo2.components.buttons.slide-button.consts :as consts]
   [react-native.reanimated :as reanimated]
   [quo2.foundations.typography :as typography]))

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn thumb-container
  [interpolate-track]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (interpolate-track :track-clamp)}]}
   {}))

(defn thumb-placeholder
  [size]
  {:width  size
   :height size})

(defn arrow-icon-container
  [interpolate-track thumb-size]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (interpolate-track :arrow-icon-position)}]}
   {:width thumb-size
    :height thumb-size
    :align-items :center
    :justify-content :center}))

(defn thumb
  [interpolate-track thumb-size]
  (reanimated/apply-animations-to-style
   {:width (interpolate-track :thumb-width)}
   {:background-color (consts/slide-colors :thumb)
    :border-radius 12
    :position :absolute
    :right 0
    :top 0
    :height thumb-size
    :align-items :center
    :overflow :hidden
    :justify-content :center}))

(defn check-icon
  [interpolate-track thumb-size]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (interpolate-track :check-icon-position)}]}
   {:width thumb-size
    :height thumb-size
    :position :absolute
    :top 0
    :left 0
    :z-index 2
    :align-items :center
    :justify-content :center}))

(defn action-icon
  [interpolate-track size]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (interpolate-track :action-icon-position)}]}
   {:height size
    :width size
    :position :absolute
    :align-items :center
    :left 0
    :top 0
    :z-index 2
    :flex-direction :row
    :justify-content :space-around}))

(defn track-container
  [height]
  {:align-self       :stretch
   :align-items      :center
   :justify-content  :center
   :height height})

(defn track
  [disabled?]
  {:align-items      :flex-start
   :justify-content  :center
   :border-radius    14
   :align-self       :stretch
   :padding          consts/track-padding
   :opacity          (if disabled? 0.3 1)
   :background-color (:track consts/slide-colors)})

(defn track-cover
  [interpolate-track]
  (reanimated/apply-animations-to-style
   {:left (interpolate-track :track-cover)}
   (merge {:overflow :hidden} absolute-fill)))

(defn track-cover-text-container
  [track-width]
  {:position :absolute
   :right 0
   :top 0
   :bottom 0
   :align-items :center
   :justify-content :center
   :flex-direction :row
   :width track-width})

(def track-text
  (merge {:color (:text consts/slide-colors)
          :z-index 0}
         typography/paragraph-1
         typography/font-medium))


