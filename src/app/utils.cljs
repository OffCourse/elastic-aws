(ns app.utils
  (:require [clojure.walk :as walk]))

(defn ->js [event]
  (-> event
      js->clj
      walk/keywordize-keys))
