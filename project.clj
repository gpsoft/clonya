(defproject clonya "0.1.0-SNAPSHOT"
  :description "A ClojureScript project"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-simpleton "1.3.0"]]

  :source-paths ["src"]

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src"]
     :compiler {:main clonya.core
                :optimizations :none
                :asset-path "js/compiled/out"
                :output-to "resources/public/js/compiled/clonya.js"
                :output-dir "resources/public/js/compiled/out"
                :source-map-timestamp true}

     :figwheel {}
     }

    {:id "min"
     :source-paths ["src"]
     :compiler {:main clonya.core
                :optimizations :advanced
                :output-to "resources/public/js/compiled/clonya.js"
                :pretty-print false}}]}

  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies [[figwheel-sidecar "0.5.16"]
                   [cider/piggieback "0.3.3"]
                   [org.clojure/tools.nrepl "0.2.13"]]
    :source-paths ["src" "dev"]
    :plugins [[cider/cider-nrepl "0.17.0"]]
    :repl-options {:nrepl-middleware
                   [cider.piggieback/wrap-cljs-repl]}
    :clean-targets
    ^{:protect false} [:target-path
                       "resources/public/js/compiled"]}})
