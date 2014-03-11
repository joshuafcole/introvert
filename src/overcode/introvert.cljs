(ns overcode.introvert)

;;***************************************************************************
;; Utilities
;;***************************************************************************
(defn atom? [v]
  (= (type v) Atom))

(defn arr? [v]
  "Returns if an object is a JS Array."
  (= (type v) js/Array))

(defn obj? [v]
  "Returns if an object is a JS Object."
  (= (type v) js/Object))

(defn obj-size [obj]
  (count (filter #(.hasOwnProperty obj %) (js/Object.keys obj))))

;;***************************************************************************
;; ->js
;;***************************************************************************

(defn ->js
  "Converts a cljs data structure into a close JS approximation."
  ([obj flat visited]
   (let [was-visited (@visited obj)]
     (cond
      was-visited
      (if flat
        "Circular"
        was-visited)

      (atom? obj)
      (->js @obj flat visited)

      (or (keyword? obj) (symbol? obj))
      (name obj)

      (map? obj)
      (let [out (js-obj)]
        (swap! visited assoc obj out)
        (doseq [[key val] obj]
          (aset out (->js key flat visited) (->js val flat visited)))
        out)

      (or (seq? obj) (set? obj) (vector? obj))
      (let [out (array)]
        (swap! visited assoc obj out)
        (doseq [elem obj]
          (.push out (->js elem flat visited)))
        out)

      (obj? obj)
      (let [out (js-obj)]
        (swap! visited assoc obj out)
        (doseq [key (js/Object.keys obj)]
          (let [val (aget obj key)]
            (aset out key (->js val flat visited))))
        out)

      :else
      obj)))

  ([obj flat] (->js obj flat (atom {})))
  ([obj] (->js obj false (atom {}))))


;;***************************************************************************
;; deep=
;;***************************************************************************

(defn deep=
  "Compares JS values for equality by value instead of by reference."
  ([val1 val2 visited]
   (cond
    (and (arr? val1) (arr? val2))
    (and (= (count val1) (count val2))
         (every? identity (map #(deep= %1 %2 visited) val1 val2)))

    (and (obj? val1) (obj? val2))
    (and (= (obj-size val1) (obj-size val2))
         (every? identity (map #(deep= %1 %2 visited) (js/Object.keys val1) (js/Object.keys val2)))
         (every? #(deep= (aget val1 %) (aget val2 %) visited) (js/Object.keys val1)))

    :else (= val1 val2)))

  ([val1 val2] (deep= val1 val2 (atom {}))))
