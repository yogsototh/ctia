(ns ctia.stores.atom.indicator
  (:require [clojure
             [set :as set]
             [string :as string]]
            [ctia.schemas
             [indicator :refer [StoredIndicator]]
             [judgement :refer [StoredJudgement]]]
            [ctia.stores.atom.common :as mc]
            [schema.core :as s]))

(def handle-create-indicator (mc/create-handler-from-realized StoredIndicator))
(def handle-read-indicator (mc/read-handler StoredIndicator))
(def handle-update-indicator (mc/update-handler-from-realized StoredIndicator))
(def handle-delete-indicator (mc/delete-handler StoredIndicator))
(def handle-list-indicators (mc/list-handler StoredIndicator))

(s/defn handle-list-indicators-by-judgements :- (s/maybe [StoredIndicator])
  [indicator-state :- (s/atom {s/Str StoredIndicator})
   judgements :- [StoredJudgement]]
  (let [indicator-ids (some->> (map :indicators judgements)
                               (mapcat #(map :indicator_id %))
                               set)]
    (filter (fn [indicator]
              (clojure.set/subset? #{(:id indicator)} indicator-ids))
            (vals @indicator-state))))

(defn not-word?
  "return true if the string is not a word"
  [txt]
  (re-matches #"^(\p{P}|\p{Z})*$" txt))

(defn normalize
  "Given some text returns a set of normalized words.
  By normalized, we mean that the words are lower-cased and we removed spaces"
  [txt]
  (->> (string/split (string/lower-case txt) #"\b")
       (remove not-word?)
       set))

(defn match?
  "Does the title match all words?"
  [title words]
  (and (not (empty? words))
       (clojure.set/subset?
        (reduce clojure.set/union #{} (map normalize words))
        (normalize title))))

(s/defn handle-list-indicators-by-title-word :- (s/maybe [StoredIndicator])
  [indicator-state :- (s/atom {s/Str StoredIndicator})
   words :- #{s/Str}]
  (filter #(match? (:title %) words) (vals @indicator-state)))
