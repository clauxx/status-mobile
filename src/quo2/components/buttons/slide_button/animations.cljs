(ns quo2.components.buttons.slide-button.animations
  (:require
   [quo2.components.buttons.slide-button.consts :as consts]
   [react-native.gesture :as gesture]
   [quo.react :as react]
   [oops.core :as oops]
   [utils.worklets.core :as w]
   [react-native.reanimated :as reanimated]))

;; Utils 
(defn clamp-value [value min-value max-value]
  (cond
    (< value min-value) min-value
    (> value max-value) max-value
    :else value))

(defn calc-usable-track
  "Calculate the track section in which the
  thumb can move in. Mostly used for interpolations."
  [track-width thumb-size]
  (- (or @track-width 200) (* consts/track-padding 2) thumb-size))

(def ^:private extrapolation {:extrapolateLeft  "clamp"
                              :extrapolateRight "clamp"})

(defn calc-final-padding
  "Calculate the padding animation applied
  to the track to surround the thumb."
  [track-width thumb-size]
  (-> track-width
      (/ 2)
      (- (/ thumb-size 2))
      (- consts/track-padding)))

;; Interpolations
(defn clamp-track
  "Clamps the thumb position to the usable portion
   of the track"
  [x-pos track-width thumb-size]
  (let [track-dim [0 (calc-usable-track track-width thumb-size)]]
    (reanimated/interpolate x-pos
                            track-dim
                            track-dim
                            extrapolation)))

(defn interpolate-track-cover
  "Interpolates the start edge of the track text container
  based on the thumb position, which should hide the text
  behind the thumb."
  [x-pos track-width thumb-size]
  (let [usable-track (calc-usable-track track-width
                                        thumb-size)
        output-start-pos (/ thumb-size 2)
        clamped (clamp-track x-pos
                             track-width
                             thumb-size)]
    (reanimated/interpolate clamped
                            [0 usable-track]
                            [output-start-pos usable-track]
                            extrapolation)))

(defn drop-interpolation-input
  [track-width thumb-size]
  (let [track (calc-usable-track track-width thumb-size)
        in-start 0
        in-mid (* track 0.5)
        in-mid-end (* track 0.75)
        in-end track]
    [in-start in-mid in-mid-end in-end]))

(defn interpolate-x
  [x-pos track-width thumb-size {:keys [out-start  out-mid out-mid-end out-end]}]
  (let [input (drop-interpolation-input track-width thumb-size)]
    (reanimated/interpolate x-pos
                            input
                            [out-start out-mid out-mid-end out-end]
                            extrapolation)))

(defn interpolate-thumb-border-radius
  [x-pos track-width thumb-size]
  (interpolate-x x-pos
                 track-width
                 thumb-size
                 {:out-start consts/thumb-border-radius
                  :out-mid consts/thumb-border-radius
                  :out-mid-end consts/thumb-border-radius
                  :out-end (/ thumb-size 2)}))

(defn interpolate-thumb-drop-position
  [x-pos track-width thumb-size]
  (interpolate-x x-pos
                 track-width
                 thumb-size
                 {:out-start 0
                  :out-mid (- (* thumb-size 0.4))
                  :out-mid-end (- (* thumb-size 0.8))
                  :out-end 0}))

(defn interpolate-thumb-drop-scale
  [x-pos track-width thumb-size]
  (interpolate-x x-pos
                 track-width
                 thumb-size
                 {:out-start 0
                  :out-mid 0.6
                  :out-mid-end 0.7
                  :out-end 1}))

(defn interpolate-thumb-drop-z-index
  [x-pos track-width thumb-size]
  (interpolate-x x-pos
                 track-width
                 thumb-size
                 {:out-start 0
                  :out-mid-start 0
                  :out-mid 0
                  :out-mid-end 1
                  :out-end 1}))

(defn interpolate-x-color
  [x-pos track-width thumb-size {:keys [out-start  out-mid out-mid-end out-end]}]
  (let [input (drop-interpolation-input track-width thumb-size)]
    (reanimated/interpolate-color x-pos
                                  input
                                  [out-start out-mid out-mid-end out-end])))

(defn interpolate-thumb-drop-color
  [x-pos track-width thumb-size]
  (let [main-col (:thumb consts/slide-colors)
        mid-dark-col "#0a2bdb"
        dark-col "#0820a4"]
    (interpolate-x-color x-pos
                         track-width
                         thumb-size
                         {:out-start dark-col
                          :out-mid-start mid-dark-col
                          :out-mid mid-dark-col
                          :out-mid-end mid-dark-col
                          :out-end main-col})))

;; Animation helpers
(defn- animate-spring
  [value to-value]
  (reanimated/animate-shared-value-with-spring value
                                               to-value
                                               {:mass      1
                                                :damping   6
                                                :stiffness 300}))

(defn- animate-timing
  [value to-position duration]
  (reanimated/animate-shared-value-with-timing value
                                               to-position
                                               duration
                                               :linear))

(defn- animate-sequence [anim & seq-animations]
  (reanimated/set-shared-value anim
                               (apply reanimated/with-sequence
                                      seq-animations)))

;; Animations
(defn init-animations []
  {:x-pos (reanimated/use-shared-value 0)
   :slide-state (reanimated/use-shared-value :rest)
   ;TODO remove
   :thumb-border-radius (reanimated/use-shared-value 12)
   :thumb-drop-width (reanimated/use-shared-value 0)
   :track-scale (reanimated/use-shared-value 1)
   :track-border-radius (reanimated/use-shared-value 14)
   :track-container-padding (reanimated/use-shared-value 0)})

(defn complete-animation
  [{:keys [track-border-radius] :as animations} slide-state]
  (reanimated/run-on-js
   (js/setTimeout (fn [] (reset! slide-state :complete)) 300)))

;; Gestures
(defn drag-gesture
  [animations
   disabled?
   track-width
   slide-state
   thumb-size]
  (let [threshold (calc-usable-track track-width thumb-size)
        x-pos (:x-pos animations)]
    (-> (gesture/gesture-pan)
        (gesture/enabled (not disabled?))
        (gesture/min-distance 0)
        (gesture/on-update (fn [event]
                             (let [x-translation (oops/oget event "translationX")
                                   clamped-x (clamp-value x-translation 0 threshold)
                                   reached-end? (>= clamped-x threshold)]
                               (reanimated/set-shared-value x-pos clamped-x)
                               (cond (and reached-end? (not= @slide-state :complete))
                                     (complete-animation animations slide-state)))))
        (gesture/on-end (fn [event]
                          (let [x-translation (oops/oget event "translationX")
                                reached-end? (>= x-translation threshold)]
                            (when (not reached-end?)
                              (animate-timing x-pos 0 200))))))))

