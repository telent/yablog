(ns yablog.flickr
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [clojure.data.json :as json]
            [clojure.string :as str]))

(defn photo-info [api-key id]
  (let [r (http/get
           "https://api.flickr.com/services/rest/"
           {:query-params
            {:method "flickr.photos.getInfo"
             :api_key api-key
             :format "json"
             :nojsoncallback "1"
             :photo_id id }})]
    (-> @r :body bs/to-string json/read-str)))

(defn photo-page [info]
  (let [urls (-> info (get "photo") (get "urls") (get "url"))]
    (get
     (first (filter #(= (get % "type") "photopage") urls))
     "_content")))

(defn size-letter [length]
  "flickr size letter corresponding to the largest size available no bigger than length pixels along its longest edge"
  (condp <= length
    2048 "k"
    1600 "h"
    1024 "b"
    800 "c"
    640 "z"
    500 "-"
    320 "n"
    240 "m"
    100 "t"
    75 "s"
    length))

(assert (= "b" (size-letter 1500)))
(assert (= "b" (size-letter 1599)))
(assert (= "h" (size-letter 1600)))


(defn photo-image-url [info size]
  (let [photo (get info "photo")
        server (get photo "server")
        farm (get photo "farm")
        id (get photo "id")
        secret (get photo "secret")
        size (size-letter size)]
    (str "https://farm" farm ".staticflickr.com/" server
         "/" id "_" secret "_" size ".jpg")))

(defn populate-photo-divs [api-key div]
  (if (vector? div)
    (let [[el-name attrs & kids] div]
      (if (and el-name
               (= el-name :div)
               (.startsWith (get attrs :class) "flickr-photo")
               (get attrs :photo_id))
        (let [info (photo-info api-key (get attrs :photo_id))]
          (println info)
          [:div attrs
           [:a {:href (photo-page info)}
            [:img {:src (photo-image-url info 300)}]
            [:div {:class "caption"}
             "Photo by " (-> info (get "photo") (get "owner") (get "username"))
             " from Flickr"]]])
        div))
    div))


;;  (image-url "7da74d0034b15043174132479f248d6c" "533614752" :n)


;; https://api.flickr.com/services/rest/?method=flickr.photos.getInfo&api_key=7da74d0034b15043174132479f248d6c&format=json&nojsoncallback=1&text=cats&extras=url_n&photo_id=533614752
