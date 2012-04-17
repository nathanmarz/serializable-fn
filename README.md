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

## License

Copyright © 2010-2012 Seajure, The Seattle Clojure group and contributors

Distributed under the EPL, the same license as Clojure.
