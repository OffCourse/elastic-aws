(ns app.core
  (:refer-clojure :exclude [get])
  (:require [cljs.nodejs :as nodejs]
            [app.get :refer [get]]
            [app.utils :as utils]
            [app.respond :refer [respond]]
            [cljs.core.async :refer [<! chan >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(nodejs/enable-util-print!)

(defn ^:export handler [event context cb]
  (go
    (let [{:keys [type] :as event} (utils/->js event)
          data (<! (get event))
          response {:type type
                    type data}]
      (cb nil (clj->js response)))))

(defn -main [] identity)
(set! *main-cli-fn* -main)
