(defproject clonya "0.1.0-SNAPSHOT"
  :description "A ClojureScript project"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [org.clojure/core.async "0.4.490"]
                 [enfocus "2.1.1"]
                 [ring/ring-defaults "0.3.2"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-ring "0.12.4"]
            [lein-garden "0.3.0"]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]

  :ring {:handler clonya.core/app
         ;; When you do `lein ring server`, auto-reloading works.
         :open-browser? false
         :auto-reload? true
         :reload-paths ["src/clj" "src/cljc" "resources/"]}

  :garden
  {:builds
   [{:source-paths ["src/styles"]
     :stylesheet clonya.styles/main
     :compiler {:output-to "resources/public/css/style.css"
                :pretty-print? false}}]}

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs" "src/cljc"]
     :compiler {:main clonya.core
                :optimizations :none
                :warnings {:single-segment-namespace false} 
                :asset-path "js/compiled/out"
                :output-to "resources/public/js/compiled/clonya.js"
                :output-dir "resources/public/js/compiled/out"
                :source-map-timestamp true}

     :figwheel {}
     }

    {:id "min"
     :source-paths ["src/cljs" "src/cljc"]
     :compiler {:main clonya.core
                :optimizations :advanced
                :output-to "resources/public/js/compiled/clonya.js"
                :pretty-print false}}]}

  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :css-dirs ["resources/public/css"]
   ;; Figwheel is aware of the Ring handler.
   :ring-handler clonya.core/app}

  :profiles
  {:dev
   {:dependencies [[figwheel-sidecar "0.5.18"]
                   [cider/piggieback "0.4.0"]
                   [nrepl "0.6.0"]
                   [garden "1.3.6"]]
    :source-paths ["dev" "src/styles"]
    :plugins [[cider/cider-nrepl "0.21.0"]]
    :repl-options {:nrepl-middleware
                   [cider.piggieback/wrap-cljs-repl]}
    :clean-targets
    ^{:protect false} [:target-path
                       "resources/public/js/compiled"]}})
