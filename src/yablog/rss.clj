(ns yablog.rss
  (:import [java.net URL])
  (:require [yablog.page :as page]
            [yablog.hiccup :as hic]
            hiccup.util
            [hiccup.core :as hiccup]
            [clj-time.core :as time]
            [clojure.walk :as w]
            [clj-time.format :as ftime]))

(defn merge-urls [base rel]
  (.toString (URL. (URL. base) rel)))

(defn fix-urls-in-hiccup [hiccup base-url]
  (w/postwalk
   (fn [x]
     (reduce (fn [x attr]
               (if-let [u (get x attr)]
                 (assoc x attr (merge-urls base-url u))
                 x))
             x
             [:src :href]))
   hiccup))

(assert (= [:article
            [:a {:href "http://www,google.com/hello"} "foo"]
            [:img {:src "http://www,google.com/static/f.jpg", :border 0}]]
           (fix-urls-in-hiccup [:article
                                [:a {:href "/hello"} "foo"]
                                [:img {:src "/static/f.jpg" :border 0}]]
                               "http://www,google.com")))


(defn rss-item [conf post]
  (let [base-url (:base-url conf)
        self-url (merge-urls base-url (page/url post))
        desc (-> post
                 hic/hiccup-entry-body
                 (fix-urls-in-hiccup base-url)
                 hiccup/html
                 hiccup.util/escape-html)]
    {:tag :item,
     :content
     [{:tag :title, :content [(page/title post)]}
      {:tag :link, :content [self-url]}
      {:tag :description,:content [desc]}
      {:tag :author, :attrs nil, :content [(:author conf)]}
      #_ {:tag :pubDate,
          :content [(ftime/unparse (ftime/formatters :rfc822) (:date post)) ]}
      {:tag :guid, :content [self-url] }
      {:tag :dc:date,
       :content [(ftime/unparse (ftime/formatters :date-time-no-ms)
                                (:date post))]}
      ]}))

(defn rss-posts [conf posts]
  {:tag :rss,
   :attrs
   {:version "2.0",
    :xmlns:content "http://purl.org/rss/1.0/modules/content/",
    :xmlns:dc "http://purl.org/dc/elements/1.1/",
    :xmlns:atom "http://www.w3.org/2005/Atom"
    :xmlns:trackback
    "http://madskills.com/public/xml/rss/module/trackback/",
    :xmlns:itunes "http://www.itunes.com/dtds/podcast-1.0.dtd"},
   :content
   [{:tag :channel,
     :attrs nil,
     :content
     (into
      [{:tag :title :content [(:title conf)]}
       {:tag :link :content [(:base-url conf)]}
       {:tag :atom:link :attrs {:href (merge-urls (:base-url conf) "news.rss")
                                :rel "self"
                                :type "application/rss+xml"}}
       {:tag :description,
        :attrs nil,
        :content [(:description conf)]}]
      (map (partial rss-item conf) posts))
     }]})
