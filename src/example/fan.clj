(ns example.simple
  (:require [clojure.core.async :as async :refer :all]))

;; http://talks.golang.org/2012/concurrency.slide#20
(defn boring [msg c]
  (go (doseq [i (iterate inc 0)]
        (>! c (str msg " " i))
        (Thread/sleep (rand 1000)))))

(defn runner []
  (let [c (chan)
        _ (boring "boring!" c)]
    (doseq [i (range 5)]
      (prn (str "You say: " (<!! c))))
    (prn "You're boring; I'm leaving.")))
