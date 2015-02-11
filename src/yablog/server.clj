(ns yablog.server
  (:require [aleph.http :as http]
            [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [yablog.page :as page]
            [texticlj.core :as tx]))

(defonce all-pages
  (atom (page/read-pages "/Users/dbarlow/src/personal/my-way/example/articles/")))

(defn hiccup-entry [page]
  (with-open [r (io/reader (:pathname page))]
    (page/read-headers r)
    (into [:div [:h1 (:subject page)]]
          (tx/to-hiccup (slurp r)))))

(defn recent-entries [req]
  (hiccup/html
   (into [:body]
         (map hiccup-entry (page/recent-pages 3 @all-pages)))))

(defroutes handler
  (GET "/" [] recent-entries)
  (route/not-found "Page not found"))
