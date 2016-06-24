(ns app.core
  (:refer-clojure :exclude [get])
  (:require [cljs.nodejs :as nodejs]
            [app.get :refer [get]]
            [app.utils :as utils]
            [app.respond :refer [respond]]
            [cljs.core.async :refer [<! chan >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(nodejs/enable-util-print!)

(defn found-action [type data]
  {:type :found-data
   :payload {:type type
             type data}})

(defn not-found-action [query]
  {:type :not-found-data
   :payload query})

(defn ^:export handler [event context cb]
  (go
    (let [{:keys [type] :as event} (utils/->js event)
          type (if (= type "collection") :courses type)
          data (<! (get event))]
      (if data
        (cb nil (clj->js (found-action type data)))
        (cb nil (clj->js (not-found-action event)))))))

(defn -main [] identity)
(set! *main-cli-fn* -main)
