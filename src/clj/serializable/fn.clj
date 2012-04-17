(ns serializable.fn
  "Serializable functions! Check it out."
  (:import [serializable.fn Utils])
  (:refer-clojure :exclude [fn]))

(defn- save-env [bindings form]
  (let [form (with-meta (cons `fn (rest form)) ; serializable/fn, not core/fn
               (meta form))
        quoted-form `(quote ~form)
        namespace (str *ns*)
        savers (for [b bindings] [(str (.sym b)) (.sym b)])
        env-form `(into {} ~(vec savers))
        ]
    [env-form namespace quoted-form]
    ))

(defmacro ^{:doc (str (:doc (meta #'clojure.core/fn))
                      "\n\n  Oh, but it also allows serialization!!!111eleven")}
  fn [& sigs]
  (let [[env-form namespace form] (save-env (vals &env) &form)]    
    `(with-meta (clojure.core/fn ~@sigs)
       {:type ::serializable-fn
        ::env ~env-form
        ::namespace ~namespace
        ::source ~form})))

(defn search-for-var [val]
  (->> (loaded-libs)
       (map ns-map)
       (mapcat identity)
       (map second)
       (filter #(and (var? %) (= (var-get %) val)))
       first ))

(def SERIALIZED-TYPES
  {:var 1
   :serfn 2
   :java 3})

(defn type->token [type]
  (SERIALIZED-TYPES type))

(let [reversed (into {} (for [[k v] SERIALIZED-TYPES] [v k]))]
  (defn token->type [token]
    (reversed token)
    ))

(defn serialize-type [val]
  (if (fn? val)
    (if (= ::serializable-fn (-> val meta :type))
      :serfn
      :var)
    :java
    ))

(defmulti serialize-val serialize-type)

(defn serialize [val]
  (let [type (serialize-type val)
        serialized (serialize-val val)]
    (Utils/serializePair (type->token type) serialized)
    ))

(defmethod serialize-val :java [val]
  (Utils/serialize val))

(defn ns-fn-name-pair [v]
  (let [m (meta v)]
    [(str (:ns m)) (str (:name m))]))

(defmethod serialize-val :var [val]
  (let [avar (search-for-var val)]
    (when-not avar
      (throw (RuntimeException. "Cannot serialize regular functions that are not bound to vars")))
    (let [[ns fn-name] (ns-fn-name-pair avar)]
      (Utils/serializeVar ns fn-name))))

(defn best-effort-map-val [amap afn]
  (into {}
       (mapcat
        (fn [[name val]]
          (try
            [[name (afn val)]]
            (catch Exception e
              []
              )))
        amap)))

(defmethod serialize-val :serfn [val]
  (let [[env namespace source-form] ((juxt ::env ::namespace ::source) (meta val))        
        ser-env (best-effort-map-val env serialize)]
    (Utils/serializeFn ser-env namespace (pr-str source-form))))

(defmulti deserialize-val (fn [token serialized]
                            (token->type token)))

(defn deserialize [serialized]
  (let [[token val-ser] (Utils/deserializePair serialized)]
    (deserialize-val token val-ser)))

(defn best-effort-env-deserialize [env]
  (->> env
       (mapcat
        (fn [[name val]]
          (try
            [name (deserialize val)]
            (catch Exception e
              []
              ))))))

(defmethod deserialize-val :var [_ serialized]
  (let [[ns name] (Utils/deserializeVar serialized)]
    (Utils/bootSimpleFn ns name)))

(defmethod deserialize-val :java [_ serialized]
  (Utils/deserialize serialized))

(defn set-var-root* [avar val]
  (alter-var-root avar (fn [avar] val)))

(defn reset-vars [var-map]
  (doseq [[avar val] var-map]
    (set-var-root* avar val)
    ))

(defmethod deserialize-val :serfn [_ serialized]
  (let [[ser-env namespace source] (Utils/deserializeFn serialized)
        env (best-effort-map-val ser-env deserialize)
        source-form (read-string source)
        namespace (symbol namespace)
        _ (println env source source-form namespace)
        old-ns (-> *ns* str symbol)
        _ (in-ns (symbol namespace))
        ;; TODO: this is stupid. it has to be lexical context.

        saved (into {}
                    (for [[name val] env :let [avar (intern *ns* (symbol name))
                                               old (var-get avar)]]
                      (do
                        (set-var-root* avar val)
                        [avar old])))
        ret (try
              (eval source-form)
              (finally
               (reset-vars saved)
               ))]
    (in-ns old-ns)
    ret ))
