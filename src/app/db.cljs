(ns app.db
  (:refer-clojure :exclude [get])
  (:require [cljs.nodejs :as nodejs]
            [app.utils :as utils]
            [cljs.core.async :refer [chan >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def AWS (nodejs/require "aws-sdk"))
(def doc (nodejs/require "dynamodb-doc"))
(def dynamo (.DynamoDB doc))
(def s3 (new AWS.S3))

(defn handle-s3-response [response]
  (let [db-string (-> response
                      utils/->js
                      :Body
                      (.toString "utf8"))]
    (->> db-string
         (.parse js/JSON)
         utils/->js)))

(defn s3-> []
  (let [c (chan)
        query {:Bucket "dynamo-events"
               :Key "courses.json"}]
    (.getObject s3 (clj->js query) #(go (>! c {:courses (handle-s3-response %2)})))
    c))
