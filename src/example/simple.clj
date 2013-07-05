(ns example.simple
  (:require [clojure.core.async :as async :refer :all]))

(comment
  ;; See Rob Pike's Concurrency is not Parallelism talk for
  ;; the basis of this example (http://talks.golang.org/2012/waza.slide#39)

  ;; Work is represented by a map
  {:x 1 :y 2}

  ;; :z represents the output of the work done based on :x and :y
  {:x 1 :y 2 :z 2})

(defn worker
  "A worker go block."
  [in out]
  (go (while true
        (let [w (<! in)
              x (:x w)
              y (:y w)]

          ;; fake work time
          (Thread/sleep (rand 1000))

          ;; do the work
          (->> (* x y)
              (assoc w :z)
              (>! out))))))

(defn runner
  "Launcher which spawns n workers. Outputs the work as it is completed."
  [n]
  ( let [in  (chan)
         out (chan)
         r (range 1 100)
         num-workers (range n)]

    ;; spawn workers
    (doseq [_ num-workers] (worker in out))

    ;; send work in
    (go (doseq [i r] (>! in
                         {:x i :y (+ i 1)})))
    ;; receive work done
    (doseq [_ r] (prn (<!! out)))))
