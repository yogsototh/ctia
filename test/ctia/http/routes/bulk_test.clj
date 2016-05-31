(ns ctia.http.routes.bulk-test
  (:refer-clojure :exclude [get])
  (:require
   [ctia.lib.url :refer [encode]]
   [ctia.http.routes.bulk :refer [singular gen-bulk-from-fn]]
   [clojure.test :refer [deftest is testing use-fixtures join-fixtures]]
   [ctia.test-helpers.core :refer [delete get post put] :as helpers]
   [ctia.test-helpers.fake-whoami-service :as whoami-helpers]
   [ctia.test-helpers.store :refer [deftest-for-each-store]]
   [ctia.auth :refer [all-capabilities]]
   [clojure.string :as str]))


(use-fixtures :once (join-fixtures [helpers/fixture-schema-validation
                                    helpers/fixture-properties:clean
                                    whoami-helpers/fixture-server]))

(use-fixtures :each whoami-helpers/fixture-reset-state)

(deftest testing-singular
  (is (= :actor (singular :actors)))
  (is (= :campaign (singular :campaigns)))
  (is (= :coa (singular :coas)))
  (is (= :exploit-target (singular :exploit-targets)))
  (is (= :feedback (singular :feedbacks)))
  (is (= :incident (singular :incidents)))
  (is (= :indicator (singular :indicators)))
  (is (= :judgement (singular :judgements)))
  (is (= :sighting (singular :sightings)))
  (is (= :ttp (singular :ttps))))


(defn mk-new-actor [n]
  {:title (str "actor-" n)
   :description (str "description: actor-" n)
   :actor_type "Hacker"
   :source "a source"
   :confidence "High"
   :associated_actors [{:actor_id "actor-123"}
                       {:actor_id "actor-456"}]
   :associated_campaigns [{:campaign_id "campaign-444"}
                          {:campaign_id "campaign-555"}]
   :observed_TTPs [{:ttp_id "ttp-333"}
                   {:ttp_id "ttp-999"}]
   :valid_time {:start_time #inst "2016-02-11T00:40:48.212-00:00"
                :end_time #inst "2016-07-11T00:40:48.212-00:00"}})

(defn mk-new-campaign [n]
  {:title (str "campaign" n)
   :description "description"
   :campaign_type "anything goes here"
   :intended_effect ["Theft"]
   :indicators [{:indicator_id "indicator-foo"}
                {:indicator_id "indicator-bar"}]
   :attribution [{:confidence "High"
                  :source "source"
                  :relationship "relationship"
                  :actor_id "actor-123"}]
   :related_incidents [{:confidence "High"
                        :source "source"
                        :relationship "relationship"
                        :incident_id "incident-222"}]
   :related_TTPs [{:confidence "High"
                   :source "source"
                   :relationship "relationship"
                   :ttp_id "ttp-999"}]
   :valid_time {:start_time #inst "2016-02-11T00:40:48.212-00:00"
                :end_time #inst "2016-07-11T00:40:48.212-00:00"}})

(deftest testing-gen-bulk-from-fn
  (let [new-bulk {:actors (map mk-new-actor (range 6))
                  :campaigns (map mk-new-campaign (range 6))}]
    (testing "testing gen-bulk-from-fn with 2 args"
      (is (= (gen-bulk-from-fn (fn [lst _] (map (fn [_] :s) lst))
                               new-bulk)
             {:actors [:s :s :s :s :s :s]
              :campaigns [:s :s :s :s :s :s]})))
    (testing "testing gen-bulk-from-fn with 3 args"
      (is (= (gen-bulk-from-fn (fn [lst _ x] (map (fn [_] x) lst))
                               new-bulk
                               :x)
             {:actors [:x :x :x :x :x :x]
              :campaigns [:x :x :x :x :x :x]})))))

(def tst-bulk{:actors (map #(str "actor-" %) (range 6))
              :campaigns (map #(str "campaign-" %) (range 6))})

(defn make-get-query-str-from-bulkrefs
  "Given a BulkRefs returns the string of query-params"
  [bulk-ids]
  (str/join "&"
            (map
             (fn [type]
               (str/join "&"
                         (map (fn [id] (str (encode (name type)) "=" (encode id)))
                              (get-in bulk-ids [type]))))
             [:actors :campaigns])))

(deftest-for-each-store test-bulk-routes
  (helpers/set-capabilities! "foouser" "user" all-capabilities)
  (whoami-helpers/set-whoami-response "45c1f5e3f05d0" "foouser" "user")
  (testing "POST /ctia/bulk"
    (let [new-bulk {:actors (map mk-new-actor (range 10))
                    :campaigns (map mk-new-campaign (range 10))}
          response (post "ctia/bulk"
                         :body new-bulk
                         :headers {"api_key" "45c1f5e3f05d0"})
          bulk-ids (:parsed-body response)]
      (when-not (= 200 (:status response))
        (clojure.pprint/pprint response))
      (is (= 200 (:status response)))
      (doseq [type [:actors :campaigns]]
        (is (= (count (get-in bulk-ids [type]))
               (count (get-in new-bulk [type])))))
      (testing "GET /ctia/bulk"
        (let [resp (get (str "ctia/bulk?"
                             (make-get-query-str-from-bulkrefs bulk-ids))
                        :headers {"api_key" "45c1f5e3f05d0"})]
          (is (= 200 (:status resp)))
          (doseq [k (keys new-bulk)]
            (is (= (get-in new-bulk [k])
                   (map #(dissoc % :created :id :type :modified :owner :tlp :version)
                        (get-in (:parsed-body resp) [k]))))))))))
