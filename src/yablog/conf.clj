(ns yablog.conf
  (require [clojure.java.io :as io]
           [clojure.edn :as edn] ))

(defn read-file [conffile]
  (with-open [infile (java.io.PushbackReader. (io/reader conffile))]
    (edn/read infile)))

(defn base-folder [conf]
  (io/file (get conf :base-folder)))

(defn subfolder [sub conf]
  (let [f (io/file (get conf sub (name sub)))]
    (if (.isAbsolute f)
      f
      (io/file (base-folder conf) f))))

(def posts-folder (partial subfolder :posts))
(def static-folder (partial subfolder :static))

(defn stylesheet [conf]
  (get conf :stylesheet "/static/css/default.css"))

(assert (= (io/file "/tmp/hello/posts")
           (posts-folder {:base-folder  "/tmp/hello"})))

(assert (= (io/file "/tmp/goodbye")
           (posts-folder {:base-folder  "/tmp/hello"
                          :posts "/tmp/goodbye"})))

(assert (= (io/file "/tmp/hello/spong")
           (posts-folder {:base-folder  "/tmp/hello"
                          :posts "spong"})))

(defn replacements [conf]
  (let [base (static-folder conf)]
    (reduce (fn [m [tag file]]
              (assoc m [tag] (read-file (io/file base file))))
            {}
            (:replace conf))))
