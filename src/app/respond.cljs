(ns app.respond
  (:require [clojure.walk :as walk]
            [cljs.core.async :refer [<! chan >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defmulti respond
  (fn [_ {:keys [type] :as event}] (keyword type)))

(defmethod respond :course [data {:keys [type] :as event}]
  {:type :course
   type data})

(defmethod respond :courses [data {:keys [type] :as event}]
  {:type :courses
   type data})

(defmethod respond :collection [data {:keys [type collection] :as event}]
  (let [ids (map :course-id data)
        collection (assoc collection :course-ids ids)]
    {:type :collection
     type collection}))
