(ns yablog.core
  (require [yablog.server :as server]
           [clojure.java.io :as io]
           [clojure.edn :as edn]
           [aleph.http :as http]
           [yablog.page :as page])
  (:gen-class))

(defonce server (atom nil))

(defn -main [conffile]
  (let [conffile (or conffile "conf.edn")
        conf (with-open [infile (java.io.PushbackReader. (io/reader conffile))]
               (edn/read infile))
        pages (page/read-pages (get conf :page-folder))]
    (reset! server
            (http/start-server (fn [req]
                                 (server/handler (assoc req :pages pages)))
                               {:port (get conf :port 4567)}))))
