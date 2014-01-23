(ns overcode.introvert.test
  (:require [overcode.introvert :as introvert]
            [cemerick.cljs.test :as test])
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing test-var)]))

;;; Test ->js functions

(defn identity->js [v]
  (is (introvert/->js v) v))

(deftest str->js
  (identity->js "hello!"))

(deftest num->js
  (identity->js 7))

(deftest array->js
  (identity->js (array 1 3 7)))

(deftest obj->js
  (identity->js (js-obj "foo" 3 "bar" "hi")))
