(ns app.extract
  (:require [clojure.walk :as walk]))

(defn courses [response]
  (let [json (.parse js/JSON response)
        hits (-> json
                 js->clj
                 walk/keywordize-keys
                 :hits
                 :hits)]
    (map :_source hits)))


(defn user-id [token]
  (-> token
      (js->clj :keywordize-keys true)
      :sub))

(defn user-profile [res]
  (-> res
      (js->clj :keywordize-keys true)
      :Items
      first))
