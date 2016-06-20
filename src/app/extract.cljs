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
