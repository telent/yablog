(ns yablog.server
  (:require [aleph.http :as http]
            [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [hiccup.page :as hpage]
            [clj-time.core :as time]
            [clj-time.format :as ftime]
            [yablog.page :as page]
            [yablog.conf :as conf]
            [texticlj.core :as tx]))

(def date-formatter (ftime/formatters :rfc822))

(defn format-time [t]
  (ftime/unparse date-formatter t))

(defn hiccup-entry [page]
  (with-open [r (io/reader (:pathname page))]
    (page/read-headers r)
    (into [:div {:class "entry"}
           [:h1 {:class "title"} (or (:subject page) (:title page))]
           [:h2 {:class "date"} (format-time (:date page))]]
          (tx/to-hiccup (slurp r)))))

(defn stylify [hiccuper]
  (fn [req]
    (let [hic (hiccuper req)]
      (hpage/html5
       [:head
        [:link {:href (conf/stylesheet (:conf req))
                :rel "stylesheet"}]
        [:title "diary at Telent Netowrks"]]
       [:body
        [:header
         [:a {:href "/"}
          [:div {:class "title"} "diary at Telent Netowrks"]]]
        hic
        [:aside
         [:img {:src "/static/images/me.jpg"}]
         [:p "There are many Daniel Barlows on the internet, but this one's me.  This blog contains geeky stuff about what I do, and in the older entries, what I used to do.  Clojure, Ruby, Linux, Android, Common Lisp, and thoughts about software development and matters arising. "]
         [:p "I have other non-tech interests too, but I don't write about them much here.  Try me on "
          [:a {:href "https://twitter.com/telent"} "Twitter"]
          " for very short rants about a variety of stuff"]
         ;; XXX add links to recent posts and by-month archive
         ]]))))

(defn recent-entries [req]
  (into [:article]
        (map hiccup-entry (page/recent-pages 5 (:pages req)))))

(defn entries-for-month [req]
  (let [p (:route-params req)]
    ;; XXX would be neat if it included "older" and "newer" links
    (into [:article]
          (map hiccup-entry
               (page/pages-in-month (Integer. (:year p))
                                    (Integer. (:month p))
                                    (:pages req))))))

(defroutes handler
  (GET "/" [] (stylify recent-entries))
  (GET "/static/*" request
       (let [f (-> request :route-params :*)]
         (io/file (conf/static-folder (:conf request)) f)))
  (GET "/:year{[0-9]+}/:month{[0-9]+}" [year month]
       (stylify entries-for-month))
  (route/not-found "Page not found"))
