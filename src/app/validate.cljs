(ns app.validate
  (:require [cljs.nodejs :as node]
            [cljs.core.async :refer [<! chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ^:private jwt (node/require "jsonwebtoken"))
(def auth-secret (.. js/process -env -AUTH0_SECRET))

(defn jwt-token [token]
  (let [secret-buffer (js/Buffer. auth-secret "base64")
        c (chan)]
    (println secret-buffer)
    (.verify jwt token secret-buffer (fn [err decoded]
                                       (go
                                         (if err
                                           (>! c err)
                                           (>! c decoded)))))
    c))
