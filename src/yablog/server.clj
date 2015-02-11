(ns yablog.server
  (:require [aleph.http :as http]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [texticlj.core :as tx]))

(defn render-entry [filename]
  (hiccup/html (tx/to-hiccup (slurp filename))))

(defroutes handler
  (GET "/" [] (fn [& stuff ] (render-entry "/Users/dbarlow/src/personal/my-way/example/articles/thin-prefork.textile")))
  (route/not-found "Page not found"))


(defonce started (http/start-server #'handler {:port 4567}))
