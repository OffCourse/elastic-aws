(set-env!
 :source-paths #{"src"}
 :resource-paths  #{"resources"}
 :dependencies '[[adzerk/boot-cljs            "1.7.228-1"      :scope "test"]
                 [org.clojure/core.async      "0.2.374"]
                 [cljs-ajax                   "0.5.3"]
                 [com.rpl/specter             "0.9.2"]
                 [org.clojure/clojurescript   "1.7.228"]])

(require
  '[adzerk.boot-cljs      :refer :all])

(deftask build []
  (task-options! cljs   {:compiler-options {:optimizations :simple
                                            :target :nodejs}})
  (comp (cljs)
        (target)))

(deftask dev []
  (comp (watch)
        (build)))

