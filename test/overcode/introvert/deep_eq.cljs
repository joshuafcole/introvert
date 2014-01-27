(ns overcode.introvert.test.deep=
  (:require [overcode.introvert :as introvert]
            [overcode.introvert.test.utils :refer [is= is-not=]])
  (:require-macros [cemerick.cljs.test :refer (is deftest)]))

;;***************************************************************************
;; deep= Primitives
;;***************************************************************************
(deftest str-deep=
  (is= "hello!" "hello!"))

(deftest num-deep=
  (is= 7 7))

(deftest str-not-deep=
  (is-not= "hello!" "ciao"))

(deftest num-not-deep=
  (is-not= 7 3))

;;***************************************************************************
;; deep= Sequences
;;***************************************************************************
(deftest arr-deep=
  (is= (array 1 3 7) (array 1 3 7)))

(deftest seq-deep=
  (is= [1 3 7] [1 3 7]))

(deftest arr-deep-deep=
  (is= (array 1 3 (array 6 8)) (array 1 3 (array 6 8))))

(deftest arr-not-deep=
  (is-not= (array 1 3 7) (array 1 2 6)))

(deftest seq-not-deep=
  (is-not= [1 3 7] [1 2 6]))

(deftest arr-deep-not-deep=
  (is-not= (array 1 3 (array 6 8)) (array 1 3 (array 7 9))))

;;***************************************************************************
;; deep= Maps
;;***************************************************************************
(deftest obj-deep=
  (is= (js-obj "foo" 3 "bar" "hi")
                   (js-obj "foo" 3 "bar" "hi")))

(deftest map-deep=
  (is= {"foo" 3 "bar" "hi"}
                   {"foo" 3 "bar" "hi"}))

(deftest obj-deep-deep=
  (is= (js-obj "foo" 3 "bar" (array 7 9))
                   (js-obj "foo" 3 "bar" (array 7 9))))

(deftest obj-key-not-deep=
  (is-not= (js-obj "foo" 3 "bar" "hi")
                   (js-obj "foo" 3 "baz" "hi")))

(deftest obj-val-not-deep=
  (is-not= (js-obj "foo" 3 "bar" "hi")
                   (js-obj "foo" 3 "bar" "bye")))

(deftest map-key-not-deep=
  (is-not= {"foo" 3 "bar" "hi"}
                   {"foo" 3 "baz" "hi"}))

(deftest map-val-not-deep=
  (is-not= {"foo" 3 "bar" "hi"}
                   {"foo" 3 "bat" "bye"}))

(deftest obj-deep-not-deep=
  (is-not= (js-obj "foo" 3 "bar" (array 7 9))
                   (js-obj "foo" 3 "bar" (array 5 3))))
