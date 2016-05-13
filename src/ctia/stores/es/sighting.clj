(ns ctia.stores.es.sighting
  (:require
    [ctia.domain.id :as id]
    [ctia.stores.es.crud :as crud]
    [ctia.stores.es.query :refer [sightings-by-observables-query]]
    [ctia.schemas.sighting :refer [Sighting
                                   NewSighting
                                   StoredSighting
                                   realize-sighting]]
    [ctia.schemas.indicator :refer [Indicator]]
    [ctia.lib.es.document :refer [search-docs]]
    [schema.core :as s]))


(def handle-create-sighting (crud/handle-create :sighting StoredSighting))
(def handle-read-sighting (crud/handle-read :sighting StoredSighting))
(def handle-update-sighting (crud/handle-update :sighting StoredSighting))
(def handle-delete-sighting (crud/handle-delete :sighting StoredSighting))
(def handle-list-sightings (crud/handle-find :sighting StoredSighting))

(def ^{:private true} mapping "sighting")

(defn handle-list-sightings-by-indicators
  [state indicator-ids params]
  (let [indicator-ids (mapv id/long-id indicator-ids)]
    (handle-list-sightings state {:type "sighting"
                                  [:indicators :indicator_id]
                                  indicator-ids} params)))

(defn handle-list-sightings-by-observables
  [{:keys [conn index]}  observables params]

  (search-docs conn
               index
               mapping
               nil
               (assoc params :query (sightings-by-observables-query observables))))
