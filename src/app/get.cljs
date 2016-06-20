(ns app.get
  (:refer-clojure :exclude [get])
  (:require [app.queries :as queries]
            [app.extract :as extract]
            [app.validate :as validate]
            [app.elastic-search :as es]
            [app.dynamo-db :as ddb]
            [cljs.core.async :refer [<! chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defmulti get (fn [{:keys [type]}] (keyword type)))

(defmethod get :user-profile [{:keys [auth-token]}]
  (go
    (-> auth-token
        validate/jwt-token
        <!
        extract/user-id
        queries/user-profile
        ddb/request
        <!
        extract/user-profile)))

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
