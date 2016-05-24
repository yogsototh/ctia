(ns ctia.http.routes.ttp
  (:require [compojure.api.sweet :refer :all]
            [ctia.flows.crud :as flows]
            [ctia.schemas
             [common :as c]
             [ttp :refer [NewTTP realize-ttp StoredTTP]]]
            [ctia.store :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defroutes ttp-routes
  (context "/ttp" []
    :tags ["TTP"]
    (POST "/" []
      :return StoredTTP
      :body [ttp NewTTP {:description "a new TTP"}]
      :summary "Adds a new TTP"
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :create-ttp
      :login login
      (ok (flows/create-flow :realize-fn realize-ttp
                             :store-fn #(create-ttp @ttp-store %)
                             :entity-type :ttp
                             :login login
                             :entity ttp)))
    (PUT "/:id" []
      :return StoredTTP
      :body [ttp NewTTP {:description "an updated TTP"}]
      :summary "Updates a TTP"
      :path-params [id :- s/Str]
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :create-ttp
      :login login
      (ok (flows/update-flow :get-fn #(read-ttp @ttp-store %)
                             :realize-fn realize-ttp
                             :update-fn #(update-ttp @ttp-store (:id %) %)
                             :entity-type :ttp
                             :id id
                             :login login
                             :entity ttp)))
    (GET "/:id" []
      :return (s/maybe StoredTTP)
      :summary "Gets a TTP by ID"
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :read-ttp
      :path-params [id :- s/Str]
      (if-let [d (read-ttp @ttp-store id)]
        (ok d)
        (not-found)))
    (DELETE "/:id" []
      :no-doc true
      :path-params [id :- s/Str]
      :summary "Deletes a TTP"
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :delete-ttp
      :login login
      (if (flows/delete-flow :get-fn #(read-ttp @ttp-store %)
                             :delete-fn #(delete-ttp @ttp-store %)
                             :entity-type :ttp
                             :id id
                             :login login)
        (no-content)
        (not-found))))
  (context "/ttps" []
    :tags ["TTP"]
    (POST "/" []
      :return [c/ID]
      :body [ttps [NewTTP] {:description "a list of new Ttp"}]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Adds a list of new Ttp"
      :capabilities :create-ttp
      :login login
      (ok (map (fn [ttp]
                 (-> (flows/create-flow :entity-type :ttp
                                        :realize-fn realize-ttp
                                        :store-fn #(create-ttp @ttp-store %)
                                        :login login
                                        :entity ttp)
                     :id))
               ttps)))))
