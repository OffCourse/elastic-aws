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

(defmethod get :collection [{:keys [type collection] :as event}]
  (go
    (let [{:keys [body]} (<! (request {:url "https://89aaedd97ab45326945f499c51e376b0.us-east-1.aws.found.io:9243/offcourse/courses/_search"
                                       :auth {:user "user1"
                                              :pass "test"}}))
          json (.parse js/JSON body)
          hits (-> json
                   js->clj
                   walk/keywordize-keys
                   :hits
                   :hits)]
      (map :_source hits))))


(defmethod get :course [{:keys [course] :as event}]
  (let [{:keys [course-slug curator]} course]
    (go
      (let [courses (<! (get {:type :courses}))]
        (select-first [ALL #(and (= (:course-slug %) course-slug)
                                 (= (:curator %) curator))] courses)))))


(defmethod get :default [event]
  (go
    (let [db (<! (db/s3->))]
      db)))
