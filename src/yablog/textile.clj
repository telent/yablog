(ns yablog.textile
  (import [net.sf.textile4j Textile]))

(defonce textile (Textile.))

(defn to-html [n]
  (.process textile n))
