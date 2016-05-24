(ns ctia.http.routes.sighting
  (:require [compojure.api.sweet :refer :all]
            [ctia.flows.crud :as flows]
            [ctia.schemas
             [common :as c]
             [sighting :refer [check-new-sighting NewSighting realize-sighting StoredSighting]]]
            [ctia.store :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defroutes sighting-routes
  (context "/sighting" []
    :tags ["Sighting"]
    (POST "/" []
      :return StoredSighting
      :body [sighting NewSighting {:description "A new Sighting"}]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Adds a new Sighting"
      :capabilities :create-sighting
      :login login
      (if (check-new-sighting sighting)
        (ok (flows/create-flow :realize-fn realize-sighting
                               :store-fn #(create-sighting @sighting-store %)
                               :entity-type :sighting
                               :login login
                               :entity sighting))
        (unprocessable-entity)))
    (PUT "/:id" []
      :return StoredSighting
      :body [sighting NewSighting {:description "An updated Sighting"}]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Updates a Sighting"
      :path-params [id :- s/Str]
      :capabilities :create-sighting
      :login login
      (if (check-new-sighting sighting)
        (ok (flows/update-flow :get-fn #(read-sighting @sighting-store %)
                               :realize-fn realize-sighting
                               :update-fn #(update-sighting @sighting-store (:id %) %)
                               :entity-type :sighting
                               :id id
                               :login login
                               :entity sighting))
        (unprocessable-entity)))
    (GET "/:id" []
      :return (s/maybe StoredSighting)
      :summary "Gets a Sighting by ID"
      :path-params [id :- s/Str]
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :read-sighting
      (if-let [d (read-sighting @sighting-store id)]
        (ok d)
        (not-found)))
    (DELETE "/:id" []
      :path-params [id :- s/Str]
      :summary "Deletes a Sighting"
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :delete-sighting
      (if (delete-sighting @sighting-store id)
        (no-content)
        (not-found))))
  (context "/sightings" []
    :tags ["Sighting"]
    (POST "/" []
      :return [c/ID]
      :body [sightings [NewSighting] {:description "a list of new Sighting"}]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Adds a list of new Sighting"
      :capabilities :create-sighting
      :login login
      (ok (map (fn [sighting]
                 (-> (flows/create-flow :entity-type :sighting
                                        :realize-fn realize-sighting
                                        :store-fn #(create-sighting @sighting-store %)
                                        :login login
                                        :entity sighting)
                     :id))
               sightings)))))
