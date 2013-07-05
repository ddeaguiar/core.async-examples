(ns example.core
  (:require [clojure.core.async :as async :refer :all]
            [example.simple :as s]))

(comment
  ;; Kick of 10 workers to process work.
  ;; work will be output to the console.
  (s/runner 10))
