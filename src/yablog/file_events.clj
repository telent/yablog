(ns yablog.file-events
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.core.async :as async
             :refer [timeout >!! >! <! alts! go chan]]
            [juxt.dirwatch :refer (watch-dir)]))

(defn file-watcher-chan [folders]
  (let [ch (chan)
        ;; watch-dir invokes its callback in an agent, so won't block
        ;; the main thread if there's nothing listening to the channel
        send-events (fn [notif] (>!! ch notif))]
    (dorun (map #(watch-dir send-events (io/file %)) folders))
    ch))
