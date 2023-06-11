(ns quo2.components.buttons.slide-button.animations
  (:require
   [quo2.components.buttons.slide-button.consts :as consts]
   [react-native.gesture :as gesture]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

;; Utils 
(defn- clamp-value [value min-value max-value]
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

(defn- track-interpolation-inputs
  [in-vectors track-width]
  (map #(* track-width %) in-vectors))

;; Interpolations
(defn- track-cover-interpolation
  [track-width thumb-size]
  {:in [0 1]
   :out [(/ thumb-size 2) track-width]})

(defn- thumb-border-radius-interpolation
  [thumb-size]
  {:in [0 0.75 1]
   :out [consts/thumb-border-radius
         consts/thumb-border-radius
         (/ thumb-size 2)]})

(defn- thumb-drop-position-interpolation
  [thumb-size]
  (let [drop-lag #(- (* thumb-size %))]
    {:in [0 0.5 0.75 1]
     :out [0 (drop-lag 0.4) (drop-lag 0.9) 0]}))

(def ^:private thumb-drop-scale-interpolation
  {:in [0 0.5 0.75 1]
   :out [0 0.6 0.7 1]})

(def ^:private thumb-drop-z-index-interpolation
  {:in [0 0.75 1]
   :out [0 1 1]})

(def ^:private thumb-drop-color
  (let [main-col (:thumb consts/slide-colors)
        dark-col "#0a2bdb"]
    {:in [0 0.75 1]
     :out [dark-col dark-col main-col]}))

(defn interpolate-track
  "Interpolate the position in the track
  `x-pos`          Track animated value
  `track-width`    Width of the track
  `thumb-size`     Size of the thumb
  `interpolation` `:thumb-border-radius`/`:thumb-drop-position`/`:thumb-drop-scale`/`:thumb-drop-z-index`/`:thumb-drop-color`/`:track-cover-interpolation`"
  ([x-pos track-width thumb-size interpolation]
   (let [interpolations {:track-cover (track-cover-interpolation track-width thumb-size)
                         :thumb-border-radius (thumb-border-radius-interpolation thumb-size)
                         :thumb-drop-position (thumb-drop-position-interpolation thumb-size)
                         :thumb-drop-scale    thumb-drop-scale-interpolation
                         :thumb-drop-z-index  thumb-drop-z-index-interpolation}
         color-interpolations {:thumb-drop-color thumb-drop-color}
         color-interpolation? (contains? color-interpolations interpolation)
         interpolation-values (interpolation (if color-interpolation?
                                               color-interpolations
                                               interpolations))
         output (:out interpolation-values)
         input (-> (:in interpolation-values)
                   (track-interpolation-inputs track-width))]
     (if (nil? interpolation-values)
       x-pos
       (if color-interpolation?
         (reanimated/interpolate-color x-pos
                                       input
                                       output)
         (reanimated/interpolate x-pos
                                 input
                                 output
                                 extrapolation))))))

;; Animation helpers

;; TODO remove
(defn- animate-spring
  [value to-value]
  (reanimated/animate-shared-value-with-spring value
                                               to-value
                                               {:mass      1
                                                :damping   4
                                                :stiffness 100}))

(defn- animate-timing
  [value to-position duration]
  (reanimated/animate-shared-value-with-timing value
                                               to-position
                                               duration
                                               :linear))

;; TODO remove
(defn- animate-sequence [anim & seq-animations]
  (reanimated/set-shared-value anim
                               (apply reanimated/with-sequence
                                      seq-animations)))

;; Animations
(defn init-animations []
  {:x-pos (reanimated/use-shared-value 0)
   :thumb-drop-width (reanimated/use-shared-value 0)
   :track-container-padding (reanimated/use-shared-value 0)})

(defn complete-animation
  [{:keys []} slide-state]
  (reanimated/run-on-js
   (js/setTimeout (fn [] (reset! slide-state :complete)) 300)))

(defn reset-track-position
  [{:keys [x-pos]}]
  (animate-timing x-pos 0 200))

;; Gestures
(defn drag-gesture
  [animations
   disabled?
   track-width
   slide-state]
  (let [x-pos (:x-pos animations)]
    (-> (gesture/gesture-pan)
        (gesture/enabled (not disabled?))
        (gesture/min-distance 0)
        (gesture/on-update (fn [event]
                             (let [x-translation (oops/oget event "translationX")
                                   clamped-x (clamp-value x-translation 0 track-width)
                                   reached-end? (>= clamped-x track-width)]
                               (reanimated/set-shared-value x-pos clamped-x)
                               (cond (and reached-end? (not= @slide-state :complete))
                                     (complete-animation animations slide-state)))))
        (gesture/on-end (fn [event]
                          (let [x-translation (oops/oget event "translationX")
                                reached-end? (>= x-translation track-width)]
                            (when (not reached-end?)
                              (reset-track-position animations))))))))

