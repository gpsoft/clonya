(ns clonya.core
  (:require [enfocus.core :as ef]
            [enfocus.events :as ev]
            [enfocus.effects :as effects]
            [cljs.core.async :refer [timeout take! put! chan <! >! close! mult tap]])
  (:require-macros [enfocus.macros :as em]
                   [cljs.core.async.macros :refer [go go-loop]]))

(defonce db (atom {:on true
                   :world {:paws []}}))
(defonce src-chan (chan))
(defonce mult-tic (mult src-chan))

(def ^:private tick 1300)
(def ^:private width 500)
(def ^:private height 300)


;;; CLOCK
(defn- format-dt [dt]
  (-> dt
      .toTimeString
      (clojure.string/split " ")
      first))

(defn- update-clock []
  (let [now (-> (js/Date.) format-dt)]
    (ef/at "#clock" (ef/content now))))

(defn- start-clock []
  (go-loop []
    (<! (timeout 100))
    (update-clock)
    (recur)))

;;; LISTENING TO DOM EVENTS
(defn- listen
  ([sel ev-type] (listen sel ev-type identity))
  ([sel ev-type ev-mapper]
   (let [out (chan)]
     (ef/at [sel]
            (ev/listen ev-type
                       #(put! out (ev-mapper %))))
     out)))

;;; CSS
(defn- str-px
  [val]
  (str val "px"))

(defonce ^:private id-gen
  (let [c (atom 0)]
    (fn [] (swap! c inc))))

(defn- to-radians
  [deg]
  (- (/ Math/PI 2)
     (/ (* Math/PI deg) 180)))

;;; PAW
(defn- paw
  [id x y deg]
  {:id id
   :x x
   :y y
   :deg deg
   :step 50
   :freq 3
   :last-move 0})

(defn- mk-paw
  []
  (let [id (id-gen)
        x (rand-int width)
        y (rand-int height)
        deg (rand-int 360)]
    (paw id x y deg)))

(defn- move-paw
  [{:keys [x y deg step freq last-move] :as paw} t]
  (if (> t (+ last-move freq))
    (let [rad (to-radians deg)
          dx (* step (Math/cos rad))
          dy (* step (Math/sin rad))]
      (assoc paw
             :x (+ x dx)
             :y (- y dy)
             :last-move t))
    paw))

(defn- paw-ele
  [id]
  [:div.paw {:data-paw-id id}])

#_(str "left:" x "px;top:" y  "px;" "transform:rotate(" deg "deg)")

(defn- sync-paw-ele
  [{:keys [id x y deg]} move?]
  (ef/at (str "div.paw[data-paw-id=" id "]")
         (ef/do-> (ef/set-style :transform (str "rotate(" deg "deg)"))
                  (if move?
                    (effects/move x y 100)
                    (ef/set-style :left (str-px x)
                                  :top (str-px y))))))

(defn- reify-paw-ele
  [{:keys [id x y deg] :as paw}]
  (let [ele (paw-ele id)]
    (ef/at [:#canvas]
           (ef/append (ef/html ele)))
    (sync-paw-ele paw false)))

(defn- activate-paw
  [paw mult-tic]
  (let [tic-chan (tap mult-tic (chan))]
    (reify-paw-ele paw)
    (go-loop
      [t (<! tic-chan)
       p paw]
      (when t
        (let [p (move-paw p t)]
          (sync-paw-ele p true)
          (recur (<! tic-chan) p))))))

(defn- start-tick
  []
  (go-loop
      [t 1]
      (<! (timeout tick))
      #_(println "tic" t)
      (when (>! src-chan t)
        (recur (inc t)))))




(defn- update-world
  [ts]
  #_(add-paw)
  #_(swap! db update-in [:world :paws] move-paws)
  #_(println @db))

(defn- draw-world []
  (let [paws (get-in @db [:world :paws])]
    )
  )

(defn- mk-main-loop []
  (let [ch (chan)]
    (go-loop [ts (<! ch)]
      (update-world ts)
      (draw-world)
      (recur (<! ch)))
    ch))

(defn- run
  [ch ts]   ; ts is DOMHighResTimeStamp
  (go (<! (timeout tick))
      (put! ch ts)
      (.requestAnimationFrame js/window (partial run ch))))

(defn- remove-intro []
  (ef/at "#intro" (ef/remove-node)))

(defn- ready-canvas []
  (ef/at "#canvas"
         (ef/set-style :width (str-px width)
                       :height (str-px height))))

(defn- listen-to-main-switch []
  (let [clicks (listen "#mainSw" :click (constantly true))]
    (go-loop []
      (.log js/console (<! clicks))
      (recur))))

(defn- listen-to-paw-btn []
  (let [clicks (listen "#pawBtn" :click)]
    (go-loop []
      (<! clicks)
      (let [paw (mk-paw)]
        (reify-paw-ele paw)
        (activate-paw paw mult-tic)
      (recur)))))

(defn start []
  (remove-intro)
  (start-clock)
  (ready-canvas)
  (listen-to-main-switch)
  (listen-to-paw-btn)
  (start-tick))

(set! (.-onload js/window) start)

(defn new-paw
  [mult-tic]
  (let [x (rand width)
        y (rand height)
        dir (rand 360)
        id (id-gen)
        ele [:div.paw {:data-paw-id id
                       :style (str "left:" x "px;top:" y  "px;"
                                   "transform:rotate(" dir "deg)")}]]
    (ef/at [:#canvas] (ef/append (ef/html ele)))
    (let [tap-tic (chan)]
      (tap mult-tic tap-tic)
      (go-loop []
        (let [t (<! tap-tic)]
          (ef/at (str "div.paw[data-paw-id=" id "]")
                 (effects/move 200 130 300))
          (recur))))))

(comment
  (start)
  (remove-intro)
  (let [ctx (ef/at ["#canvas"] (fn [e] (.clearRect (.getContext e "2d") 0 0 width height)))]
    )
  (let [img (js/Image.)
        _ (set! (.-src img) "img/paw.png")
        ctx (ef/at ["#canvas"] (fn [e] (.drawImage (.getContext e "2d") img 0 0 64 64 100 100 32 32)))]
    )
  (ef/at ["#canvas .paw"] (effects/move 60 :cury 300))
  (id-gen)
  (add-paw)
  (ef/at [:#canvas]
         (ef/append (ef/html [:div.paw {:data-paw-id 3 :style "left:130px;top:80px;transform:rotate(135deg)"}])))
  (go-loop
    [t 1]
    (<! (timeout 1000))
    (println "tic" t)
    (when (>! tic t)
      (recur (inc t))))
  (go-loop
    []
    (let [t (<! tic)]
      (println "tac" t)
      (when-not (nil? t) (recur))))
  (def tic (chan))
  (def mult-tic (mult tic))
  (new-paw mult-tic)
  (put! tic nil)
  (take! tic println)
  (take! (tap mult-tic (chan)) println)
  (close! tic)
  (ef/at "div.paw[data-paw-id=3]"
         (effects/move 200 130 300))

  (let [paw (mk-paw)]
    #_(reify-paw-ele paw)
    (println paw)
    (activate-paw paw mult-tic)
    #_(move-paw-ele paw))
  )
