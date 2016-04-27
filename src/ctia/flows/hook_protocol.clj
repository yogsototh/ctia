(ns ctia.flows.hook-protocol
  "Declare the protocol without any other function around to prevent reload error.
  See https://nelsonmorris.net/2015/05/18/reloaded-protocol-and-no-implementation-of-method.html")

(defprotocol Hook
  "A hook is mainly a function"
  ;; `init` Should be function which could have stateful effect.
  ;; The function will be called during initialization of the application.
  ;; Its return value will be discarded.
  ;;
  ;; Typing : `IO ()`, an IO effect that returns nothing.
  (init [this])
  ;; The Handle function will take
  ;; an object of some type (look at :type value)
  ;; and optionally it could contains a previous object.
  ;; typically for the update hook the `prev-object`
  ;; will contains the previous value of the object
  ;;
  ;; Typing: handle :- Command
  ;;     [object :- Commnad]
  (handle [this command])
  ;; `destroy` Should be function which could have stateful effect.
  ;; It will be called at the shutdown of the application.
  ;; This function will typically be used to free some resources for example.
  ;; Typing: `IO ()`, an IO effect that returns nothing
  (destroy [this]))
