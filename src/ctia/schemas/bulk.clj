(ns ctia.schemas.bulk
  (:require [ctia.schemas
             [actor :as actor]
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
            [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema StoredBulk
  (st/optional-keys
   {:actors          [actor/StoredActor]
    :campaigns       [campaign/StoredCampaign]
    :coas            [coa/StoredCOA]
    :exploit-targets [et/StoredExploitTarget]
    :feedbacks       [feedback/StoredFeedback]
    :incidents       [incident/StoredIncident]
    :indicators      [indicator/StoredIndicator]
    :judgements      [judgement/StoredJudgement]
    :sightings       [sighting/StoredSighting]
    :ttps            [ttp/StoredTTP]}))

(s/defschema BulkRefs
  (st/optional-keys
   {:actors          [c/Reference]
    :campaigns       [c/Reference]                  
    :coas            [c/Reference]
    :exploit-targets [c/Reference]
    :feedbacks       [c/Reference]
    :incidents       [c/Reference]
    :indicators      [c/Reference]
    :judgements      [c/Reference]
    :sightings       [c/Reference]
    :ttps            [c/Reference]}))

(s/defschema NewBulk
  (st/optional-keys
   {:actors          [actor/NewActor]
    :campaigns       [campaign/NewCampaign]
    :coas            [coa/NewCOA]
    :exploit-targets [et/NewExploitTarget]
    :feedbacks       [feedback/NewFeedback]
    :incidents       [incident/NewIncident]
    :indicators      [indicator/NewIndicator]
    :judgements      [judgement/NewJudgement]
    :sightings       [sighting/NewSighting]
    :ttps            [ttp/NewTTP]}))
