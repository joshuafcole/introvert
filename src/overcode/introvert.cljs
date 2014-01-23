(ns overcode.introvert)

(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq)
  )

(def ->js)

(defn visit [obj visited]
  "Marks potentially linked objects as visited to handle circular refs."
  (cond
   (nil? obj) true

   (vector? obj) true
   (list?   obj) true
   (seq?    obj) true

   (in? @visited obj) nil
   :else (swap! visited conj obj)))

(defn seq->js
  "Converts a seq-like value into a JS Array."
  ([obj visited]
   (if (visit obj visited)
     (into-array (map #(->js % visited) obj))
     obj))
  ([obj] (seq->js obj (atom ()))))

(defn map->js
  "Converts a mal-like value into a JS Object."
  ([obj visited]
   (if (visit obj visited)
     (let [out (js-obj)]
       (doall (map (fn [pair]
                     (let [
                           key (->js (first pair)  visited)
                           val (->js (second pair) visited)]
                       (aset out key val)))
                   obj))
       out)
     obj))
  ([obj] (map->js obj (atom ()))))

(defn ->js
  "Converts a cljs data structure into a close JS approximation."
  ([obj visited]
   (condp = (type obj)
     cljs.core/Keyword (name obj)
     cljs.core/Symbol  (name obj)
     cljs.core/Atom    (->js @obj visited)

     cljs.core/PersistentVector (seq->js obj visited)
     cljs.core/IndexedSeq       (seq->js obj visited)
     cljs.core/LazySeq          (seq->js obj visited)

     cljs.core/PersistentHashSet  (seq->js obj visited)
     cljs.core/PersistentHashMap  (map->js obj visited)
     cljs.core/PersistentArrayMap (map->js obj visited)
     obj))
  ([obj] (->js obj (atom ()))))
