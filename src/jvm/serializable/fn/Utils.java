package serializable.fn;

import clojure.lang.IFn;
import clojure.lang.MultiFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import serializable.fn.kryo.KryoSerialization;

public class Utils {
    
    public static byte[] serialize(Object obj) throws IOException {
        // ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // ObjectOutputStream oos = new ObjectOutputStream(bos);
        // oos.writeObject(obj);
        // oos.close();
        // return bos.toByteArray();
        KryoSerialization kryo = new KryoSerialization();
        return kryo.serialize(obj);
    }

    public static Object deserialize(byte[] serialized) throws IOException {
        // ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
        // ObjectInputStream ois = new ObjectInputStream(bis);
        // Object ret = ois.readObject();
        // ois.close();
        // return ret;
        KryoSerialization kryo = new KryoSerialization();
        return kryo.deserialize(serialized);
    }
    
    static Var require = RT.var("clojure.core", "require");
    static Var symbol = RT.var("clojure.core", "symbol");

    public static Throwable getRootCause(Throwable e) {
        Throwable rootCause = e;
        Throwable nextCause = rootCause.getCause();

        while (nextCause != null) {
            rootCause = nextCause;
            nextCause = rootCause.getCause();
        }
        return rootCause;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static void tryRequire(String ns_name) {
        try {
            require.invoke(symbol.invoke(ns_name));
        } catch (Exception e) {

            //if playing from the repl and defining functions, file won't exist
            Throwable rootCause = getRootCause(e);

            boolean fileNotFound = (rootCause instanceof FileNotFoundException);
            boolean nsFileMissing = e.getMessage().contains(ns_name + ".clj on classpath");

            if (!(fileNotFound && nsFileMissing))
                throw new RuntimeException(e);
        }
    }

    public static IFn bootSimpleFn(String ns_name, String fn_name) {
        return (IFn) bootSimpleVar(ns_name, fn_name).deref();
    }

    public static MultiFn bootSimpleMultifn(String ns_name, String fn_name) {
        return (MultiFn) bootSimpleVar(ns_name, fn_name).deref();
    }
    
    public static Var bootSimpleVar(String ns_name, String fn_name) {
        tryRequire(ns_name);
        return RT.var(ns_name, fn_name);
    }    
}
