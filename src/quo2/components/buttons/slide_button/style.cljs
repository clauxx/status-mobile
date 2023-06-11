(ns quo2.components.buttons.slide-button.style
  (:require
   [quo2.components.buttons.slide-button.consts :as consts]
   [quo2.components.buttons.slide-button.animations :as anim]
   [react-native.reanimated :as reanimated]
   [quo2.foundations.typography :as typography]))

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn thumb-container
  [{:keys [x-pos]}]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x x-pos}]}
   {}))

(defn thumb
  [{:keys [x-pos]} size track-width]
  (reanimated/apply-animations-to-style
   {:border-radius (anim/interpolate-track x-pos track-width size :thumb-border-radius)}
   {:width  size
    :height size
    :align-items :center
    :justify-content :center
    :background-color (:thumb consts/slide-colors)}))

(defn thumb-drop
  [{:keys [x-pos]} size track-width]
  (let [interpolate-track (partial anim/interpolate-track x-pos track-width size)]
    (reanimated/apply-animations-to-style
     {:transform [{:scale (interpolate-track :thumb-drop-scale)}]
      :z-index (interpolate-track :thumb-drop-z-index)
      :background-color (interpolate-track :thumb-drop-color)
      :left (interpolate-track :thumb-drop-position)}
     {:height size
      :width size
      :position :absolute
      :align-items :center
      :justify-content :center
      :border-radius (/ size 2)})))

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

(defn track-cover [{:keys [x-pos]} track-width thumb-size]
  (reanimated/apply-animations-to-style
   {:left (anim/interpolate-track x-pos track-width thumb-size :track-cover)}
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


