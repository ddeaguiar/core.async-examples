(ns example.core
  (:require [clojure.core.async :as async :refer :all]
            [example.simple :as s]
            [example.balancer :as b]))

(comment
  ;; Kick of 10 workers to process work.
  ;; work will be output to the console.
  (s/runner 10)

  ;; Kick off a balancer with 10 workers in the pool.
  (b/runner 5))
