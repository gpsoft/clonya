(ns clonya.core
  (:require [clonya.util :as util]
            [enfocus.core :as ef]
            [enfocus.events :refer [listen]]
            [enfocus.effects :refer [move]]
            [cljs.core.async :refer [timeout put! chan <! >! close! mult tap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


;; Listening to dom events
(defn- listen-ev
  ([sel ev-type] (listen-ev sel ev-type identity))
  ([sel ev-type ev-mapper]
   (let [out (chan)]
     (ef/at sel
            (listen ev-type
                    #(put! out (ev-mapper %))))
     out)))

;; Vars
(defonce ^:private tick-src-chan (chan))
(defonce ^:private mult-tick (mult tick-src-chan))
(defonce ^:private paw-btn-chan (listen-ev "#pawBtn" :click))
(def ^:private tick 300)
(def ^:private width 500)
(def ^:private height 300)


;;; CLOCK
(defn- format-dt [dt]
  (-> dt
      .toTimeString
      (clojure.string/split " ")
      first))

(defn- update-clock []
  (let [now (format-dt (js/Date.))]
    (ef/at "#clock" (ef/content now))))

(defn- clock-loop []
  (go-loop []
    (<! (timeout 100))
    (update-clock)
    (recur)))


;;; WALKING PAWS

;; Helper
(defonce ^:private id-gen
  (let [c (atom 0)]
    (fn [] (swap! c inc))))

(defn- str-px
  [val]
  (str val "px"))

(defn- deg->rad
  ;; deg: 0 for north, clockwise
  ;; rad: 0 for east, counter-clockwise
  [deg]
  (- (/ Math/PI 2)
     (/ (* Math/PI deg) 180)))

(defn- str-rotate
  [deg]
  (str "rotate(" deg "deg)"))

(defn- in-bounds?
  [x y]
  (and (< 0 x width)
       (< 0 y height)))


;; Paw
(defn- paw
  [id x y deg freq]
  {:id id
   :x x
   :y y
   :deg deg
   :step 15
   :freq freq
   :last-move 0})

(defn- mk-paw
  []
  (let [id (id-gen)
        x (rand-int width)
        y (rand-int height)
        deg (rand-int 360)
        freq (+ 2 (rand-int 10))]
    (paw id x y deg freq)))

(defn- turn-paw
  [deg]
  (mod (+ deg 90 (rand-int 180)) 360))

(defn- move-paw
  [{:keys [x y deg step freq last-move] :as paw} ts]
  (if (> ts (+ last-move freq))   ; time to move?
    (let [rad (deg->rad deg)
          dx (* step (Math/cos rad))
          dy (* step (Math/sin rad))
          new-x (+ x dx)
          new-y (- y dy)]
      (if (in-bounds? new-x new-y)
        (assoc paw :x new-x :y new-y :last-move ts)
        (assoc paw :deg (turn-paw deg))))
    paw))


;; Element for paw
(defn- paw-ele
  [id]
  [:div.paw {:data-paw-id id}])

(defn- paw-sel
  [id]
  (str "div.paw[data-paw-id=" id "]"))

(defn- sync-paw-ele
  [{:keys [id x y deg]} animation?]
  (ef/at (paw-sel id)
         (ef/do-> (ef/set-style :transform (str-rotate deg))
                  (if animation?
                    (move x y 100)
                    (ef/set-style :left (str-px x)
                                  :top (str-px y))))))

(defn- reify-paw-ele
  [{:keys [id x y deg] :as paw}]
  (let [ele (paw-ele id)]
    (ef/at "#canvas"
           (ef/append (ef/html ele)))
    (sync-paw-ele paw false)))



;;; MAIN
(defn- remove-intro []
  (ef/at "#intro" (ef/remove-node)))

(defn- ready-canvas []
  (ef/at "#canvas"
         (ef/set-style :width (str-px width)
                       :height (str-px height))))

(defn- paw-loop
  [paw mult-tick]
  (let [tick-chan (tap mult-tick (chan))]
    (go-loop
      [ts (<! tick-chan)
       p paw]
      (when ts
        (let [p (move-paw p ts)]
          (sync-paw-ele p true)
          (recur (<! tick-chan) p))))))

(defn- listen-to-paw-btn []
  (go-loop []
    (when (<! paw-btn-chan)
      (let [paw (mk-paw)]
        (reify-paw-ele paw)
        (paw-loop paw mult-tick))
      (recur))))

(defn- tick-loop
  []
  (go-loop
    [ts 1]
    (<! (timeout tick))
    #_(println "tic" ts)
    (when (>! tick-src-chan ts)
      (recur (inc ts)))))

(defn start []
  (remove-intro)
  (clock-loop)
  (ready-canvas)
  (listen-to-paw-btn)
  (ef/at "title" (ef/content util/title))
  (tick-loop))

(set! (.-onload js/window) start)

(comment
  (close! tick-src-chan)
  )
