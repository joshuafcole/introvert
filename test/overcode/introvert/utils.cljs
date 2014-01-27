(ns overcode.introvert.test.utils
  (:require [overcode.introvert :as introvert]
            [cemerick.cljs.test :as test])
  (:require-macros [cemerick.cljs.test :refer (is deftest)]))

(defn is=
  "Compares JS values using deep=."
  ([val1 val2] (is= val1 val2 nil))
  ([val1 val2 msg]
   (is (introvert/deep= val1 val2) msg)))

(defn is-not=
  "Compares JS values using deep=, negated."
  ([val1 val2] (is-not= val1 val2 nil))
  ([val1 val2 msg]
   (is (not (introvert/deep= val1 val2)) msg)))
