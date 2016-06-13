(ns app.get
  (:refer-clojure :exclude [get])
  (:require [app.db :as db]
            [com.rpl.specter :refer [ALL select-first]]
            [cljs.core.async :refer [<! close! put! chan >!]]
            [clojure.walk :as walk]
            [cljs.nodejs :as node]
            [clojure.set :as set])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private js-request (node/require "request"))

(defn request [url-or-opts]
  (let [c-resp (chan 1)]
    (js-request (clj->js url-or-opts)
                (fn [error response body]
                  (put! c-resp
                              (if error
                                {:error error}
                                {:response response :body body})
                              #(close! c-resp))))
    c-resp))

(defmulti get (fn [{:keys [type]}] (keyword type)))

(def endpoint (.. js/process -env -ELS_ENDPOINT))

(def auth {:user "user1"
           :pass "test"})

(defn extract-courses [response]
  (let [json (.parse js/JSON response)
        hits (-> json
                 js->clj
                 walk/keywordize-keys
                 :hits
                 :hits)]
    (map :_source hits)))

(defmethod get :collection [{:keys [type collection] :as event}]
  (go
    (let [{:keys [collection-type collection-name]} collection
          query-key (case (keyword collection-type)
                      :flags :flags
                      :tags :checkpoints.tags
                      :curators :curator)
          query {:query {:bool {:should [{:match {query-key collection-name}}]}}}
          {:keys [body]} (<! (request {:url  endpoint
                                       :body (.stringify js/JSON (clj->js query))
                                       :auth auth}))]
      (extract-courses body))))

(defmethod get :course [{:keys [course] :as event}]
  (go
    (let [{:keys [course-slug curator]} course
          query {:query {:bool {:must [{:match {:course-slug course-slug}}
                                       {:match {:curator curator}}]}}}
          {:keys [body]} (<! (request {:url endpoint
                                       :body (.stringify js/JSON (clj->js query))
                                       :auth auth}))]
      (first (extract-courses body)))))

(defmethod get :default [event]
  (go
    {:error :query-not-supported}))
