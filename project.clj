(def patchlevel (or (System/getenv "PATCH_LEVEL") "0-SNAPSHOT"))
(defproject yablog (str "0.1." patchlevel)
  :description "Yet another blog engine"
  :url "https://github.com/telent/yablog"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [telent/texticlj "0.1.0-SNAPSHOT"]
                 [hiccup "1.0.5"]
                 [org.clojure/data.json "0.2.5"]
                 [clj-time "0.9.0"]
                 [juxt/dirwatch "0.2.2"]
                 [compojure "1.3.1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [aleph "0.4.0-beta2"]]
  :main ^:skip-aot yablog.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
