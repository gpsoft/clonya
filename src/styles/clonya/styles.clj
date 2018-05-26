(ns clonya.styles
  (:require [garden.def :refer [defstyles]]
            [garden.color :as color :refer [hsl rgb]]
            [garden.selectors :as sel]
            [garden.units :refer [px]]))

(def ^:private azure (hsl 210 50 40))
(def ^:private sky (hsl 210 60 70))
(def ^:private light-grey (hsl 0 0 90))
(def ^:private orange (hsl 20 60 70))
(def ^:private orange-dense (hsl 20 60 60))

(defstyles
  main
  [:body
   {:color light-grey
    :background-color azure
    :font-family 'sans-serif
    :font-size (px 15)
    :line-height 1.5}]

  [:section
   {:margin [[(px 4) 0]]}]

  [:#top
   {:padding (px 4)} ]

  [:#top:after
   {:display 'block
    :content ""
    :clear 'both}]

  [:#logo
   {:width (px 32)
    :height (px 32)
    :float 'left
    :margin-right (px 8)
    :background "url('../img/paw.png') center center/contain"}]

  [:#clock
   {:font-size (px 24)
    :line-height (px 32)}]

  [:#canvas
   {:overflow 'hidden
    :background-color sky
    :position 'relative}]

  [:.paw
   {:width (px 24)
    :height (px 24)
    :position 'absolute
    :background "url('../img/paw.png') center center/contain"}]

  [:button.btn
   {:font-size (px 24)
    :font-weight 'bold
    :padding [[(px 10) (px 30)]]
    :color light-grey
    :background-color orange
    :border-style 'none
    :cursor 'pointer}]

  [:button.btn:hover
   {:background-color orange-dense}]
  )
