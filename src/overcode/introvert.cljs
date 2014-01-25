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
  ([obj visited]
   (let [was-visited (visited? visited obj)]
     (if was-visited
       was-visited
       (let [out (array)]
         (visit! visited obj out)
         (array-append out (map #(->js % visited) obj))))))


  ([obj] (seq->js obj (atom {}))))

(defn map->js
  "Converts a map-like value into a JS Object."
  ([obj visited]
   (let [was-visited (visited? visited obj)]
     (if was-visited
       was-visited
       (let [out (js-obj)]
         (visit! visited obj out)
         (doall
          (map
           #(let [key (->js (first %)  visited)
                  val (->js (second %) visited)]
              (aset out key val))
           obj))
         out))))

  ([obj] (map->js obj (atom {}))))

(defn ->js
  "Converts a cljs data structure into a close JS approximation."
  ([obj visited]
   (condp = (type obj)
     cljs.core/Keyword (name obj)
     cljs.core/Symbol  (name obj)
     cljs.core/Atom    (->js @obj visited)

     cljs.core/IndexedSeq        (seq->js obj visited)
     cljs.core/LazySeq           (seq->js obj visited)
     cljs.core/PersistentVector  (seq->js obj visited)
     cljs.core/PersistentHashSet (seq->js obj visited)

     cljs.core/PersistentArrayMap (map->js obj visited)
     cljs.core/PersistentHashMap  (map->js obj visited)
     obj))

  ([obj] (->js obj (atom {}))))
