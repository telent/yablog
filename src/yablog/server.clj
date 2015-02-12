(ns yablog.server
  (:require [aleph.http :as http]
            [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [hiccup.page :as hpage]
            [clj-time.format :as ftime]
            [yablog.page :as page]
            [yablog.conf :as conf]
            [yablog.time :as ytime]
            [clojure.walk :as w]
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

(defn recent-posts-box [req]
  [:div {:class "sidebox"}
   [:h2 "Recent posts"]
   [:p "Things I can remember"]
   [:ul (map (fn [n] [:li ,n]) (range 0 4))]])

(defn by-month-box [req]
  [:div {:class "sidebox"}
   [:h2 "Archived posts"]
   [:p "The tenured generation"]
   [:ul (map (fn [n]
               (vector :li n (map ytime/month-name (range 1 12))))
             (range 2015 2001 -1))]])

(defn stylify [hiccuper]
  (fn [req]
    (let [hic (hiccuper req)
          conf (:conf req)]
      (hpage/html5
       [:head
        [:link {:href (conf/stylesheet conf)
                :rel "stylesheet"}]
        [:title (:title conf)]]
       (w/postwalk-replace
        (conf/replacements conf)
        [:body
         [:header
          [:a {:href "/"}
           [:div {:class "title"} (:title conf)]]]
         hic
         [:aside
          [:bio]
          (recent-posts-box req)
          (by-month-box req)]
         [:footer]
         ])))))

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
