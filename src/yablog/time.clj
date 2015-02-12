(ns yablog.time
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [instaparse.core :as parse]
            [instaparse.failure :as pfail]
            [clj-time.core :as time] ))

(def parse-datetime (parse/parser (io/resource "dates.bnf")))

(def months {"jan" 1 "feb" 2 "mar" 3 "apr" 4 "may" 5 "jun" 6
             "jul" 7 "aug" 8 "sep" 9 "oct" 10 "nov" 11 "dec" 12})

(defn month-name [n]
  (str/capitalize (get (clojure.set/map-invert months) n)))

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
