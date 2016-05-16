(ns ctia.http.routes.pagination-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer [join-fixtures testing use-fixtures]]
            [ctia.domain.id :as id]
            [ctia.properties :refer [properties]]
            [ctia.test-helpers
             [auth :refer [all-capabilities]]
             [core :as helpers]
             [fake-whoami-service :as whoami-helpers]
             [http :refer [assert-post]]
             [pagination :refer [pagination-test]]
             [store :refer [deftest-for-each-store]]]
            [ctia.test-helpers.generators.schemas :as gs]
            [ring.util.codec :refer [url-encode]]
            [ctia.lib.url :as url]))

(use-fixtures :once (join-fixtures [helpers/fixture-schema-validation
                                    helpers/fixture-properties:clean
                                    whoami-helpers/fixture-server]))

(use-fixtures :each whoami-helpers/fixture-reset-state)

(def ->long-id
  "Fn to convert indicator short IDs into long IDs"
  (id/long-id-factory :indicator
                      #(get-in @properties [:ctia :http :show])))

(deftest-for-each-store ^:slow test-pagination-lists
  "generate an observable and many records of all listable entities"
  (helpers/set-capabilities! "foouser" "user" all-capabilities)
  (whoami-helpers/set-whoami-response "45c1f5e3f05d0" "foouser" "user")

  (testing "with pagination test setup"
    (let [observable {:type "ip" :value "1.2.3.4"}
          indicators (->> (gs/sample-by-kw 5 :new-indicator)
                          (map #(assoc % :title "test")))
          created-indicators (doall (map #(assert-post "ctia/indicator" %)
                                         indicators))
          indicator-rels (map (fn [{:keys [id]}]
                                {:indicator_id (->long-id id)})
                              created-indicators)
          judgements (->> (gs/sample-by-kw 5 :new-judgement)
                          (map #(assoc %
                                       :observable observable
                                       :disposition 5
                                       :disposition_name "Unknown"
                                       :indicators indicator-rels)))
          sightings (->> (gs/sample-by-kw 5 :new-sighting)
                         (map #(-> (assoc %
                                          :observables [observable]
                                          :indicators indicator-rels)
                                   (dissoc % :relations))))
          route-pref (str "ctia/" (:type observable) "/" (:value observable))]

      (doseq [sighting sightings]
        (assert-post "ctia/sighting" sighting))
      (doseq [judgement judgements]
        (assert-post "ctia/judgement" judgement))

      (testing "test paginated lists responses"
        (pagination-test (str route-pref "/indicators")
                         {"api_key" "45c1f5e3f05d0"}
                         [:id :title])
        (pagination-test (str "/ctia/indicator/title/"
                              (-> indicators first :title))
                         {"api_key" "45c1f5e3f05d0"}
                         [:id :title])
        (pagination-test (str "/ctia/indicator/"
                              (-> created-indicators first :id url/encode)
                              "/sightings")
                         {"api_key" "45c1f5e3f05d0"}
                         [:id :timestamp :confidence])
        (pagination-test (str route-pref "/sightings")
                         {"api_key" "45c1f5e3f05d0"}
                         [:id :timestamp :confidence])
        (pagination-test (str route-pref "/judgements")
                         {"api_key" "45c1f5e3f05d0"}
                         [:id :disposition :priority :severity :confidence])))))
