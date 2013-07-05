(ns example.core
  (:require [clojure.core.async :as async :refer :all])
  (:gen-class))


(comment

  (defn requestor-fn [work-channel]
    (go (let [c (chan)]
          (while true
            (do (Thread/sleep 500)
                (>! work-channel {:fn + :c c})
                (let [result (<! c)]
                  (prn "Obtained Result: " result)))))))

  (def request {:fn #(+ 1 1) :c (chan)})

  (def worker {:requests (chan) :pending 0 :index 0})

  (defn work-fn [worker done-channel]
    (go (while true
          (let [req (<! (:requests worker))]
            (>! (:c req) ((:fn req)))
            (>! done-channel worker)))))

  (def balancer :pool [] :done (chan))

  (defn balance-fn [balancer work-channel]
    (go (while true
          (let [balancer-channel (:done balancer)
                [v c] (alts! [work-channel balancer-channel])]
            (if (= c work-channel)
              (dispatch v)
              (completed v))))))

  (defn dispatch [balancer req]
    (let [pool (:pool balancer)
          worker (reduce (fn [w1 w2] (min (:pending w1)
                                         (:pending w2)))
                         pool)
          requests (:requests worker)
          pending (:pending worker)]
      (<! requests req)
      ;; broken because worker is not removed from pool.
      ;; pool should probably be implemented as an atom.
      (assoc worker :pending (inc pending))
      (conj pool worker)))

  (defn completed [ balancer worker]
    (let [pool (:pool balancer)
          pending (:pending worker)]
      ;; decrement worker pending counter
      ;; remove worker from pool
      ;; re-add worker to pool (based on new pending counter)
    )
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
