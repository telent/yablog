(ns yablog.rss
  (:require [yablog.page :as page]
            [yablog.hiccup :as hic]
            hiccup.util
            [hiccup.core :as hiccup]
            [clj-time.core :as time]
            [clj-time.format :as ftime]))

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
     [(page/url post)]}                 ;XXX absoluteize this somehow
    {:tag :description,
     :attrs nil,
     :content
     [(hiccup.util/escape-html (hiccup/html (hic/hiccup-entry-body post)))]}
    {:tag :author, :attrs nil, :content ["dan@telent.net (Daniel Barlow)"]}
    {:tag :pubDate,
     :attrs nil,
     :content [(ftime/unparse (ftime/formatters :rfc822) (:date post)) ]}
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
