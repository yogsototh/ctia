(ns ctia.stores.atom.sighting
  (:require [clojure.tools.logging :as log]
            [ctia.domain.id :as id]
            [ctia.schemas.indicator :refer [StoredIndicator]]
            [ctia.schemas.sighting
             :refer [NewSighting StoredSighting realize-sighting]]
            [ctia.store :refer [ISightingStore]]
            [ctia.stores.atom.common :as mc]
            [ctia.lib.pagination :refer [list-response-schema]]
            [schema.core :as s]
            [ctia.schemas.common :as c]))

(def handle-create-sighting (mc/create-handler-from-realized StoredSighting))
(def handle-read-sighting (mc/read-handler StoredSighting))
(def handle-update-sighting (mc/update-handler-from-realized StoredSighting))
(def handle-delete-sighting (mc/delete-handler StoredSighting))
(def handle-list-sightings (mc/list-handler StoredSighting))

(s/defn handle-list-sightings-by-indicators :- (list-response-schema StoredSighting)
  [sightings-state :- (s/atom {s/Str StoredSighting})
   indicator-ids :- (s/maybe [(s/protocol id/ID)])
   params]
  (log/info {:message "handle-list-sightings-by-indicators"
             :part 1
             :indicators-ids indicator-ids
             :params params})
  (let [indicators-set (->> indicator-ids
                            (map (fn [id]
                                   {:indicator_id (id/long-id id)}))
                            set)]
    (handle-list-sightings sightings-state
                           {:indicators indicators-set} params)))

(s/defn handle-list-sightings-by-observables :- (list-response-schema StoredSighting)
  [sightings-state :- (s/atom {s/Str StoredSighting})
   observables :- (s/maybe [c/Observable])
   params]
  (handle-list-sightings sightings-state
                         {:observables (set observables)} params))
