(ns app.get
  (:refer-clojure :exclude [get])
  (:require [app.queries :as queries]
            [app.extract :as extract]
            [app.elastic-search :as es]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defmulti get (fn [{:keys [type]}] (keyword type)))

(defmethod get :user-profile [_]
  (go
    {:user-name "yeehaa"}))

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
