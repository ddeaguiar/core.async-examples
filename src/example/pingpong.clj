(ns example.pingpong
  (:require [clojure.core.async :as async :refer :all]))

;; From Advanced Go Concurrency Patterns
;; http://talks.golang.org/2013/advconc.slide#6

(defn player [name table]
  (go (while true
        (let [ball (<! table)
              ball (inc ball)]
          (prn (str name " " ball))
          (Thread/sleep 100)
          (>! table ball)))))

(defn runner []
  (let [table (chan)
        ball 0
        player-1 (player "ping" table)
        player-2 (player "pong" table)]
    (>!! table ball)
    (Thread/sleep 1000)
    (<!! table)
    (prn "Done.")))

(comment
  (runner))
