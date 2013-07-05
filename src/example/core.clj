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





(comment
  (let [c1 (chan)
        c2 (chan)]
    (go (while true
          (let [[v ch]  (alts!! c1 c2)]
            (println "Read" v "from" ch))))
    (go (>! c1 "hi"))
    (go (>! c2 "there"))))

(comment
  (defn worker [in out]
    (go (loop [x (<! in)]
          (if (nil? x)
            (prn "Done")
            (do  (>! out (* 2 x))
                 (recur (<! in)))))))

  (def in-c (chan))
  (def out-c (chan))

  (defn exec [c]
    (go (loop [x (<! c)
               acc []]
          (println "Starting " x)
          (if (nil? x)
            (println acc)
            (recur (<! c)
                   (conj acc x)))))))

(comment
  (go (doseq [i (range 10)]
        (>! in-c i)))

  (go (doseq [_ (range 10)]
        (println (<! in-c))))

  (exec in-c)
  )


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
