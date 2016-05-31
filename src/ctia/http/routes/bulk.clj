(ns ctia.http.routes.bulk
  (:require [compojure.api.sweet :refer :all]
            [clojure.string :as str]
            [ctia.flows.crud :as flows]
            [ctia.schemas
             [actor :as actor]
             [bulk :refer [BulkRefs NewBulk StoredBulk]]
             [campaign :as campaign]
             [coa :as coa]
             [common :as c]
             [exploit-target :as et]
             [feedback :as feedback]
             [incident :as incident]
             [indicator :as indicator]
             [judgement :as judgement]
             [sighting :as sighting]
             [ttp :as ttp]]
            [ctia.store :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [clojure.tools.logging :as log]))

(defn singular [k]
  "remove the last s of a keyword see test for an example."
  (-> k
      name
      (str/replace #"s$" "")
      keyword))

(defn realize [k]
  "return the realize function provided an entity key name"
  (condp = k
    :actor          actor/realize-actor
    :campaign       campaign/realize-campaign
    :coa            coa/realize-coa
    :exploit-target et/realize-exploit-target
    :feedback       feedback/realize-feedback
    :incident       incident/realize-incident
    :indicator      indicator/realize-indicator
    :judgement      judgement/realize-judgement
    :sighting       sighting/realize-sighting
    :ttp            ttp/realize-ttp))

(defn create-fn [k]
  "return the create function provided an entity key name"
  (condp = k
    :actor          #(create-actor @actor-store %)
    :campaign       #(create-campaign @campaign-store %)
    :coa            #(create-coa @coa-store %)
    :exploit-target #(create-exploit-target @exploit-target-store %)
    :feedback       #(create-feedback @feedback-store %)
    :incident       #(create-incident @incident-store %)
    :indicator      #(create-indicator @indicator-store %)
    :judgement      #(create-judgement @judgement-store %)
    :sighting       #(create-sighting @sighting-store %)
    :ttp            #(create-ttp @ttp-store %)))

(defn read-fn [k]
  "return the create function provided an entity key name"
  (condp = k
    :actor          #(read-actor @actor-store %)
    :campaign       #(read-campaign @campaign-store %)
    :coa            #(read-coa @coa-store %)
    :exploit-target #(read-exploit-target @exploit-target-store %)
    :feedback       #(read-feedback @feedback-store %)
    :incident       #(read-incident @incident-store %)
    :indicator      #(read-indicator @indicator-store %)
    :judgement      #(read-judgement @judgement-store %)
    :sighting       #(read-sighting @sighting-store %)
    :ttp            #(read-ttp @ttp-store %)))

(defn create-entities
  "Create many entities provided their type and returns a list of ids"
  [entities entity-type login]
  (->> entities
       (map #(try (flows/create-flow
                   :entity-type entity-type
                   :realize-fn (realize entity-type)
                   :store-fn (create-fn entity-type)
                   :login login
                   :entity %)
                  (catch Exception e
                    (do (log/error (pr-str e))
                        nil))))
       (map :id)))

(defn read-entities
  "Retrieve many entities of the same type provided their ids and common type"
  [ids entity-type]
  (let [read-entity (read-fn entity-type)]
    (->> ids
         (map (fn [id] (try (read-entity id)
                            (catch Exception e
                              (do (log/error (pr-str e))
                                  nil))))))))

(defn gen-bulk-from-fn
  "Kind of fmap but adapted for bulk

  ~~~~.clojure
  (gen-bulk-from-fn f {k [v1 ... vn]} args)
  ===> {k (map #(apply f % (singular k) args) [v1 ... vn])}
  ~~~~
  "
  [func bulk & args]
  (reduce (fn [acc entity-type]
            (assoc acc
                   entity-type
                   (apply func
                          (get bulk entity-type)
                          (singular entity-type)
                          args)))
          {}
          (keys bulk)))

(defroutes bulk-routes
  (context "/bulk" []
    :tags ["Bulk"]
    (POST "/" []
      :return BulkRefs
      :body [bulk NewBulk {:description "a new Bulk object"}]
      :header-params [api_key :- (s/maybe s/Str)]
      :summary "Adds a lot of new entities in only one HTTP call"
      :capabilities #{:create-actor
                      :create-campaign
                      :create-coa
                      :create-exploit-target
                      :create-feedback
                      :create-incident
                      :create-indicator
                      :create-judgement
                      :create-sighting
                      :create-ttp}
      :login login
      (ok (gen-bulk-from-fn create-entities bulk login)))
    (GET "/" []
      :return (s/maybe StoredBulk)
      :summary "Gets many entities at once"
      :query-params [{actors          :- [c/Reference] []}
                     {campaigns       :- [c/Reference] []}
                     {coas            :- [c/Reference] []}
                     {exploit-targets :- [c/Reference] []}
                     {feedbacks       :- [c/Reference] []}
                     {incidents       :- [c/Reference] []}
                     {indicators      :- [c/Reference] []}
                     {judgements      :- [c/Reference] []}
                     {sightings       :- [c/Reference] []}
                     {ttps            :- [c/Reference] []}]
      :header-params [api_key :- (s/maybe s/Str)]
      :capabilities #{:read-actor
                      :read-campaign
                      :read-coa
                      :read-exploit-target
                      :read-feedback
                      :read-incident
                      :read-indicator
                      :read-judgement
                      :read-sighting
                      :read-ttp}
      (let [bulk (into {} (filter (comp not empty? second)
                                  {:actors          actors
                                   :campaigns       campaigns
                                   :coas            coas
                                   :exploit-targets exploit-targets
                                   :feedbacks       feedbacks
                                   :incidents       incidents
                                   :indicators      indicators
                                   :judgements      judgements
                                   :sightings       sightings
                                   :ttps            ttps}))]
        (ok (gen-bulk-from-fn read-entities bulk))))))
