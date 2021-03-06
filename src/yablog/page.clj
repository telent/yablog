(ns yablog.page
  (:require [clojure.string :as str]
            [instaparse.core :as parse]
            [instaparse.failure :as pfail]
            [clj-time.core :as time]
            [yablog.time :as ytime]
            [clojure.java.io :as io]))

(defn read-headers [reader]
  (reduce (fn [ret line]
            (let [[name val] (str/split line #":\s+" 2)]
              (assoc ret (-> name str/lower-case keyword) val)))
          {}
          (take-while (complement empty?) (line-seq reader))))

(defn read-page [file]
  (with-open [r (io/reader file)]
    (let [h (assoc (read-headers r) :pathname file ) ]
      (if-let [date (get h :date)]
        (assoc h :date (ytime/read-datetime date))
        h))))

(defn textile? [filename]
  (and (.endsWith (.toString filename) ".textile")
       (.exists filename)
       (.canRead filename)))

(defn markdown? [filename]
  (and (.endsWith (.toString filename) ".md")
       (.exists filename)
       (.canRead filename)))

(defn renderable? [x] (or (textile? x) (markdown? x)))

(defn slug [name]
  (str/lower-case
   (str/replace
    (str/replace name #"[^A-Za-z0-9]+" "_")
    #"_+\z" "")))

(assert (= "of_course_the_foo_batr_baz"
           (yablog.page/slug "Of course, the foo batr baz,")))

(defn title [page]
  (or (:title page) (:subject page)))

(defn url [page]
  (if-let [title (title page)]
    (if-let [d (:date page)]
      (let [y (time/year d)
            m (time/month d)
            d (time/day d)]
        (str "/" y "/" m "/" d "/" (slug title)))
      (slug title))))

(defn read-pages [path]
  (let [names (filter renderable? (file-seq (io/file path)))]
    (reduce (fn [m name]
              (let [h (read-page name)]
                (assoc m (url h) h)))
            {}
            names)))

(defn date-interval-for [y m]
  (let [date (time/date-time y m)
        start (time/minus date (time/days 1))
        end (time/plus date (time/months 1) (time/days 1))]
    (time/interval start end)))

(defn page-in-date-interval? [interval page]
  (time/within? interval (:date page) ))

(defn pages-in-month [y m pages]
  (sort-by :date
           (org.joda.time.DateTimeComparator/getInstance)
           (filter (partial page-in-date-interval?
                            (date-interval-for y m))
                   (vals pages))))

(defn find-page [y m d slug pages]
  (get pages (str "/" y "/" m "/" d "/" slug)))

(defn find-by-pathname [file pages]
  (first (filter #(= (:pathname %) file) (vals pages))))

(defn recent-pages [n pages]
  (take n
        (reverse
         (sort-by :date
                  (org.joda.time.DateTimeComparator/getInstance)
                  (vals pages)))))

#_
(count (filter (partial page-in-date-interval? (date-interval-for 2003 2))
               (vals @all-pages)))
