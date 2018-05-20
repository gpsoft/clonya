(ns clonya.core
  (:require [enfocus.core :as ef]
            [enfocus.events :as ev]
            [enfocus.effects :as effects]
            [cljs.core.async :refer [timeout put! chan <! mult tap]])
  (:require-macros [enfocus.macros :as em]
                   [cljs.core.async.macros :refer [go go-loop]]))

(defonce db (atom {:on true
                   :world {:paws []}}))

(def ^:private ^:dynamic *tick* 300)
(def ^:private width 500)
(def ^:private height 300)

(defn- dt->time-str [dt]
  (-> dt
      .toTimeString
      (clojure.string/split " ")
      first))

(defn- update-clock []
  (let [now (-> (js/Date.) dt->time-str)]
    (ef/at ["#clock"] (ef/content now))))

(defn- listen
  ([sel ev-type] (listen sel ev-type identity))
  ([sel ev-type ev-mapper]
   (let [out (chan)]
     (ef/at [sel]
            (ev/listen ev-type
                       #(put! out (ev-mapper %))))
     out)))

(defn- remove-intro []
  (ef/at "#intro" (ef/remove-node)))

(defn- start-clock []
  (go-loop []
    (<! (timeout 500))
    (update-clock)
    (recur)))

(defn- ready-canvas []
  (ef/at ["#canvas"] (ef/set-attr :width width :height height))
  )

(defn- listen-to-main-switch []
  (let [clicks (listen "#mainSw" :click (constantly true))]
    (go-loop []
      (.log js/console (<! clicks))
      (recur))))

(defonce ^:private id-gen
     (let [c (atom 0)]
       (fn [] (swap! c inc))))

(defn- to-radians
  [deg]
  (- (/ Math/PI 2) (/ (* Math/PI deg) 180)))

(defn- paw
  [id x y deg]
  {:id id
   :x x
   :y y
   :deg deg
   :last-move 0})

(defn- mk-paw
  []
  (let [id (id-gen)
        x (rand-int width)
        y (rand-int height)
        deg (rand-int 360)]
    (paw id x y deg)))

(defn- move-paw
  [{:keys [x y deg] :as paw}]
  (let [rad (to-radians deg)
        dx (* 50 (Math/cos rad))
        dy (* 50 (Math/sin rad))]
    (assoc paw :x (+ x dx) :y (+ y dy))))

(defn- paw-ele
  [id x y deg]
  [:div.paw {:data-paw-id id
             :style (str "left:" x "px;top:" y  "px;"
                         "transform:rotate(" deg "deg)")}])

(defn- reify-paw-ele
  [{:keys [id x y deg] :as paw}]
  (let [ele (paw-ele id x y deg)]
    (ef/at [:#canvas] (ef/append (ef/html ele)))))

(defn- move-paw-ele
  [paw]
  (let [{:keys [id x y] :as next-paw} (move-paw paw)]
    (ef/at (str "div.paw[data-paw-id=" id "]")
           (effects/move x y 300))
    next-paw))

#_(defn- add-paw []
  (let [paw (mk-paw (id-gen))]
    (swap! db update-in [:world :paws] conj paw)))

#_(defn- move-paw [paw]
  (let [x (inc (:x paw))
        x (if (> x width) (- x width) x)]
    (assoc paw :x x)))

#_(defn- move-paws [paws]
  (mapv move-paw paws))

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
  (go (<! (timeout *tick*))
      (put! ch ts)
      (.requestAnimationFrame js/window (partial run ch))))

(defn start []
  (remove-intro)
  (start-clock)
  (ready-canvas)
  #_(add-paw)
  (listen-to-main-switch)
  (let [tick-ch (mk-main-loop)]
    (run tick-ch 0)))

(set! (.-onload js/window) start)

(defn new-paw
  [mul-tic]
  (let [x (rand width)
        y (rand height)
        dir (rand 360)
        id (id-gen)
        ele [:div.paw {:data-paw-id id
                       :style (str "left:" x "px;top:" y  "px;"
                                   "transform:rotate(" dir "deg)")}]]
    (ef/at [:#canvas] (ef/append (ef/html ele)))
    (let [tap-tic (chan)]
      (tap mul-tic tap-tic)
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
  (def tic (chan))
  (def mul-tic (mult tic))
  (new-paw mul-tic)
  (put! tic 1)
  (ef/at "div.paw[data-paw-id=3]"
         (effects/move 200 130 300))
  (let [paw (mk-paw)]
    (reify-paw-ele paw)
    (println paw)
    (move-paw-ele paw))
  )
