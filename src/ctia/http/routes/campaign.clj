(ns ctia.http.routes.campaign
  (:require [compojure.api.sweet :refer :all]
            [ctia.flows.crud :as flows]
            [ctia.schemas
             [campaign :refer [NewCampaign realize-campaign StoredCampaign]]
             [common :as c]]
            [ctia.store :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defroutes campaign-routes
  (context "/campaign" []
    :tags ["Campaign"]
    (POST "/" []
      :return StoredCampaign
      :body [campaign NewCampaign {:description "a new campaign"}]
      :summary "Adds a new Campaign"
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :create-campaign
      :login login
      (ok (flows/create-flow :realize-fn realize-campaign
                             :store-fn #(create-campaign @campaign-store %)
                             :entity-type :campaign
                             :login login
                             :entity campaign)))
    (PUT "/:id" []
      :return StoredCampaign
      :body [campaign NewCampaign {:description "an updated campaign"}]
      :summary "Updates a Campaign"
      :path-params [id :- s/Str]
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :create-campaign
      :login login
      (ok (flows/update-flow :get-fn #(read-campaign @campaign-store %)
                             :realize-fn realize-campaign
                             :update-fn #(update-campaign @campaign-store (:id %) %)
                             :entity-type :campaign
                             :id id
                             :login login
                             :entity campaign)))
    (GET "/:id" []
      :return (s/maybe StoredCampaign)
      :summary "Gets a Campaign by ID"
      :path-params [id :- s/Str]
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :read-campaign
      (if-let [d (read-campaign @campaign-store id)]
        (ok d)
        (not-found)))
    (DELETE "/:id" []
      :no-doc true
      :path-params [id :- s/Str]
      :summary "Deletes a Campaign"
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities :delete-campaign
      :login login
      (if (flows/delete-flow :get-fn #(read-campaign @campaign-store %)
                             :delete-fn #(delete-campaign @campaign-store %)
                             :entity-type :campaign
                             :id id
                             :login login)
        (no-content)
        (not-found))))
  (context "/campaigns" []
    :tags ["Campaign"]
    (POST "/" []
      :return [c/ID]
      :body [campaigns [NewCampaign] {:description "a list of new Campaign"}]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Adds a list of new Campaign"
      :capabilities :create-campaign
      :login login
      (ok (map (fn [campaign]
                 (-> (flows/create-flow :entity-type :campaign
                                        :realize-fn realize-campaign
                                        :store-fn #(create-campaign @campaign-store %)
                                        :login login
                                        :entity campaign)
                     :id))
               campaigns)))))
