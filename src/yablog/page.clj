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
    (let [h (read-headers r)]
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
                (assoc m (slug (get h :title "")) h)))
            {}
            names)))

(defonce all-pages (atom {}))
