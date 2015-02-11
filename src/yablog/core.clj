(ns yablog.core
  (require [yablog.server :as server]
           [aleph.http :as http]
           [yablog.page :as page])
  (:gen-class))

(defonce server (http/start-server #'server/handler {:port 4567}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
