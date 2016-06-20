(ns app.get
  (:refer-clojure :exclude [get])
  (:require [app.queries :as queries]
            [app.extract :as extract]
            [app.elastic-search :as es]
            [cljs.nodejs :as node]
            [cljs.core.async :refer [<! chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private jwt (node/require "jsonwebtoken"))
(def ^:private AWS (node/require "aws-sdk"))
(def ^:private doc (node/require "dynamodb-doc"))
(def dynamo (.DynamoDB doc))

(defmulti get (fn [{:keys [type]}] (keyword type)))

(defn validate-token [token secret]
  (let [secret-buffer (js/Buffer. secret "base64")
        c (chan)]
    (.verify jwt token secret-buffer (fn [err decoded]
                                       (go
                                         (if err
                                           (>! c err)
                                           (>! c decoded)))))
    c))

(def auth-secret (.. js/process -env -AUTH0_SECRET))

(defn extract-user-id [decoded-token]
  (-> decoded-token
      (js->clj :keywordize-keys true)
      :sub))

(defn get-user-profile [user-id]
  (let [c (chan)
        query {:TableName "user-profiles"
               :IndexName "user-id-index"
               :KeyConditionExpression "#ui = :id"
               :ExpressionAttributeNames {"#ui" "user-id"}
               :ExpressionAttributeValues {":id" user-id}}]
    (.query dynamo (clj->js query) #(go (>! c
                                              (if %1
                                                (do
                                                  (println %1)
                                                  %1)
                                                (do
                                                (println %2)
                                                %2)))))
    c))

(defmethod get :user-profile [{:keys [auth-token]}]
  (go
    (-> auth-token
             (validate-token auth-secret)
             <!
             extract-user-id
             get-user-profile
             <!
             (js->clj :keywordize-keys true)
             :Items
             first)))

(defmethod get :collection [{:keys [type collection] :as event}]
  (go
    (-> collection
        queries/collection
        es/request
        <!
        extract/courses)))

(defmethod get :course [{:keys [course] :as event}]
  (go
    (-> course
        queries/course
        es/request
        <!
        extract/courses
        first)))

(defmethod get :default [event]
  (go
    {:error :query-not-supported}))
