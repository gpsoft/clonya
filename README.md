# Clonya

This is a not-so-minimal Clojure/ClojureScript application featuring the minimal setup(master branch) plus:

- Ring
- Garden
- Enfocus
- core.async
- and more


# Files

    $ git clone https://github.com/gpsoft/clonya.git
    $ cd clonya
    $ git checkout notsominimal

Directories and files are:

    ├── README.md
    ├── dev
    │   └── user.clj
    ├── project.clj
    ├── resources
    │   ├── jetty-logging.properties
    │   └── public
    │       ├── css
    │       ├── img
    │       │   └── paw.png
    │       ├── index.html
    │       └── js
    └── src
        ├── clj
        │   └── clonya
        │       └── core.clj
        ├── cljc
        │   └── clonya
        │       └── util.cljc
        ├── cljs
        │   └── clonya
        │       └── core.cljs
        └── styles
            └── clonya
                └── styles.clj

# Dev

First start Clojure REPL.

     $ lein repl

Then start Figwheel server by calling the helper function `go`. It also watches `clonya.styles/main` to transpile css file with Garden(see `user.clj` and `styles.clj`).

    user=> (go)

Navigate a browser to `http://localhost:3449/`.

![ss](ss.png)

Finally open `core.cljs` with Vim and start CLJS REPL session.

    :PigFig

# Release

    $ lein cljsbuild once min
    $ lein garden once
    $ lein ring uberjar

An http server(Jetty) is embedded in the uberjar file. So try it.

    $ PORT=8080 java -jar target/clonya-0.1.0-SNAPSHOT-standalone.jar

# Note

We use `lein-ring` plugin only for releasing. During development, we let Figwheel hook the Ring handler and serves it through its own web server(not the Rint&Jetty setup). A caveat is that auto-reloading for CLJ doesn't work.

Also we don't do `lein garden auto` in dev. Instead, the helper function detects the change in `lein-garden` and re-generate the css file. As auto-reloading doesn't work, you need to do `cpp` on the `defstyles` form to update the var.

