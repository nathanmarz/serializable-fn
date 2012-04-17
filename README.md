# serializable fn

This library is capable of serializing anonymous functions defined with the replacement serializable.fn/fn macro or functions that are bound to vars somewhere. Use serializable.fn/serialize and serializable.fn/deserialize to serialize and deserialize to and from byte arrays.

For anonymous functions, this library captures the following into the metadata of the function:

1. The environment (closure) as a map from variable name to value.
2. The namespace
3. The form that defines the function

To deserialize anonymous functions, it evals the function form in the namespace it was defined by first wrapping the form in a let binding that reads from the environment that was captured when the function was originally created.

The environment of an anonymous function is serialized recursively.

For other functions (that were not created using the replacement `fn` macro), the library searches for a var in any loaded lib whose value is equal to that function. It then serializes the namespace and name of that var.

All other objects that need to be serialized are serialized using Java serialization.

Demo:

```clojure
(use 'serializable.fn)

;; note that this function being created has a function value in its closure
(def f (let [x + c 2]
          (fn [a b] (x a b c))))

(println (f 1 2))

;; 5

(def b (serialize f))

(println (seq b))

;; (0 0 0 2 0 0 0 -79 0 0 0 2 0 1 99 0 0 0 90 0 0 0 3 0 0 0 82 -84 -19 0 5 115 114 0 14 106 97 118 97 46 108 97 110 103 46 76 111 110 103 59 -117 -28 -112 -52 -113 35 -33 2 0 1 74 0 5 118 97 108 117 101 120 114 0 16 106 97 118 97 46 108 97 110 103 46 78 117 109 98 101 114 -122 -84 -107 29 11 -108 -32 -117 2 0 0 120 112 0 0 0 0 0 0 0 2 0 1 120 0 0 0 25 0 0 0 1 0 0 0 17 0 12 99 108 111 106 117 114 101 46 99 111 114 101 0 1 43 0 4 117 115 101 114 0 36 40 115 101 114 105 97 108 105 122 97 98 108 101 46 102 110 47 102 110 32 91 97 32 98 93 32 40 120 32 97 32 98 32 99 41 41)

;; now, restart the repl

(def b
(byte-array (map byte '(0 0 0 2 0 0 0 -79 0 0 0 2 0 1 99 0 0 0 90 0 0 0 3 0 0 0 82 -84 -19 0 5 115 114 0 14 106 97 118 97 46 108 97 110 103 46 76 111 110 103 59 -117 -28 -112 -52 -113 35 -33 2 0 1 74 0 5 118 97 108 117 101 120 114 0 16 106 97 118 97 46 108 97 110 103 46 78 117 109 98 101 114 -122 -84 -107 29 11 -108 -32 -117 2 0 0 120 112 0 0 0 0 0 0 0 2 0 1 120 0 0 0 25 0 0 0 1 0 0 0 17 0 12 99 108 111 106 117 114 101 46 99 111 114 101 0 1 43 0 4 117 115 101 114 0 36 40 115 101 114 105 97 108 105 122 97 98 108 101 46 102 110 47 102 110 32 91 97 32 98 93 32 40 120 32 97 32 98 32 99 41 41))))

(use 'serializable.fn)

(def f (deserialize b))

(println f 1 2)

;; 5
```

## TODO

- Recurse into collections with the deep function serialization

## License

Copyright © 2010-2012 Seajure, The Seattle Clojure group and contributors

Distributed under the EPL, the same license as Clojure.
