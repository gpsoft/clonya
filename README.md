# Clonya

This is a minimal ClojureScript application featuring:

- Leiningen
- Figwheel-sidecar
- Vim
- vim-fireplace

Make sure to use the latest version of vim-fireplace.

# Files

    $ git clone https://github.com/gpsoft/clonya.git
    $ cd clonya

Directories and files are:

    ├── README.md
    ├── dev
    │   └── user.clj
    ├── project.clj
    ├── resources
    │   └── public
    │       ├── css
    │       │   └── style.css
    │       └── index.html
    └── src
        └── clonya
            └── core.cljs

# Dev

First start Clojure REPL. It's a traditional REPL session with nREPL server and REPL-y(the client).

     $ lein repl

Then start Figwheel server by calling a helper function defined in `user.clj`.

    user=> (startfig)

Figwheel takes care of lots of things:

- compliles your CLJS sources once
- serves HTML pages at port 3449
- watches source files to recompile and reload when you changed one
- also CSS files will be reloaded automatically (but not HTML files)

So navigate a browser(Chrome is recommended) to `http://localhost:3449/` to open `index.html`, and confirm the reloading by editing `style.css`.

Finally open `core.cljs` with Vim and let vim-fireplace launch CLJS REPL and connect to it. This is a CLJS bREPL(browser REPL) supported by Figwheel.

    :Piggieback (figwheel-sidecar.repl-api/repl-env)

Now you can do `cpp`, `K`, `:Eval (js/alert "hey!")`, `]<C-D>`, `]d`, etc...

![ss](ss.png)

What's happening here?

- I hit `cpp` on the Vim with cursor inside of `(js/alert "hey!")`
- vim-fireplace sends a request for evaluating the form to nREPL server
- an nREPL middleware, Piggeback, hooks the request
- knowing the form is a CLJS code, Piggieback redirects the request to CLJS bREPL
- bREPL compiles(transpiles) the code into JavaScript and tries to run it on a JavaScript runtime
- in this case, the runtime is provided by Figwheel
- so Figwheel sends the code to its agent on the browser through a websocket connection
- the agent runs the code and the alert pops up

## Tips

A custom vim command would help you. Add below line to your `.vimrc`.

    command! PigFig execute('Piggieback (figwheel-sidecar.repl-api/repl-env)')

When something went wrong, do `:Piggieback!` to disconnect and `:PigFig` to connect again.

Be carefull not to do anything before connect to CLJS bREPL, otherwise vim-fireplace falls back to Nashorn REPL, which has no DOM, no console.

## Caveats and notes

- `K` doesn't seem to work for some symbols, such as `def`, `ns`, or `if`
- when you defined Vars on the fly, source code lookup(line `]<C-D>` and `]d`) for them won't work until you reconnect(`:Piggieback!` and `:PigFig`)

Apropos the `K` issue, I'm guessing `K` on CLJS sources doesn't work for special forms. Did you know `cljs.core/let` is not a special form, by the way? `cljs.core/let*` is. Also `ns` is a special form in CLJS.

Piggieback has changed its group-id and namespace as of version 0.2.3. It's now `cider/piggieback`. We need Figwheel 0.5.16 and vim-fireplace 7b1246e(2018-04-22) or later to work with it.

# Release

Compile all into one JS file(`resources/public/js/compiled/clonya.js`).

    $ lein cljsbuild once min

Try it by a web server(lein-simpleton) to see if all is good.

    $ lein simpleton 8080 file :from resources/public/

# Annotated project.clj

The project is base on the `figwheel` Leiningen template.

    (defproject clonya "0.1.0-SNAPSHOT"
      :description "A ClojureScript project"

      :dependencies [[org.clojure/clojure "1.9.0"]
                     [org.clojure/clojurescript "1.10.238"]]

      ;; Plugins for Leiningen.
      ;; No figwheel here becuase we don't do `lein figwheel`.
      ;; We do `lein repl` and start figwheel manually instead.
      :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
                [lein-simpleton "1.3.0"]]

      :source-paths ["src"]

      ;; Options for lein-cljsbuild.
      ;; Remember this section is only for CLJS(not for CLJ).
      :cljsbuild
      {:builds
       [{:id "dev"
         :source-paths ["src"]
         :compiler {:main clonya.core
                    :optimizations :none
                    ;; asset-path controls url path from where
                    ;; JS files are loaded.
                    ;; It's impotant in non-optimized mode build
                    ;; because we have many JS files
                    ;; not just clonya.js.
                    :asset-path "js/compiled/out"
                    :output-to "resources/public/js/compiled/clonya.js"
                    :output-dir "resources/public/js/compiled/out"
                    :source-map-timestamp true}

         ;; Compiler hooks for Figwheel.
         ;; You need this even if it's empty
         ;; so that lein-cljsbuild can inject the Figwheel agent
         ;; into the output JS code.
         :figwheel {}
         }

        {:id "min"
         :source-paths ["src"]
         :compiler {:main clonya.core
                    :optimizations :advanced
                    :output-to "resources/public/js/compiled/clonya.js"
                    :pretty-print false}}]}

      ;; Options for Figwheel.
      :figwheel
      {:http-server-root "public"
       :server-port 3449
       :css-dirs ["resources/public/css"]}

      :profiles
      {:dev
       ;; Figwheel is a Leiningen plugin, but its core
       ;; functionalities are extracted from the plugin
       ;; to a generic library--Figwheel-sidecar.
       {:dependencies [[figwheel-sidecar "0.5.16"]
                       [cider/piggieback "0.3.3"]
                       [org.clojure/tools.nrepl "0.2.13"]]

        ;; Some helper fn's for development are defined
        ;; in the `user` namespace.
        ;; Those code are under `dev` directory.
        :source-paths ["src" "dev"]

        ;; Leiningen plugins and nREPL middleware for vim-fireplace.
        ;; Vim-fireplace can do most of the job by itself
        ;; while it can be even better when it makes use of
        ;; another nREPL middleware, cider-nrepl.
        :plugins [[cider/cider-nrepl "0.17.0"]]
        :repl-options {:nrepl-middleware
                       [cider.piggieback/wrap-cljs-repl]}

        :clean-targets
        ^{:protect false} [:target-path
                           "resources/public/js/compiled"]}})

