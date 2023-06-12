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

(defn thumb
  [size]
  {:width  size
   :height size
   :border-radius 12
   :background-color (:thumb consts/slide-colors)})

(defn thumb-icon-container
  [interpolate-track]
  (reanimated/apply-animations-to-style
   {:opacity (interpolate-track :thumb-icon-opacity)}
   {:flex 1
    :align-items :center
    :justify-content :center}))

(defn thumb-drop
  [interpolate-track size]
  (reanimated/apply-animations-to-style
   {:transform [{:scale (interpolate-track :thumb-drop-scale)}]
    :z-index (interpolate-track :thumb-drop-z-index)
    :background-color (interpolate-track :thumb-drop-color)
    :width (interpolate-track :thumb-drop-width)
    :opacity (interpolate-track :thumb-drop-opacity)
    :padding-right (interpolate-track :thumb-drop-padding)
    :left (interpolate-track :thumb-drop-position)}
   {:height size
    :position :absolute
    :align-items :center
    :justify-content :center
    :border-radius 12}))

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

(defn track-success
  [interpolate-track]
  (reanimated/apply-animations-to-style
   {:opacity (interpolate-track :track-success-opacity)}
   (merge absolute-fill {:transform [{:scale 1.3}]
                         :align-items :center
                         :justify-content :center})))

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


