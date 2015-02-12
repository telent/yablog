(ns yablog.core
  (require [yablog.server :as server]
           [clojure.java.io :as io]
           [clojure.edn :as edn]
           [aleph.http :as http]
           [yablog.conf :as conf]
           [yablog.page :as page])
  (:gen-class))

(defonce server (atom nil))

(defn -main [conffile]
  (let [conf (conf/read-file (or conffile "conf.edn"))
        pages (page/read-pages (conf/posts-folder conf))]
    (reset! server
            (http/start-server (fn [req]
                                 (server/handler (assoc req :conf conf
                                                        :pages pages)))
                               {:port (get conf :port 4567)}))))
