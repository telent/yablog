(ns yablog.core
  (require [yablog.server :as server]
           [clojure.java.io :as io]
           [clojure.edn :as edn]
           [aleph.http :as http]
           [yablog.page :as page])
  (:gen-class))

(defonce server (atom nil))

;; XXX there's a lot of hardcoded that should be confg options
;; 1) the css whould be outwith this project
;; 2) and the fonts
;; 3) and there should be some way to change the sidebar without
;; editing source files


(defn -main [conffile]
  (let [conffile (or conffile "conf.edn")
        conf (with-open [infile (java.io.PushbackReader. (io/reader conffile))]
               (edn/read infile))
        pages (page/read-pages (get conf :page-folder))]
    (reset! server
            (http/start-server (fn [req]
                                 (server/handler (assoc req :pages pages)))
                               {:port (get conf :port 4567)}))))
