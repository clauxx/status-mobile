(ns status-im.ethereum.resolver
  (:refer-clojure :exclude [name])
  (:require [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [clojure.string :as string]))

(def default-hash "0x0000000000000000000000000000000000000000000000000000000000000000")
(defn contenthash [registry ens-name cb]
  (ens/resolver registry
                ens-name
                #(ens/contenthash % ens-name cb)))

(defn content [registry ens-name cb]
  (ens/resolver registry
                ens-name
                #(ens/content % ens-name cb)))

(defn name [registry ens-name cb]
  (ens/resolver registry
                ens-name
                #(ens/name % ens-name cb)))

(defn pubkey
  [registry ens-name cb]
  {:pre [(ens/is-valid-eth-name? ens-name)]}
  (ens/resolver registry
                ens-name
                #(ens/pubkey % ens-name cb)))

(defn ens-name-parse [contact-identity]
  (when (string? contact-identity)
    (string/lower-case
     (if (ens/is-valid-eth-name? contact-identity)
       contact-identity
       (stateofus/subdomain contact-identity)))))
