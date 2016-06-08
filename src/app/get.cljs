(ns app.get
  (:refer-clojure :exclude [get])
  (:require [app.db :as db]
            [com.rpl.specter :refer [ALL select-first]]
            [cljs.core.async :refer [<! chan >!]]
            [clojure.walk :as walk]
            [cljs.nodejs :as node]
            [clojure.set :as set])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def http (node/require "http"))

(defn handle-response [response]
  (let [{:keys [type error] :as response} (-> response
                                              walk/keywordize-keys)]
    (if ((keyword type) response)
      ((keyword type) response)
      {:error :not-found})))

(defn fetch [{:keys [endpoint]} query]
  (.request http (clj->js {:method "GET"
                           :host "http://www.google.com"})
            #(println %2))
  (go
    (str "I")))

(defmulti get (fn [{:keys [type]}] (keyword type)))

(defmethod get :collection [{:keys [type collection] :as event}]
  (go
    (-> (<! (fetch "ho" "hi")))))

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
