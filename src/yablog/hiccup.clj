(ns yablog.hiccup
  (:require [clojure.java.io :as io]
            [hiccup.core :as hiccup]
            [texticlj.core :as tx]
            [markdown.core :as md]
            [hiccup.page :as hpage]
            [yablog.page :as page]
            [clj-time.format :as ftime]
            hiccup.util)
  (:import [java.io StringWriter]))

(def date-formatter (ftime/formatters :rfc822))

(defn format-time [t]
  (ftime/unparse date-formatter t))

(defn hiccup-entry-body [page]
  (with-open [r (io/reader (:pathname page))]
    (page/read-headers r)
    (cond (page/textile? (:pathname page))
          (tx/to-hiccup (slurp r))
          (page/markdown? (:pathname page))
          (let [w (new StringWriter)]
            (md/md-to-html r w)
            (.toString w)))))

(defn hiccup-entry [page]
  (into [:div {:class "entry"}
         [:h1 {:class "title"}
          (page/title page)
          [:span {:class "permalink"}
           [:a {:href (page/url page)} "#"]]]
         [:h2 {:class "date"} (format-time (:date page))]]
        (hiccup-entry-body page)))
