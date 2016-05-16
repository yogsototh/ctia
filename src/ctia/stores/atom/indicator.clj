(ns ctia.stores.atom.indicator
  (:require [ctia.schemas
             [indicator :refer [StoredIndicator]]
             [judgement :refer [StoredJudgement]]]
            [ctia.lib.pagination :refer [list-response-schema]]
            [ctia.stores.atom.common :as mc]
            [ctia.domain.id :as id]
            [schema.core :as s]))

(def handle-create-indicator (mc/create-handler-from-realized StoredIndicator))
(def handle-read-indicator (mc/read-handler StoredIndicator))
(def handle-update-indicator (mc/update-handler-from-realized StoredIndicator))
(def handle-delete-indicator (mc/delete-handler StoredIndicator))
(def handle-list-indicators (mc/list-handler StoredIndicator))

(s/defn handle-list-indicators-by-ids :- (list-response-schema StoredIndicator)
  [indicator-state :- (s/atom {s/Str StoredIndicator})
   ids :- (s/maybe [(s/protocol id/ID)])
   params]
  (let [indicator-ids (set (map id/short-id ids))]
    (handle-list-indicators indicator-state {:id indicator-ids} params)))
