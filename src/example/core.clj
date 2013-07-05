(ns example.core
  (:require [clojure.core.async :as async :refer :all])
  (:gen-class))

(comment
  "Work is represented by a map:"

  {:x 1 :y 2}

  ":z represents the output of the work done based on :x and :y"

  {:x 1 :y 2 :z 2}
  )

(defn worker
  "A worker go routine."
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
    (<!! (go (doseq [_ r] (prn (<! out)))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
