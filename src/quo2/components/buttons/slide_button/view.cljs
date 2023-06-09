(ns quo2.components.buttons.slide-button.view
  (:require
   [quo2.components.icon :as icon]
   [quo2.foundations.colors :as colors]
   [quo2.components.buttons.slide-button.consts :as consts]
   [quo2.components.buttons.slide-button.style :as style]
   [quo2.components.buttons.slide-button.animations :as anim]
   [reagent.core :as r]
   [react-native.gesture :as gesture]
   [react-native.core :as rn]
   [quo.react :as react]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

(defn- use-slide-state-change
  [slide-state on-state-change]
  (rn/use-effect (fn [] (cond (not (nil? on-state-change))
                              (on-state-change @slide-state)))
                 [@slide-state]))

(defn- use-slide-animations
  [animations slide-state track-width reset-thumb-state on-complete dimensions]
  (rn/use-effect (fn []
                   (let [final-padding  (anim/calc-final-padding @track-width
                                                                 (:thumb dimensions))]
                     (case @slide-state
                       :incomplete ((anim/animate-reset-thumb animations)
                                    (reset-thumb-state))
                       :complete ((anim/animate-shrink-track animations final-padding)
                                  (anim/animate-center-thumb animations)
                                  (anim/animate-round-track-thumb animations)
                                  (anim/animate-scale-track animations)
                                  (on-complete))
                       nil)))
                 [@slide-state @track-width]))

(defn- f-slider
  [{:keys [on-complete
           on-state-change
           track-text
           track-icon
           disabled?
           size]}]
  (let [animations (anim/init-animations)
        dimensions  (case size
                      :small consts/small-dimensions
                      :large consts/large-dimensions
                      consts/large-dimensions)
        track-width (react/state nil)
        slide-state (react/state :rest)
        thumb-icon (if (= :complete @slide-state) track-icon :arrow-right)
        disabled-gestures? (if (= :complete @slide-state) true disabled?)
        reset-thumb-state #(reset! slide-state :rest)
        on-track-layout (fn [evt]
                          (let [width (oops/oget evt "nativeEvent" "layout" "width")]
                            (reset! track-width width)))]

    (use-slide-state-change slide-state
                            on-state-change)
    (use-slide-animations animations
                          slide-state
                          track-width
                          reset-thumb-state
                          on-complete
                          dimensions)

    [gesture/gesture-detector {:gesture (anim/drag-gesture animations
                                                           disabled-gestures?
                                                           track-width
                                                           slide-state (:thumb dimensions))}
     [reanimated/view {:style (style/track-container animations
                                                     (:height dimensions))}
      [reanimated/view {:style (style/track animations disabled?)
                        :on-layout (when-not (some? @track-width) on-track-layout)}
       [reanimated/view {:style (style/track-cover animations
                                                   track-width
                                                   (:thumb dimensions))}
        [rn/view {:style (style/track-cover-text-container track-width)}
         [icon/icon track-icon {:color (:text style/slide-colors)
                                :size  20}]
         [rn/view {:width 4}]
         [rn/text {:style style/track-text} track-text]]]
       [reanimated/view {:style (style/thumb animations
                                             (:thumb dimensions)
                                             track-width)}
        [icon/icon thumb-icon {:color colors/white
                               :size  20}]]]]]))

(defn slide-button
  "Options
  - `on-complete`     Callback called when the sliding is complete
  - `on-state-change` Callback called on slide state change 
                      _args_: [state `:rest`/`:dragging`/`:incomplete`/`:complete`]
  - `disabled?`       Boolean that disables the button
                      (_and gestures_)
  - `size`            `:small`/`:large`
  - `track-text`      Text that is shown on the track
  - `track-icon`      Key of the icon shown on the track
                      (e.g. `:face-id`)
  "
  [props]
  [:f> f-slider props])


