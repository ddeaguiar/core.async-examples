(ns example.balancer
  (:require [clojure.core.async :as async :refer :all]))

;; A more elaborate balancer example loosely based on
;; http://talks.golang.org/2012/waza.slide#45

(defn create-request [fn c]
  {
   ;; operation to be invoked by worker
   :fn fn
   ;; channel the result will be pushed to by the worker
   :response-channel c})

(defn requestor
  "A requestor is represented by function
   that puts work into a work channel."
  [work-channel]
  (go (let [response-channel (chan)
            req (create-request #(do (Thread/sleep (rand 1000))
                                     "Done")
                                response-channel)]
        (>! work-channel req)
        (prn (<! response-channel)))))

(defn create-worker
  "A worker is represented by a channel
   and a function that reads from that channel."
  []
  (let [request-channel (chan)]
    ((fn []
       (go (while true (let [req (<! request-channel)
                             op (:fn req)
                             response-channel (:response-channel req)]
                         (>! response-channel (op)))))))
    request-channel))

(defn round-robin
  "Simple round-robin worker selection."
  [workers]
  (let [w (first @workers)]
    (swap! workers #(rest %))
    w))

(defn balance
  "Balancer function. Distributes work across 'workers' pool based on the
work arriving in the 'work' channel and the 'balancer-algorithm' fn."
  [work-channel workers balancer-algorithm]
  (go (while true
        (let [req (<! work-channel)
              request-channel (balancer-algorithm workers)]
          (>! request-channel req)))))

(defn runner [n]
  (let [workers (atom (cycle (for [i (range n)]
                               (create-worker))))
        work-channel (chan)]
    (balance work-channel workers round-robin)
    (doseq [_ (range 100)] (requestor work-channel)))
  (prn "Started"))
