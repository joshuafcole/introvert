(ns overcode.introvert.test.->js
  (:require [overcode.introvert :as introvert]
            [overcode.introvert.test.utils :refer [is= is-not=]])
  (:require-macros [cemerick.cljs.test :refer (is deftest)]))

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
;; ->js NameSpaces
;;***************************************************************************
(deftest ns->js
  (is= (introvert/->js (js-obj "test" (seq [1 "2" :three [:four 5]])))
       (js-obj "test" (array 1 "2" "three" (array "four" 5)))))

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
      (is (= (count (js/Object.keys result)) (count (js/Object.keys b-js)))))))

(deftest circular-seq->flattened-js
  (let [a (atom [1])
        b [9 a]
        a-js (array 1 "Circular")
        b-js (array 9 a-js)]

    (swap! a conj b)

    (is= (introvert/->js b true)
         b-js)))

(deftest circular-map->flattened-js
  (let [a (atom {:1 1})
        b {:9 9 :a a}
        a-js (js-obj "1" 1 "b" "Circular")
        b-js (js-obj "9" 9 "a" a-js)]

    (swap! a assoc :b b)

    (is= (introvert/->js b true)
         b-js)))
