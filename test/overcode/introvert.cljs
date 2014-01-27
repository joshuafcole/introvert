(ns overcode.introvert.test
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


;;***************************************************************************
;; ->js Identities
;;***************************************************************************
(let [identity->js
      (fn identity->js [v]
        (is= (introvert/->js v) v))]

  (deftest str->js
    (identity->js "hello!"))

  (deftest num->js
    (identity->js 7))

  (deftest arr->js
    (identity->js (array 1 3 7)))

  (deftest obj->js
    (identity->js (js-obj "foo" 3 "bar" "hi"))))


;;***************************************************************************
;; ->js Sequences
;;***************************************************************************
(let [in  [1 2 "three"]
      out (array 1 2 "three")]

  (deftest IndexedSeq->js
    (is= (introvert/->js (seq in))
         out))

  (deftest LazySeq->js
    (is= (introvert/->js (take 3 in))
         out))

  (deftest PersistentVector->js
    (is= (introvert/->js in)
         out))

  (deftest atomic-seq->js
    (is= (introvert/->js (atom in))
         out)))


;;***************************************************************************
;; ->js Maps
;;***************************************************************************
(let [in  {"one" 1 "two" 2 "three" "three"}
      out (js-obj "one" 1 "two" 2 "three" "three")]
  (deftest PersistentArrayMap->js
    (is= (introvert/->js in)
         out))

  (deftest PersistentHashMap->js
    (is= (introvert/->js (into (hash-map) in))
         out))

  (deftest atomic-map->js
    (is= (introvert/->js (atom in))
         out)))


;;***************************************************************************
;; ->js Recursion
;;***************************************************************************
(deftest RecursiveSeq->js
  (is= (introvert/->js (seq [1 "2" :three [:four 5]]))
       (array 1 "2" "three" (array "four" 5))))

(deftest RecursiveMap->js
  (is= (introvert/->js {:one 1 :two :two :three {:a "A"}})
       (js-obj "one" 1 "two" "two" "three" (js-obj "a" "A"))))


;;***************************************************************************
;; ->js Circular Refs
;;***************************************************************************
(deftest circular-seq->js
  (let [a (atom [1])
        b [9 a]
        a-js (array 1)
        b-js (array 9 a-js)]

    (swap! a conj b)
    (.push a-js b-js)

    (let [result (introvert/->js b)]
      (is (= (count result) (count b-js))))))

(deftest circular-map->js
  (let [a (atom {:1 1})
        b {:9 9 :a a}
        a-js (js-obj "1" 1)
        b-js (js-obj "9" 9 "a" a-js)]

    (swap! a assoc :b b)
    (aset a-js "b" b-js)

    (let [result (introvert/->js b)]
      (is (= (introvert/obj-size result) (introvert/obj-size b-js))))))

(deftest circular-seq->flattened-js
  (let [a (atom [1])
        b [9 a]
        a-js (array 1 "Circular Seq")
        b-js (array 9 a-js)]

    (swap! a conj b)

    (is= (introvert/->js b true)
         b-js)))

(deftest circular-map->flattened-js
  (let [a (atom {:1 1})
        b {:9 9 :a a}
        a-js (js-obj "1" 1 "b" "Circular Map")
        b-js (js-obj "9" 9 "a" a-js)]

    (swap! a assoc :b b)

    (is= (introvert/->js b true)
         b-js)))
