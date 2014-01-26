(ns overcode.introvert)

(def ->js)

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

  ([visited obj] (visit! visited obj true)))

(defn seq->js
  "Converts a seq-like value into a JS Array."
  ([obj flat visited]
   (let [was-visited (visited? visited obj)]
     (if was-visited
       (if flat
         "Circular Seq"
         was-visited)
       (let [out (array)]
         (visit! visited obj out)
         (array-append out (map #(->js % flat visited) obj))))))

  ([obj flat] (seq->js obj flat (atom {})))
  ([obj] (seq->js obj false (atom {}))))

(defn map->js
  "Converts a map-like value into a JS Object."
  ([obj flat visited]
   (let [was-visited (visited? visited obj)]
     (if was-visited
       (if flat
         "Circular Map"
         was-visited)
       (let [out (js-obj)]
         (visit! visited obj out)
         (doall
          (map
           #(let [key (->js (first %)  flat visited)
                  val (->js (second %) flat visited)]
              (aset out key val))
           obj))
         out))))

  ([obj flat] (map->js obj flat (atom {})))
  ([obj] (map->js obj false (atom {}))))

(defn ->js
  "Converts a cljs data structure into a close JS approximation."
  ([obj flat visited]
   (cond
     (keyword? obj)      (name obj)
     (symbol? obj)       (name obj)
     (= (type obj) Atom) (->js @obj flat visited)
     (seq? obj)          (seq->js obj flat visited)
     (set? obj)          (seq->js obj flat visited)
     (map? obj)          (map->js obj flat visited)
     :else               obj))

  ([obj flat] (->js obj flat (atom {})))
  ([obj] (->js obj false (atom {}))))


(.log js/console (let [a (atom [1])
      b [9 a]
      a-js (array 1)
      b-js (array 9 a-js)]

  (swap! a conj b)
  (.push a-js b)

  (seq->js b true)))
