(ns quo2.components.buttons.slide-button.view
  (:require
   [quo2.components.icon :refer [icon]]
   [quo2.foundations.colors :as colors]
   [quo2.components.buttons.slide-button.consts
    :refer [small-dimensions large-dimensions timing-duration]]
   [quo2.components.buttons.slide-button.style
    :refer [thumb-style
            slide-colors
            track-style
            track-cover-style
            track-container-style
            track-text-style
            track-cover-text-container-style]]
   [quo2.components.buttons.slide-button.animations
    :refer [init-animations clamp-track drag-gesture animate-timing animate-complete]]
   [react-native.gesture :as gesture]
   [react-native.core :as rn :refer [use-effect]]
   [quo.react :as react]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

(defn slider
  [{:keys [on-complete
           on-state-change
           track-text
           track-icon
           disabled?
           size]}]
  (let [animations (init-animations)
        dimensions  (case size
                      :small small-dimensions
                      :large large-dimensions
                      large-dimensions)
        track-width (react/state nil)
        thumb-state (react/state :rest)
        thumb-icon (if (= :complete @thumb-state) track-icon :arrow-right)
        disabled-gestures? (if (= :complete @thumb-state) true disabled?)
        reset-thumb-state #(reset! thumb-state :rest)
        on-track-layout (fn [evt]
                          (let [width (oops/oget evt "nativeEvent" "layout" "width")]
                            (reset! track-width width)))]

    (use-effect
     (fn [] (cond
              (not (nil? on-state-change))
              (on-state-change @thumb-state)))
     [@thumb-state])

    (use-effect
     (fn [] (println (str "thumb-state-changed: " @thumb-state))) [@thumb-state])

    (use-effect
     (fn []
       (let [x (animations :x-pos)]
         (case @thumb-state
           :complete ((animate-complete animations @track-width (:thumb dimensions))
                      ;;(reset! thumb-state :complete-shrink)
                      ;;TODO remove comment
                      (comment on-complete))
           :incomplete (doall
                        [(animate-timing x 0 timing-duration)
                         (reset-thumb-state)])
           nil)))
     [@thumb-state @track-width])

    [gesture/gesture-detector {:gesture (drag-gesture animations disabled-gestures? track-width thumb-state (:thumb dimensions))}
     [reanimated/view {:style (track-container-style animations (:height dimensions))}
      [reanimated/view {:style (track-style animations disabled?)
                        :on-layout (when-not
                                    (some? @track-width)
                                     on-track-layout)}
       [reanimated/view {:style (track-cover-style animations track-width (:thumb dimensions))}
        [rn/view {:style (track-cover-text-container-style  track-width)}
         [icon track-icon {:color (:text slide-colors)
                           :size  20}]
         [rn/view {:width 4}]
         [rn/text {:style track-text-style} track-text]]]
       [reanimated/view {:style (thumb-style animations (:thumb dimensions) track-width)}
        [icon thumb-icon {:color colors/white
                          :size  20}]]]]]))

;; TODO 
;; - allow disabling the button through props
;; - figure out the themes and colors
;; - add documentation
;; 
;; PROPS:
;; - disabled
;; - on-complete (DONE)
;; - track-icon (DONE)
;; - track-text (DONE)
;; - size (DONE)

(defn slide-button [{:keys [on-complete on-state-change track-text track-icon size disabled?]} as props]
  [:f> slider {:on-complete on-complete
               :on-state-change on-state-change
               :size size
               :disabled? disabled?
               :track-text track-text
               :track-icon track-icon}])


