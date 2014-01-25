(ns overcode.introvert.test
  (:require [overcode.introvert :as introvert]
            [cemerick.cljs.test :as test])
  (:require-macros [cemerick.cljs.test :refer (is deftest)]))

;;***************************************************************************
;; Identities
;;***************************************************************************
(let [identity->js
      (fn identity->js [v]
        (is (introvert/->js v) v))]

  (deftest str->js
    (identity->js "hello!"))

  (deftest num->js
    (identity->js 7))

  (deftest array->js
    (identity->js (array 1 3 7)))

  (deftest obj->js
    (identity->js (js-obj "foo" 3 "bar" "hi"))))


;;***************************************************************************
;; Sequences
;;***************************************************************************
(let [in  [1 2 "three"]
      out (array 1 2 "three")]

  (deftest IndexedSeq->js
    (is (introvert/->js (seq in))
        out))

  (deftest LazySeq->js
    (is (introvert/->js (take 3 in))
        out))

  (deftest PersistentVector->js
    (is (introvert/->js in)
        out))

  (deftest atomic-seq->js
    (is (introvert/->js (atom in))
        out)))


;;***************************************************************************
;; Maps
;;***************************************************************************
(let [in  {"one" 1 "two" 2 "three" "three"}
      out (js-obj "one" 1 "two" 2 "three" "three")]
  (deftest PersistentArrayMap->js
    (is (introvert/->js in)
        out))

  (deftest PersistentHashMap->js
    (is (introvert/->js (hash-map in))
        out))

  (deftest atomic-map->js
    (is (introvert/->js (atom in))
        out)))


;;***************************************************************************
;; Recursion
;;***************************************************************************
(deftest RecursiveSeq->js
  (is (introvert/->js (seq [1 "2" :three [:four 5]]))
      (array 1 "2" "3" (array "four" 5))))

(deftest RecursiveMap->js
  (is (introvert/->js {:one 1 :two :two :three {:a "A"}})
      (js-obj "one" 1 "two" "two" "three" (js-obj "a" "A"))))


;;***************************************************************************
;; Circular Refs
;;***************************************************************************
(deftest circular-seq->js
  (let [a (atom [1])
        b [9 a]
        a-js (array 1)
        b-js (array 9 a-js)]

    (swap! a conj b)
    (.push a-js b)

    (is (introvert/->js b)
        b-js)))

(deftest circular-map->js
  (let [a (atom {:1 1})
        b {:9 9 :a a}
        a-js (js-obj "1" 1)
        b-js (js-obj "9" 9 "a" a-js)]

    (swap! a assoc :b b)
    (aset a-js "b" b)

    (is (introvert/->js b)
        b-js)))

(deftest circular-seq->flattened-js
  (let [a (atom [1])
        b [9 a]
        a-js (array 1 "Circular Seq")
        b-js (array 9 a-js)]

    (swap! a conj b)
    (.push a-js b)

    (is (introvert/->js b true)
        b-js)))

(deftest circular-map->flattened-js
  (let [a (atom {:1 1})
        b {:9 9 :a a}
        a-js (js-obj "1" 1 "Circular Map")
        b-js (js-obj "9" 9 "a" a-js)]

    (swap! a assoc :b b)

    (is (introvert/->js b true)
        b-js)))
