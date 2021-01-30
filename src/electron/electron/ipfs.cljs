(ns electron.ipfs
  (:require ["ipfs-core" :as IPFS]
            ["peer-id" :as PeerId]
            ["libp2p-mdns" :as MulticastDNS]
            ["buffer" :refer [Buffer]]
            ["libp2p/src/pnet" :as Protector]
            ["yjs" :as Y]
            [promesa.core :as p]
            [clojure.string]))

(.extend Y (js/require "y-ipfs-connector"))
(.extend Y (js/require "y-memory"))
(.extend Y (js/require "y-array"))
(.extend Y (js/require "y-text"))

(defonce ipfs (atom nil))

(defn start
  []
  (let [opt {:start false
             ;;:pass "logseqlogseq"
             :repo "./.logseq_ipfs"
             :init {:algorithm "ed25519"}
             :config {:Addresses {:Swarm ["/ip4/0.0.0.0/tcp/9527"]}}
             :libp2p {
                      :modules {:peerDiscovery [MulticastDNS]
                                :connProtector
                                (new Protector
                                     (.from Buffer
                                            "/key/swarm/psk/1.0.0/\n/base16/\ndffb7e3135399a8b1612b2aaca1c36a3a8ac2cd0cca51ceeb2ced87d308cac6d"))
                                }
                      :config {:peerDiscovery {:mdns {:enabled true
                                                      :serviceTag "logseq.local"}}}}}]
    (p/let [ipfs-inst (IPFS/create (clj->js opt))
            _ (.clear (.-bootstrap ^js ipfs-inst))
            _ (.start ipfs-inst)]
           (reset! ipfs ipfs-inst)
           (js/console.error "ipfs started"))))

(defn start-y
  []
  (-> (Y (clj->js {:db {:name "memory"}
                   :connector {:name "ipfs"
                               :ipfs @ipfs
                               :room "logseq-test"}
                   ;;:sourceDir ""
                   :share {:textarea "Text"}}))
      (.then (fn [y]
               (-> (.. ^js y -share -textarea)
                   (.bind ))))))

(defn stop
  []
  (-> (.stop @ipfs)
      (.then #(js/console.log "ipfs stopped."))))




(comment

  (+ 4 3)




  (-> @ipfs (.-swarm) (.addrs)
      (.then #(js/console.log %)))


  (->> (get-all-addresses)
       (map #(.isThinWaistAddress ^js %)))

(-> @ipfs )


  (start)

  (-> (start)
      (.then #(js/console.log "start")))

  (stop)

  (clojure.repl/pst)

  (-> (.start @ipfs)
      (p/then #(js/console.log "ipfs start")))


  (-> @ipfs
      (.then #(-> (.libp2p %)
                  (.then (fn [v]
                           (js/console.log (.-id v))))))))