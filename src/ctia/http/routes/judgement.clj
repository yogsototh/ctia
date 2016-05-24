(ns ctia.http.routes.judgement
  (:require [compojure.api.sweet :refer :all]
            [ctia
             [properties :refer [properties]]
             [store :refer :all]]
            [ctia.domain.id :as id]
            [ctia.flows.crud :as flows]
            [ctia.http.routes.common :refer [PagingParams]]
            [ctia.schemas
             [common :as c]
             [judgement :refer [NewJudgement realize-judgement StoredJudgement]]
             [relationships :as rel]]
            [ring.util.http-response :refer :all]
            [schema-tools.core :as st]
            [schema.core :as s]))

(s/defschema FeedbacksByJudgementQueryParams
  (st/merge
   PagingParams
   {(s/optional-key :sort_by) (s/enum :id :feedback :reason)}))

(def ->id
  (id/long-id-factory :judgement
                      #(get-in @properties [:ctia :http :show])))

(defroutes judgement-routes
  (context "/judgement" []
    :tags ["Judgement"]
    (POST "/" []
      :return StoredJudgement
      :body [judgement NewJudgement {:description "a new Judgement"}]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Adds a new Judgement"
      :capabilities :create-judgement
      :login login
      (ok (flows/create-flow :realize-fn realize-judgement
                             :store-fn #(create-judgement @judgement-store %)
                             :entity-type :judgement
                             :login login
                             :entity judgement)))
    (POST "/:judgement-id/indicator" []
      :return (s/maybe rel/RelatedIndicator)
      :path-params [judgement-id :- s/Str]
      :body [indicator-relationship rel/RelatedIndicator]
      :header-params [api_key :- s/Str]
      :summary "Adds an Indicator to a Judgement"
      :capabilities :create-judgement
      (if-let [d (add-indicator-to-judgement @judgement-store
                                             judgement-id
                                             indicator-relationship)]
        (ok d)
        (not-found)))
    (GET "/:id" []
      :return (s/maybe StoredJudgement)
      :path-params [id :- s/Str]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Gets a Judgement by ID"
      :capabilities :read-judgement
      (if-let [d (read-judgement @judgement-store id)]
        (ok d)
        (not-found)))
    (DELETE "/:id" []
      :no-doc true
      :path-params [id :- s/Str]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Deletes a Judgement"
      :capabilities :delete-judgement
      :login login
      (if (flows/delete-flow :get-fn #(read-judgement @judgement-store %)
                             :delete-fn #(delete-judgement @judgement-store %)
                             :entity-type :judgement
                             :id id
                             :login login)
        (no-content)
        (not-found))))
  (context "/judgements" []
    :tags ["Judgement"]
    (POST "/" []
      :return [c/ID]
      :body [judgements [NewJudgement] {:description "a list of new Judgement"}]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Adds a list of new Judgement"
      :capabilities :create-judgement
      :login login
      (ok (map (fn [judgement]
                 (-> (flows/create-flow :entity-type :judgement
                                        :realize-fn realize-judgement
                                        :store-fn #(create-judgement @judgement-store %)
                                        :login login
                                        :entity judgement)
                     :id))
               judgements)))))
