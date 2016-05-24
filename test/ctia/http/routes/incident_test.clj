(ns ctia.http.routes.incident-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer [is join-fixtures testing use-fixtures]]
            [ctia.lib.url :as u]
            [ctia.test-helpers
             [auth :refer [all-capabilities]]
             [core :as helpers :refer [delete get post put]]
             [fake-whoami-service :as whoami-helpers]
             [http :refer [api-key]]
             [store :refer [deftest-for-each-store]]]))

(use-fixtures :once (join-fixtures [helpers/fixture-schema-validation
                                    helpers/fixture-properties:clean
                                    whoami-helpers/fixture-server]))

(use-fixtures :each whoami-helpers/fixture-reset-state)

(deftest-for-each-store test-incident-routes
  (helpers/set-capabilities! "foouser" "user" all-capabilities)
  (whoami-helpers/set-whoami-response "45c1f5e3f05d0" "foouser" "user")

  (testing "POST /ctia/incident"
    (let [response (post "ctia/incident"
                         :body {:title "incident"
                                :description "description"
                                :confidence "High"
                                :categories ["Denial of Service"
                                             "Improper Usage"]
                                :valid_time {:start_time "2016-02-11T00:40:48.212-00:00"}
                                :related_indicators [{:confidence "High"
                                                      :source "source"
                                                      :relationship "relationship"
                                                      :indicator_id "indicator-123"}]
                                :related_incidents [{:incident_id "incident-123"}
                                                    {:incident_id "indicent-789"}]}
                         :headers {"api_key" "45c1f5e3f05d0"})
          incident (:parsed-body response)]
      (is (= 200 (:status response)))
      (is (deep=
           {:type "incident"
            :title "incident"
            :description "description"
            :tlp "green"
            :confidence "High"
            :categories ["Denial of Service"
                         "Improper Usage"]
            :valid_time {:start_time #inst "2016-02-11T00:40:48.212-00:00"
                         :end_time #inst "2525-01-01T00:00:00.000-00:00"}
            :related_indicators [{:confidence "High"
                                  :source "source"
                                  :relationship "relationship"
                                  :indicator_id "indicator-123"}]

            :related_incidents [{:incident_id "incident-123"}
                                {:incident_id "indicent-789"}]
            :owner "foouser"}
           (dissoc incident
                   :id
                   :created
                   :modified)))

      (testing "GET /ctia/incident/:id"
        (let [response (get (str "ctia/incident/" (:id incident))
                            :headers {"api_key" "45c1f5e3f05d0"})
              incident (:parsed-body response)]
          (is (= 200 (:status response)))
          (is (deep=
               {:type "incident"
                :title "incident"
                :description "description"
                :tlp "green"
                :confidence "High"
                :categories ["Denial of Service"
                             "Improper Usage"]
                :valid_time {:start_time #inst "2016-02-11T00:40:48.212-00:00"
                             :end_time #inst "2525-01-01T00:00:00.000-00:00"}
                :related_indicators [{:confidence "High"
                                      :source "source"
                                      :relationship "relationship"
                                      :indicator_id "indicator-123"}]
                :related_incidents [{:incident_id "incident-123"}
                                    {:incident_id "indicent-789"}]
                :owner "foouser"}
               (dissoc incident
                       :id
                       :created
                       :modified)))))

      (testing "PUT /ctia/incident/:id"
        (let [{status :status
               updated-incident :parsed-body}
              (put (str "ctia/incident/" (:id incident))
                   :body {:title "updated incident"
                          :description "updated description"
                          :tlp "green"
                          :confidence "Low"
                          :categories ["Denial of Service"
                                       "Improper Usage"]
                          :valid_time {:start_time "2016-02-11T00:40:48.212-00:00"}
                          :related_indicators [{:confidence "High"
                                                :source "another source"
                                                :relationship "relationship"
                                                :indicator_id "indicator-234"}]
                          :related_incidents [{:incident_id "incident-123"}
                                              {:incident_id "indicent-789"}]}
                   :headers {"api_key" "45c1f5e3f05d0"})]
          (is (= 200 status))
          (is (deep=
               {:type "incident"
                :id (:id incident)
                :created (:created incident)
                :title "updated incident"
                :description "updated description"
                :tlp "green"
                :confidence "Low"
                :categories ["Denial of Service"
                             "Improper Usage"]
                :valid_time {:start_time #inst "2016-02-11T00:40:48.212-00:00"
                             :end_time #inst "2525-01-01T00:00:00.000-00:00"}
                :related_indicators [{:confidence "High"
                                      :source "another source"
                                      :relationship "relationship"
                                      :indicator_id "indicator-234"}]
                :related_incidents [{:incident_id "incident-123"}
                                    {:incident_id "indicent-789"}]
                :owner "foouser"}
               (dissoc updated-incident
                       :modified)))))

      (testing "DELETE /ctia/incident/:id"
        (let [response (delete (str "ctia/incident/" (:id incident))
                               :headers {"api_key" "45c1f5e3f05d0"})]
          (is (= 204 (:status response)))
          (let [response (get (str "ctia/incident/" (:id incident))
                              :headers {"api_key" "45c1f5e3f05d0"})]
            (is (= 404 (:status response)))))))))

(deftest-for-each-store test-incident-multi-route
  (helpers/set-capabilities! "foouser" "user" all-capabilities)
  (whoami-helpers/set-whoami-response api-key "foouser" "user")
  (testing "POST /ctia/incidents"
    (let [incidents (map (fn [nb]
                           {:title (str "incident-" nb)
                            :description (str "incident-" nb)
                            :confidence "High"
                            :categories ["Denial of Service" "Improper Usage"]
                            :valid_time {:start_time #inst "2016-02-11T00:40:48.212-00:00"
                                         :end_time #inst "2017-02-11T00:40:48.212-00:00"}})
                      [1 2 3])
          incident-keys (keys (first incidents))
          response (post "ctia/incidents"
                         :body incidents
                         :headers {"api_key" api-key})
          ids (:parsed-body response)
          retrieved-incidents (doall (map #(-> (get (str "ctia/incident/" (u/encode %))
                                                 :headers {"api_key" api-key})
                                            :parsed-body)
                                       ids))]
      (is (= incidents
             (map #(select-keys % incident-keys) retrieved-incidents))))))
