(ns user
  (:require
    clonya.styles
    [figwheel-sidecar.repl-api :as fig]
    [garden.core :refer [css]]
    [clojure.core.async :refer [go-loop timeout <!]]))

(defn startfig [] (fig/start-figwheel!))
(defn stopfig [] (fig/stop-figwheel!))
(defn cljsrepl [] (fig/cljs-repl))

(defn- project-m
  "The project map"
  []
  (->> "project.clj"
       slurp
       clojure.edn/read-string
       (drop 3)
       (partition 2)
       (map vec)
       (into {})))

(defn start-garden
  ([] (start-garden 0))
  ([n]
   (let [prj (project-m)
         build (get-in prj [:garden :builds n])
         style-sym (:stylesheet build)
         opts (:compiler build)
         style (atom (eval style-sym))]
     ;; compile once.
     (css opts style)
     ;; and keep watching.
     (go-loop []
       (<! (timeout 500))
       (let [s (eval style-sym)]
         (when-not (= s @style)
           (css opts s)
           (reset! style s)))
       (recur))
     nil)))

(defn go
  []
  (startfig)
  (start-garden))
