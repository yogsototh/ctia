(ns ctia.schemas.indicator-test
  (:require [ctia.schemas.indicator :as sut]
            [schema-generators.generators :as g]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [schema.core :as s]
            [clojure.test :refer [is testing] :as t]))

(defspec realize-indicator-test
  (prop/for-all [isq (g/generator sut/NewIndicator)]
    (s/validate sut/StoredIndicator (sut/realize-indicator isq "id" "login"))))

(defspec format-indicator-query-test
  (prop/for-all [isq (g/generator sut/IndicatorSwaggerQuery)]
    (s/validate sut/IndicatorQuery (sut/format-indicator-query isq))))
