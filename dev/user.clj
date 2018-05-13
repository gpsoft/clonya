(ns user
  (:require
    [figwheel-sidecar.repl-api :as fig]))

(defn startfig [] (fig/start-figwheel!))
(defn stopfig [] (fig/stop-figwheel!))
(defn cljsrepl [] (fig/cljs-repl))
