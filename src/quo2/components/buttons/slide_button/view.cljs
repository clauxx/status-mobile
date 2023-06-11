(ns quo2.components.buttons.slide-button.view
  (:require
   [quo2.components.icon :as icon]
   [quo2.foundations.colors :as colors]
   [quo2.components.buttons.slide-button.consts :as consts]
   [quo2.components.buttons.slide-button.style :as style]
   [quo2.components.buttons.slide-button.animations :as anim]
   [react-native.gesture :as gesture]
   [react-native.core :as rn]
   [quo.react :as react]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

(defn- f-slider
  [{:keys [on-complete
           track-text
           track-icon
           disabled?
           size]}]
  (let [animations (anim/init-animations)
        track-width (react/state nil)
        on-track-layout (fn [evt]
                          (let [width (oops/oget evt "nativeEvent" "layout" "width")]
                            (reset! track-width width)))
        dimensions  (let [default-dimensions (case size
                                               :small consts/small-dimensions
                                               :large consts/large-dimensions
                                               consts/large-dimensions)]
                      (merge default-dimensions
                             {:track-width (anim/calc-usable-track
                                            track-width
                                            (:thumb default-dimensions))}))
        slide-state (react/state :rest)]

    (rn/use-effect (fn []
                     (case @slide-state
                       :complete (on-complete)
                       nil))
                   [@slide-state])

    [gesture/gesture-detector {:gesture (anim/drag-gesture animations
                                                           disabled?
                                                           (:track-width dimensions)
                                                           slide-state)}
     [reanimated/view {:style (style/track-container (:height dimensions))}
      [reanimated/view {:style (style/track disabled?)
                        :on-layout (when-not (some? @track-width)
                                     on-track-layout)}
       [reanimated/view {:style (style/track-success animations
                                                     (:track-width dimensions)
                                                     (:thumb dimensions))}

        [icon/icon :check {:color (:text consts/slide-colors)
                           :size  20}]]
       [reanimated/view {:style (style/track-cover animations
                                                   (:track-width dimensions)
                                                   (:thumb dimensions))}
        [rn/view {:style (style/track-cover-text-container @track-width)}
         [icon/icon track-icon {:color (:text consts/slide-colors)
                                :size  20}]
         [rn/view {:width 4}]
         [rn/text {:style style/track-text} track-text]]]
       [reanimated/view {:style (style/thumb-container animations (:track-width dimensions) (:thumb dimensions))}
        [reanimated/view {:style (style/thumb-drop animations
                                                   (:thumb dimensions)
                                                   (:track-width dimensions))}
         [icon/icon track-icon {:color colors/white
                                :size  20}]]
        [reanimated/view {:style (style/thumb animations
                                              (:thumb dimensions)
                                              (:track-width dimensions))}

         [reanimated/view {:style (style/thumb-icon-container animations
                                                              (:thumb dimensions)
                                                              (:track-width dimensions))}
          [icon/icon :arrow-right {:color colors/white
                                   :size  20}]]]]]]]))

(defn slide-button
  "Options
  - `on-complete`     Callback called when the sliding is complete
  - `disabled?`       Boolean that disables the button
                      (_and gestures_)
  - `size`            `:small`/`:large`
  - `track-text`      Text that is shown on the track
  - `track-icon`      Key of the icon shown on the track
                      (e.g. `:face-id`)
  "
  [props]
  [:f> f-slider props])


