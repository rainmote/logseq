(ns electron.mdns
  (:require ["libp2p-mdns" :as MDNS]
            [electron.ipfs :as ipfs]))

(defonce mdns (atom nil))

(defn handle-peer
  [peer-data]
  (js/console.log peer-data)
  (js/console.log "Found a peer in the local network" (aget peer-data "multiaddrs")))

(defn start
  []
  (let [opt (clj->js {:serviceTag "logseq.local"
             :libp2p {:multiaddrs (ipfs/get-all-addresses)}
             :broadcast true
             :peerId (ipfs/get-peer-id)})]
    (reset! mdns (MDNS. opt))
    (.on ^js @mdns "peer" handle-peer)
    (.start ^js @mdns)
    (js/console.log "mdns started.")))

(defn stop
  []
  (-> (.stop ^js @mdns)
      (.then #(js/console.log "mdns stopped"))))

(comment

  (start)

  (stop)

  (-> @mdns (.-peerMultiaddrs))

  (-> (.-mdns ^js @mdns)
      (.query (clj->js {:questions
                        [{:name "logseq.local"
                          :type "PTR"}]})))

  (ipfs/get-peer-id)
  )