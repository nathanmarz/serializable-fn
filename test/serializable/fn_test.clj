(ns serializable.fn-test
  (:refer-clojure :exclude [fn])
  (:use [serializable.fn]
        [clojure.test]))

(def dinc-list '(serializable.fn/fn [x] (inc (inc x))))

(def dinc (eval dinc-list))

(deftest fns-created-with-metadata
  (is (ifn? dinc))
  (is (map? (meta dinc))))

(deftest metadata-fns-work-as-original
  (is (= 2 (dinc 0))))

(deftest metadata-fns-return-source
  (is (= dinc-list (:serializable.fn/source (meta dinc)))))

(deftest printing-fns-show-source
  (is (= (pr-str dinc-list)
         (pr-str dinc))))

(deftest preserve-reader-metadata
  (is (number? (:line (meta (:serializable.fn/source
                             (meta dinc)))))))

(def write+read (comp eval read-string pr-str))

(defn round-trip [f & args]
  (apply (write+read f) args))

(deftest serializable-fn-roundtrip!!!111eleven
  (is (= 2 (round-trip dinc 0))))

(deftest roundtrip-with-lexical-nonconst-context
  (let [x 10, y (inc x)]
    (is (= 11
           (round-trip (fn [] y))))))

(deftest roundtrip-with-fnarg-context
  (is (= 11
         (round-trip ((fn [x]
                        (let [y (inc x)]
                          (fn [] y)))
                      10)))))

(deftest roundtrip-twice!
  (is (= 5
         ((write+read (write+read (let [x 5]
                                    (fn [] x))))))))
