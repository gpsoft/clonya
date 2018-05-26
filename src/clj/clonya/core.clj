(ns clonya.core
  (:require [clonya.util :as util]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            [hiccup.core :refer [html]]
            [ring.middleware.defaults :as ring]
            [ring.util.response :refer [redirect]]))

(def ^:private error404
  (html [:h2 util/title]
        [:p "404: Not Found"]))

(defroutes
  handler
  (GET "/" [] (redirect "/index.html"))
  (not-found #'error404))

(def app
  (ring/wrap-defaults
    #'handler
    (assoc ring/site-defaults
           :static {:resources ["public"]})))

