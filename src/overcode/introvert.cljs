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

(declare ->js)

(defn seq->js
  "Converts a seq-like value into a JS Array."
  ([obj flat visited]
   (if-let [was-visited (@visited obj)]
     (if flat
       "Circular Seq"
       was-visited)
     (let [out (array)]
       (swap! visited assoc obj out)
       (doseq [elem obj]
         (.push out (->js elem flat visited)))
       out)))

  ([obj flat] (seq->js obj flat (atom {})))
  ([obj] (seq->js obj false (atom {}))))

(defn map->js
  "Converts a map-like value into a JS Object."
  ([obj flat visited]
   (if-let [was-visited (@visited obj)]
     (if flat
       "Circular Map"
       was-visited)
     (let [out (js-obj)]
       (swap! visited assoc obj out)
       (doseq [[key val] obj]
         (aset out (->js key flat visited) (->js val flat visited)))
       out)))

  ([obj flat] (map->js obj flat (atom {})))
  ([obj] (map->js obj false (atom {}))))

(defn ns->js
  "Converts a namespace obj into a JS Object."
  ([obj flat visited]
   (if-let [was-visited (@visited obj)]
     (if flat
       "Circular NS"
       was-visited)
     (let [out (js-obj)]
       (swap! visited assoc obj out)
       (doseq [key (js/Object.keys obj)]
         (let [val (->js (aget obj key) flat visited)]
           (aset out key val)))
       out)))

  ([obj flat] (ns->js obj flat (atom {})))
  ([obj] (ns->js obj false (atom {}))))

(defn ->js
  "Converts a cljs data structure into a close JS approximation."
  ([obj flat visited]
   (cond
     (atom? obj)         (->js @obj flat visited)
     (keyword? obj) (name obj)
     (symbol? obj)  (name obj)
     (map? obj)     (map->js obj flat visited)
     (seq? obj)     (seq->js obj flat visited)
     (set? obj)     (seq->js obj flat visited)
     (vector? obj)  (seq->js obj flat visited)
     (obj? obj)     (ns->js obj flat visited)
     :else          obj))

  ([obj flat] (->js obj flat (atom {})))
  ([obj] (->js obj false (atom {}))))


;;***************************************************************************
;; deep=
;;***************************************************************************
(declare deep=)

(defn deep-arr= [val1 val2 visited]
  (and (= (count val1) (count val2))
       (every? identity (map #(deep= % %2 visited) val1 val2))))

(defn deep-obj= [val1 val2 visited]
  (and (= (obj-size val1) (obj-size val2))
       (deep-arr= (js/Object.keys val1)
                  (js/Object.keys val2)
                  visited)
       (every? #(deep= (aget val1 %) (aget val2 %) visited)
               (js/Object.keys val1))))

(defn deep=
  "Compares JS values for equality by value instead of by reference."
  ([val1 val2 visited]
   (cond
    (and (arr? val1) (arr? val2)) (deep-arr= val1 val2 visited)
    (and (obj? val1) (obj? val2)) (deep-obj= val1 val2 visited)
    :else (= val1 val2)))

  ([val1 val2] (deep= val1 val2 (atom {}))))
