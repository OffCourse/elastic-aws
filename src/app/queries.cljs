(ns app.queries)

(defn- query [subquery]
  {:query {:bool subquery}})

(defn course [course]
  (let [{:keys [course-slug curator]} course]
    (query {:must [{:match {:course-slug course-slug}}
                    {:match {:curator curator}}]})))

(defn collection [collection]
  (let [{:keys [collection-type collection-name]} collection
        query-key (case (keyword collection-type)
                    :flags :flags
                    :tags :checkpoints.tags
                    :curators :curator)]
    (query {:should [{:match {query-key collection-name}}]})))
