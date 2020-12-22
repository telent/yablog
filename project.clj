(def majorminor (clojure.string/trim-newline (slurp "VERSION")))
(def patchlevel (or (System/getenv "PATCH_LEVEL") "0-SNAPSHOT"))

(defproject yablog (str majorminor "." patchlevel)
  :description "Yet another blog engine"
  :url "https://github.com/telent/yablog"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [aleph "0.4.6"]
                 [clj-time "0.15.2"]
                 [compojure "1.6.2"]
                 [hiccup "1.0.5"]
                 [markdown-clj "1.10.5"]
                 [juxt/dirwatch "0.2.5"]
                 [org.clojure/core.async "1.3.610"]
                 [org.clojure/data.json "1.0.0"]
                 [telent/texticlj "0.1.0-SNAPSHOT"]
                 ]
  :main ^:skip-aot yablog.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
