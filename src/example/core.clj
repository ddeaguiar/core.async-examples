(ns example.core
  (:require [clojure.core.async :as async :refer :all])
  (:gen-class))

(defn requestor-fn [work-channel]
  (go (let [c (chan)]
        (do (Thread/sleep 500)
            (>! work-channel {:fn + :c c})
            (let [result (<! c)]
              (prn "Obtained Result: " result))))))

(comment
  "A sample request"
  {:fn #(+ 1 1) :c (chan)}

  "A sample worker"
  {:requests (chan) :pending 0 :index 0}

  "A sample balancer"
  "Perhaps pool should be externalized..."
  {pool [] :done (chan)})

(defn work-fn [worker done-channel]
  (go (while true
        (let [requests (:requests worker)
              req (<! requests)
              result-channel (:c req)
              req-fn (:fn req)]

          ;; do the work
          (>! result-channel (req-fn))

          ;; state that your done
          (>! done-channel worker)))))

(defn dispatch [balancer req]
  (let [pool (:pool balancer)
        worker (reduce (fn [w1 w2] (min (:pending w1)
                                       (:pending w2)))
                       pool)
        requests (:requests worker)
        pending (:pending worker)]

    ;; queue the work
    (<! requests req)

    ;; TODO: Update worker's pending work count. Is this needed?
    ;; Ideally could count number of pending requests

    ;; TODO: Update worker's position in pool.
    ;; Is this necessary?
    ))

(defn completed [ balancer worker]
  (let [pool (:pool balancer)
        pending (:pending worker)]

    ;; TODO: decrement worker pending counter. Is this needed?

    ;; TODO: remove worker from pool. Is this needed?

    ;; TODO: re-add worker to pool (based on new pending counter). Is this needed?
    )
  )

(defn balance-fn [balancer work-channel]
  (go (while true
        (let [balancer-channel (:done balancer)
              [v c] (alts! [work-channel balancer-channel])]
          (if (= c work-channel)
            ;; do work
            (dispatch v)

            ;; worker is done
            (completed v)
            )))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
