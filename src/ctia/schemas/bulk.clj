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
            [schema.core :as s]))

(s/defschema StoredBulk
  {:actor          [actor/StoredActor]
   :campaign       [campaign/StoredCampaign]
   :coa            [coa/StoredCOA]
   :exploit-target [et/StoredExploitTarget]
   :feedback       [feedback/StoredFeedback]
   :incident       [incident/StoredIncident]
   :indicator      [indicator/StoredIndicator]
   :judgement      [judgement/StoredJudgement]
   :sighting       [sighting/StoredSighting]
   :ttp            [ttp/StoredTTP]})

(s/defschema BulkRefs
  {:actor          [c/Reference]
   :campaign       [c/Reference]                  
   :coa            [c/Reference]
   :exploit-target [c/Reference]
   :feedback       [c/Reference]
   :incident       [c/Reference]
   :indicator      [c/Reference]
   :judgement      [c/Reference]
   :sighting       [c/Reference]
   :ttp            [c/Reference]})

(s/defschema NewBulk
  {:actor          [actor/NewActor]
   :campaign       [campaign/NewCampaign]
   :coa            [coa/NewCOA]
   :exploit-target [et/NewExploitTarget]
   :feedback       [feedback/NewFeedback]
   :incident       [incident/NewIncident]
   :indicator      [indicator/NewIndicator]
   :judgement      [judgement/NewJudgement]
   :sighting       [sighting/NewSighting]
   :ttp            [ttp/NewTTP]})
