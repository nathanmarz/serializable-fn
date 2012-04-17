(ns serializable.fn
  "Serializable functions! Check it out."
  (:refer-clojure :exclude [fn]))


;; TODO: need to change this to save {:env :form}
(defn- save-env [bindings form]
  (let [form (with-meta (cons `fn (rest form)) ; serializable/fn, not core/fn
               (meta form))
        quoted-form `(quote ~form)]
    (if bindings
      `(list `let ~(vec (apply concat (for [b bindings]
                                        [`(quote ~(.sym b))
                                         (.sym b)])))
             ~quoted-form)
      quoted-form)))

(defmacro ^{:doc (str (:doc (meta #'clojure.core/fn))
                      "\n\n  Oh, but it also allows serialization!!!111eleven")}
  fn [& sigs]
  `(with-meta (clojure.core/fn ~@sigs)
     {:type ::serializable-fn
      ::source ~(save-env (vals &env) &form)}))

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
    ;; TODO: finish
    ))

(defmethod serialize-val :java [val]
  ;; TODO: java serialization
  )

(defmethod serialize-val :var [val]
  (let [var (search-for-var val)]
    (when-not var?
      (throw-runtime "Cannot serialize regular functions that are not bound to vars"))
    ;; TODO: serialize namespace + name
    ))

(defmethods serialize-val :serfn [val]
  ;; TODO: call serialize on all the environment vars, skipping any that don't serialize
  ;; TODO: then save the namespace and source of the function
  )

(defmulti deserialize-val (fn [token serialized]
                            (token->type token)))

(defn deserialize [serialized]
  ;; TODO: read token
  ;; return the deserialized value
  )

(defmethod deserialize-val :var [_ serialized]
  ;; TODO: require the namespace and deref the var
  )

(defmethod deserialize-val :java [_ serialized]
  ;; TODO: use java deserialization
  )

(defmethod deserialize-val :serfn [_ serialized]
  ;; iterate through env, calling deserialize, skipping any that don't deserialize
  ;; turn the source into a form
  ;; inside the namespace, create the let binding + form and eval it
  )
