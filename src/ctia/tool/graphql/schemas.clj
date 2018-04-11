(ns ctia.tool.graphql.schemas
  (:require
   [flanders.utils :as fu]
   [ctia.feedback.graphql.schemas :as feedback]
   [ctia.relationship.graphql.schemas :as relationship]
   [ctia.schemas.graphql
    [flanders :as flanders]
    [helpers :as g]
    [pagination :as pagination]
    [refs :as refs]
    [sorting :as sorting]]
   [ctia.schemas.sorting :as sort-fields]
   [ctim.schemas.tool :as ctim-tool]))

(def ToolType
  (let [{:keys [fields name description]}
        (flanders/->graphql
         (fu/optionalize-all ctim-tool/Tool)
         {})]
    (g/new-object
     name
     description
     []
     (merge fields
            feedback/feedback-connection-field
            relationship/relatable-entity-fields))))

(def tool-order-arg
  (sorting/order-by-arg
   "ToolOrder"
   "tools"
   (into {}
         (map (juxt sorting/sorting-kw->enum-name name)
              sort-fields/tool-sort-fields))))

(def ToolConnectionType
  (pagination/new-connection ToolType))


