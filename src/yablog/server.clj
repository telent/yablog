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
            [yablog.time :as ytime]
            [yablog.flickr :as flickr]
            [yablog.hiccup :as hic]
            [yablog.rss :as rss]
            [ring.util.response :as resp]
            [clojure.walk :as w]
            [clojure.string :as str]
            [clojure.xml :as xml]))

(defn recent-posts-box [req]
  (let [pages (page/recent-pages 10 (:pages req))]
    [:div {:class "sidebox"}
     [:h2 "Recent posts"]
     [:p "Things I can remember"]
     [:ul (map (fn [p] [:li [:a {:href (page/url p)} (page/title p)]])
               pages)]]))

(defn by-month-box [req]
  (let [ymlink (fn [y m] [:a {:href (str "/" y "/" m "/")} (ytime/month-name m)])
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
    (let [conf (:conf req)
          hic (w/postwalk
               (partial flickr/populate-photo-divs (:flickr-api-key conf))
               (hiccuper req))
          title (:data-title
                 (second
                  (first (filter #(= (first %) :article)
                                 (tree-seq vector? rest hic)))))
          ]
      (hpage/html5
       [:head
        [:link {:href (conf/stylesheet conf)
                :rel "stylesheet"}]
        [:link {:rel "alternate"
                :type "application/rss+xml"
                :title "RSS"
                :href "/news.rss"}]
        [:title (str/join " - " (remove not [title (:title conf)]))]]
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
         [:footer]])))))


(defn recent-entries [req]
  (into [:article {:data-title "Recently"}]
        (map hic/hiccup-entry (page/recent-pages 5 (:pages req)))))

(defn entries-for-month [req]
  (let [p (:route-params req)
        y (Integer. (:year p))
        m (Integer. (:month p))
        start (time/date-time y m)
        next-month (time/plus start (time/months 1))
        prev-month (time/minus start (time/months 1))]
    ;; XXX would be neat if it included "older" and "newer" links
    (conj
     (into [:article
            {:data-title (ftime/unparse (ftime/formatter "MMMMMM yyyy") start)}]
           (map hic/hiccup-entry
                (page/pages-in-month y m (:pages req))))
     [:div {:class "nav"}
      [:a {:href (ftime/unparse (ftime/formatter "/yyyy/M/") prev-month)}
       "&#x27ea;" (ftime/unparse (ftime/formatter "MMM yyyy") prev-month)]
      " "
      [:a {:href (ftime/unparse (ftime/formatter "/yyyy/M/") next-month)}
       (ftime/unparse (ftime/formatter "MMM yyyy") next-month)   "&#x27eb;"]
      ])))

(defn entry-by-y-m-d-slug [y m d slug request]
  (let [p (page/find-page (Integer. y)
                          (Integer. m)
                          (Integer. d)
                          slug (:pages request))]
    [:article {:data-title (page/title p)}
     (hic/hiccup-entry p)]))

(defn handle-rss [req]
  (let [recent (page/recent-pages 10 (:pages req))
        xml (rss/rss-posts (:conf req) recent)
        body (with-out-str (xml/emit xml))]
    {:status 200
     :headers {"Content-Type" "application/rss+xml; charset=utf-8"}
     :body body}))

(defroutes handler
  (GET "/" [] (stylify recent-entries))
  (GET "/news.rss" [] handle-rss)
  (GET "/static/*" request
       (let [f (-> request :route-params :*)]
         (io/file (conf/static-folder (:conf request)) f)))
  (GET "/:year{[0-9]+}/:month{[0-9]+}/:day{[0-9]+}/:slug" [year month day slug :as request]
       (stylify (partial entry-by-y-m-d-slug year month day slug)))
  (GET "/:year{[0-9]+}/:month{[0-9]+}/" [year month]
       (stylify entries-for-month))
  (GET "/:year{[0-9]+}/:month{[0-9]+}" [year month]
       (resp/redirect (str "/" year "/" month "/")))

  (GET "/diary/*" [*] (resp/redirect (str "/" *)))

  (route/not-found "Page not found"))
