(ns yablog.page
  (:require [clojure.string :as str]
            [instaparse.core :as parse]
            [instaparse.failure :as pfail]
            [clj-time.core :as time]
            [clojure.java.io :as io]))

(def parse-datetime (parse/parser (io/resource "dates.bnf")))

(def months {"jan" 1 "feb" 2 "mar" 3 "apr" 4 "may" 5 "jun" 6
             "jul" 7 "aug" 8 "sep" 9 "oct" 10 "nov" 11 "dec" 12})

(defn read-datetime [s]
  (let [tree (parse-datetime (str/trim (str/lower-case s)))
        els (if-not (parse/failure? tree)
              (reduce (fn [m [key val]]
                        (if (keyword? key) (assoc m key val) m))
                      {}
                      (tree-seq #(keyword (first %)) rest tree)))]
    (if els
      (apply time/date-time
             (map #(Integer. %)
                  [(:year els) (get months (:monthname els)) (:day els)
                   (:hour els) (:min els) (:sec els)]))
      (pfail/pprint-failure tree))))

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
        (assoc h :date (read-datetime date))
        h))))

(defn textile? [filename]
  (.endsWith (.toString filename) ".textile"))

(defn slug [name]
  (str/lower-case
   (str/replace
    (str/replace name #"[^A-Za-z0-9]+" "_")
    #"_+\z" "")))

(assert (= "of_course_the_foo_batr_baz"
           (yablog.page/slug "Of course, the foo batr baz,")))

(defn read-pages [path]
  (let [names (filter textile? (file-seq (io/file path)))]
    (reduce (fn [m name]
              (let [h (read-page name)]
                (assoc m (slug (get h :title (:subject h))) h)))
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
  (filter (partial page-in-date-interval?
                   (date-interval-for y m))
          (vals pages)))

(defn recent-pages [n pages]
  (take n
        (reverse
         (sort-by :date
                  (org.joda.time.DateTimeComparator/getInstance)
                  (vals pages)))))

#_
(count (filter (partial page-in-date-interval? (date-interval-for 2003 2))
               (vals @all-pages)))
