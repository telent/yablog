(ns yablog.core
  (:require [yablog.server :as server]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [aleph.http :as http]
            [yablog.conf :as conf]
            [yablog.file-events :as file-events]
            [clojure.core.async :as async
             :refer [timeout >!! >! <! alts! go chan]]
            [yablog.page :as page])
  (:gen-class))

(defonce server (atom nil))

(defn handle-file-change [folder pages event]
  (let [file (:file event)]
    (when (page/textile? file)
      (println [:update file])
      (when-let [u (page/url (page/find-by-pathname file @pages))]
        (swap! pages dissoc u))
      (let [page (page/read-page file)]
        (swap! pages assoc (page/url page) page)))))

(defn handle-file-changes [folder pages]
  (let [chan (file-events/file-watcher-chan [folder])]
    (go (while true
          (handle-file-change folder pages (<! chan))))))

(defn -main [conffile]
  (let [conf (conf/read-file (or conffile "conf.edn"))
        folder (conf/posts-folder conf)
        pages (atom (page/read-pages folder))]
    (handle-file-changes folder pages)
    (reset! server
            (http/start-server (fn [req]
                                 (server/handler (assoc req :conf conf
                                                        :pages @pages)))
                               {:port (get conf :port 4567)}))))
