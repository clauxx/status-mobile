(ns quo2.components.buttons.slide-button.animations
  (:require
   [quo2.components.buttons.slide-button.consts
    :refer [track-padding
            timing-duration
            threshold-frac]]
   [react-native.gesture :as gesture]
   [quo.react :as react]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

(defn init-animations [] {:x-pos (reanimated/use-shared-value 0)})

(defn calc-usable-track [track-width thumb-size]
  [0 (- (or @track-width 200) (* track-padding 2) thumb-size)])

(def ^:private extrapolation {:extrapolateLeft  "clamp"
                              :extrapolateRight "clamp"})

(defn clamp-track [x-pos track-width thumb-size]
  (let [track-dim (calc-usable-track track-width thumb-size)]
    (reanimated/interpolate
     x-pos
     track-dim
     track-dim
     extrapolation)))

(defn interpolate-track-cover [x-pos track-width thumb-size]
  (let [track-dim (calc-usable-track track-width thumb-size)
        clamped (clamp-track x-pos track-width thumb-size)]
    (reanimated/interpolate
     clamped
     track-dim
     (-> track-dim
         vec
         (assoc 0 (/ thumb-size 2)))
     extrapolation)))

(defn- gesture-on-update
  [event
   offset
   x-pos
   thumb-state
   track-width
   thumb-size]
  (let [x-translation (oops/oget event "translationX")
        x (+ x-translation @offset)]
    (doall [(reanimated/set-shared-value x-pos x)
            (doall [(when (= @thumb-state :rest)
                      (reset! thumb-state :dragging))
                    (when (>= x (get (calc-usable-track track-width thumb-size) 1))
                      (reset! thumb-state :complete))])])))

(defn- gesture-on-end
  [event
   offset
   complete-threshold
   thumb-state]
  (let [x-translation (oops/oget event "translationX")
        x (+ x-translation @offset)]
    (if (<= x complete-threshold)
      (reset! thumb-state :incomplete)
      (reset! thumb-state :complete))))

(defn drag-gesture
  [{:keys [x-pos]}
   disabled?
   track-width
   thumb-state
   thumb-size]
  (let [offset (react/state 0)
        complete-threshold (* @track-width threshold-frac)]
    (-> (gesture/gesture-pan)
        (gesture/enabled (not disabled?))
        (gesture/on-update
         (fn [event]
           (gesture-on-update event offset x-pos thumb-state track-width thumb-size)))
        (gesture/on-end
         (fn [event]
           (gesture-on-end event offset complete-threshold thumb-state)))
        (gesture/on-start
         (fn [_]
           (reset! offset (reanimated/get-shared-value x-pos)))))))

(defn animate-slide
  [value to-position]
  (reanimated/animate-shared-value-with-timing
   value to-position timing-duration :linear))

(defn animate-complete
  [value end-position]
  (reanimated/with-sequence
    (animate-slide value end-position)))


