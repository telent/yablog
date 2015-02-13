(ns yablog.server
  (:require [aleph.http :as http]
            [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            hiccup.util
            [hiccup.page :as hpage]
            [clj-time.core :as time]
            [clj-time.format :as ftime]
            [yablog.page :as page]
            [yablog.conf :as conf]
            [yablog.time :as ytime]
            [yablog.hiccup :as hic]
            [clojure.walk :as w]
            [clojure.xml :as xml]))

(defn recent-posts-box [req]
  (let [pages (page/recent-pages 10 (:pages req))]
    [:div {:class "sidebox"}
     [:h2 "Recent posts"]
     [:p "Things I can remember"]
     [:ul (map (fn [p] [:li [:a {:href (page/url p)} (page/title p)]])
               pages)]]))

(defn by-month-box [req]
  (let [ymlink (fn [y m] [:a {:href (str "/" y "/" m)} (ytime/month-name m)])
        pages (:pages req)
        pages? (fn [y m] (seq (page/pages-in-month y m pages)))]
    [:div {:class "sidebox"}
     [:h2 "Archived posts"]
     [:p "The tenured generation"]
     [:ul (map (fn [y]
                 (vector :li [:b y] " "
                         (interpose
                          " "
                          (map (partial ymlink y)
                               (filter (partial pages? y) (range 1 12))))))
               (range 2015 2001 -1))]]))

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
        (map hic/hiccup-entry (page/recent-pages 5 (:pages req)))))

(defn entries-for-month [req]
  (let [p (:route-params req)]
    ;; XXX would be neat if it included "older" and "newer" links
    (into [:article]
          (map hic/hiccup-entry
               (page/pages-in-month (Integer. (:year p))
                                    (Integer. (:month p))
                                    (:pages req))))))

(defn entry-by-y-m-slug [y m slug request]
  (let [p (page/find-page (Integer. y) (Integer. m)
                          slug (:pages request))]

(defn rss-item [post]
  {:tag :item,
   :attrs nil,
   :content
   [{:tag :title,
     :attrs nil,
     :content [(page/title post)]}
    {:tag :link,
     :attrs nil,
     :content
     [(page/url post)]}                 ;XXX absoluteize this?
    {:tag :description,
     :attrs nil,
     :content
     [(hiccup.util/escape-html (hiccup/html (hiccup-entry-body post)))]}
    {:tag :author, :attrs nil, :content ["Daniel Barlow"]}
    {:tag :pubDate,
     :attrs nil,
     :content [(format-time (:date post))]}
    {:tag :guid,
     :attrs nil,
     :content [(page/url post)]
     }
    {:tag :dc:date,
     :attrs nil,
     :content [(ftime/unparse (ftime/formatters :date-time-no-ms)
                              (:date post))]}
    ]})

(defn rss-posts [posts]
  {:tag :rss,
   :attrs
   {:version "2.0",
    :xmlns:content "http://purl.org/rss/1.0/modules/content/",
    :xmlns:dc "http://purl.org/dc/elements/1.1/",
    :xmlns:trackback
    "http://madskills.com/public/xml/rss/module/trackback/",
    :xmlns:itunes "http://www.itunes.com/dtds/podcast-1.0.dtd"},
   :content
   [{:tag :channel,
     :attrs nil,
     :content
     (into
      [{:tag :title, :attrs nil, :content ["diary at Telent Netowrks"]}
       {:tag :link, :attrs nil, :content ["http://ww.telent.net/"]}
       {:tag :description,
        :attrs nil,
        :content ["Geeky stuff about what I do.  By Daniel Barlow"]}]
      (map rss-item posts))
     }]})
    [:article (hic/hiccup-entry p)]))

(defn handle-rss [req]
  (let [recent (page/recent-pages 10 (:pages req))
        xml (rss-posts recent)
        body (with-out-str (xml/emit xml))]
    #_ (clojure.pprint/pprint xml)
    {:status 200
     :headers {"Content-Type" "application/rss+xml; charset=utf-8"}
     :body body}))

(defroutes handler
  (GET "/" [] (stylify recent-entries))
  (GET "/news.rss" [] handle-rss)
  (GET "/static/*" request
       (let [f (-> request :route-params :*)]
         (io/file (conf/static-folder (:conf request)) f)))
  (GET "/:year{[0-9]+}/:month{[0-9]+}/:slug" [year month slug :as request]
       (stylify (partial entry-by-y-m-slug year month slug)))
  (GET "/:year{[0-9]+}/:month{[0-9]+}" [year month]
       (stylify entries-for-month))
  (route/not-found "Page not found"))
