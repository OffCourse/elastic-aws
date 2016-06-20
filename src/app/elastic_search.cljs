(ns app.elastic-search
  (:require [cljs.core.async :refer [<! close! put! chan >!]]
            [cljs.nodejs :as node])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private js-request (node/require "request"))

(defn- -request [url-or-opts]
  (let [c-resp (chan 1)]
    (js-request (clj->js url-or-opts)
                (fn [error response body]
                  (put! c-resp
                        (if error
                          {:error error}
                          {:response response :body body})
                        #(close! c-resp))))
    c-resp))

(defn request [query]
  (go
    (:body (<! (-request {:url  (.. js/process -env -ELS_ENDPOINT)
                         :body (.stringify js/JSON (clj->js query))
                         :auth {:user "user1"
                                :pass "test"}})))))

