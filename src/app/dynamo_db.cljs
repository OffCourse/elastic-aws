(ns app.dynamo-db
(:require [cljs.nodejs :as node]
          [cljs.core.async :refer [<! chan]])
(:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private AWS (node/require "aws-sdk"))
(def ^:private doc (node/require "dynamodb-doc"))
(def dynamo (.DynamoDB doc))

(defn request [query]
  (let [c (chan)]
    (.query dynamo (clj->js query) #(go (>! c
                                            (if %1
                                              (do
                                                (println %1)
                                                %1)
                                              %2))))
    c))
