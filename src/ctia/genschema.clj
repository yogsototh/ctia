(ns ctia.genschema
  (:gen-class)
  (:require [schema-viz.core :as svc]
            [schema.core :as s]
            ;; [ctia.schemas.common :as common]
            ;; [ctia.schemas.actor :as actor]
            ;; [ctia.schemas.campaign :as campaign]
            ;; [ctia.schemas.coa :refer :as coa]
            ;; [ctia.schemas.exploi-target :as et]
            ;; [ctia.schemas.feedback :as feedback]
            ;; [ctia.schemas.identity :as identity]
            ;; [ctia.schemas.indicator :as indicator]
            ;; [ctia.schemas.judgement :as judgement]
            ;; [ctia.schemas.relationships :as relationships]
            ;; [ctia.schemas.sighting :as sightings]
            ;; [ctia.schemas.ttps :as ttps]
            ;; [ctia.schemas.verdict :as verdict]
            ;; [ctia.schemas.vocabularies :as vocabularies]
            ))
(s/defschema TestSchema
  {:a s/Str
   :b s/Num})

(defn -main []
  (svc/save-schemas "doc/img/schema.png")
  (println "Generated: doc/img/schema.png"))

