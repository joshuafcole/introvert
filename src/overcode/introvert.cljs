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
  (count (filter #(.hasOwnProperty obj %) (.keys js/Object obj))))

(defn array-append [arr seq]
  "Inserts all elements from a Seq into an existing JS array."
  (doseq [v seq]
    (.push arr v))
  arr)

(defn visited? [visited obj]
  "Check if a collection has been visited. If so, return its JS equivalent."
  (if (contains? @visited obj)
    (get @visited obj)))

(defn visit!
  "Marks potentially linked objects as visited to handle circular refs."
  ([visited obj out]
   (swap! visited assoc obj out)
   out)

  ([visited obj] (visit! visited obj "Circular Ref")))


;;***************************************************************************
;; ->js
;;***************************************************************************

(declare ->js)

(defn seq->js
  "Converts a seq-like value into a JS Array."
  ([obj flat visited]
   (if-let [was-visited (visited? visited obj)]
     (if flat
       "Circular Seq"
       was-visited)
     (let [out (array)]
       (visit! visited obj out)
       (array-append out (map #(->js % flat visited) obj)))))

  ([obj flat] (seq->js obj flat (atom {})))
  ([obj] (seq->js obj false (atom {}))))

(defn map->js
  "Converts a map-like value into a JS Object."
  ([obj flat visited]
   (if-let [was-visited (visited? visited obj)]
     (if flat
       "Circular Map"
       was-visited)
     (let [out (js-obj)]
       (visit! visited obj out)
       (dorun
        (map
         #(let [key (->js (first %)  flat visited)
                val (->js (second %) flat visited)]
            (aset out key val))
         obj))
       out)))

  ([obj flat] (map->js obj flat (atom {})))
  ([obj] (map->js obj false (atom {}))))

(defn ns->js
  "Converts a namespace obj into a JS Object."
  ([obj flat visited]
   (let [was-visited (visited? visited obj)]
     (if was-visited
       (if flat
         "Circular NS"
         was-visited)
       (let [out (js-obj)]
         (visit! visited obj out)
         (dorun
          (map
           #(let [key %
                  val (->js (aget obj %) flat visited)]
              (aset out key val))
           (.keys js/Object obj)))
         out))))

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
(def deep=)

(defn deep-arr=
  ([val1 val2 visited]
   (if (= (count val1) (count val2))
     (every? identity (map #(deep= % %2 visited) val1 val2))
     false)))

(defn deep-obj=
  ([val1 val2 visited]
  (if (and (= (obj-size val1) (obj-size val2))
           (deep-arr= (.keys js/Object val1)
                      (.keys js/Object val2)
                      visited))
    (every? identity
            (map #(deep= (aget val1 %) (aget val2 %) visited)
                 (.keys js/Object val1)))
    false)))

(defn deep=
  "Compares JS values for equality by value instead of by reference."
  ([val1 val2 visited]
   (cond
    (and (arr? val1) (arr? val2)) (deep-arr= val1 val2 visited)
    (and (obj? val1) (obj? val2)) (deep-obj= val1 val2 visited)
    :else (= val1 val2)))

  ([val1 val2] (deep= val1 val2 (atom {}))))
