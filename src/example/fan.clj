(ns example.simple
  (:require [clojure.core.async :as async :refer :all]))

;; http://talks.golang.org/2012/concurrency.slide#20
(defn boring [msg c]
  (go (doseq [i (iterate inc 0)]
        (>! c (str msg " " i))
        (Thread/sleep (rand 1000)))))

(defn runner-20 []
  (let [c (chan)
        _ (boring "boring!" c)]
    (doseq [i (range 5)]
      (prn (str "You say: " (<!! c))))
    (prn "You're boring; I'm leaving.")))

;; http://talks.golang.org/2012/concurrency.slide#26
(defn boring-2 [msg]
  (let [c (chan)]
    (go (doseq [i (iterate inc 0)]
          (>! c (str msg " " i))
          (Thread/sleep (rand 1000))))
    c))

(defn runner-26 []
  (let [joe (boring-2 "Joe")
        ann (boring-2 "Ann")]
    (doseq [i (range 5)]
      (prn (<!! joe))
      (prn (<!! ann))))
  (prn "You're both boring; I'm leaving."))

;; http://talks.golang.org/2012/concurrency.slide#27
(defn fan-in [input1 input2]
  (let [c (chan)]
    (do
      (go (while true
            (>! c (<! input1))))
      (go (while true
            (>! c (<! input2)))))
    c))

(defn runner-27 []
  (let [c (fan-in (boring-2 "Joe")
                  (boring-2 "Ann"))]
    (doseq [i (range 10)]
      (prn (<!! c)))
    (prn "You're both boring; I'm leaving.")))
