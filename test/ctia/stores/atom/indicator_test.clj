(ns ctia.stores.atom.indicator-test
  (:require [ctia.stores.atom.indicator :as sut]
            [clojure.test :refer [is deftest testing] :as t]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [schema-generators.generators :as g]
            [ctia.schemas.indicator :refer [StoredIndicator
                                            format-indicator-query]]
            [ctia.schemas.sighting :refer [StoredSighting]]))

(deftest handle-list-indicators-test
  (let [k :negate
        v true
        indicators (->> (g/sample 20 StoredIndicator)
                        (map #(update-in % [:id] str "ind-"))
                        (map #(assoc % k v)))
        store (->> indicators
                   (map (fn [x] {(:id x) x}))
                   (reduce into {})
                   atom)]
    (testing "Empty search"
      (is (empty? (sut/handle-list-indicators store []))))
    (testing "basic search"
      (is (= (set (vals @store))
             (set (sut/handle-list-indicators
                   store
                   (format-indicator-query {k v}))))))))


(deftest match?-test
  (testing "Some simple examples"
    (is (not (sut/match? "this is a title" #{})))
    (is (sut/match? "this is a title" #{"TiTlE"}))
    (is (sut/match? "this is a TiTle" #{"title"}))
    (is (not (sut/match? "this is a TiTle" #{"title" "foo"})))
    (is (sut/match? "this is a TiTle" #{"a "}))
    (is (sut/match? "this is a TiTle" #{" a,"}))
    (is (not (sut/match? "this is a TiTle" #{"i."})))
    (is (sut/match? "this is a TiTle" #{"A"})))
  (testing "Some tricky examples"
    (is (sut/match? "a→b" #{"→"}))
    (is (sut/match? "a→b" #{"a"}))
    (is (sut/match? "a→b" #{"b"}))
    (is (not (sut/match? "a→" #{"a→b"})))))
