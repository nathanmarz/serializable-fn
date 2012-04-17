package serializable.fn;

import clojure.lang.IFn;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        return bos.toByteArray();
    }

    public static Object deserialize(byte[] serialized) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object ret = ois.readObject();
        ois.close();
        return ret;
    }
    
    public static byte[] serializePair(Number token, byte[] serialized) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(token.intValue());
        writeBinary(dos, serialized);
        return bos.toByteArray();
    }
    
    public static List deserializePair(byte[] serialized) throws IOException {
        List ret = new ArrayList();
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(serialized));
        int token = is.readInt();
        byte[] val = readBinary(is);
        ret.add(token);
        ret.add(val);
        return ret;
    }
    
    public static byte[] serializeVar(String namespace, String name) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF(namespace);
        dos.writeUTF(name);
        return bos.toByteArray();        
    }
    
    public static List deserializeVar(byte[] serialized) throws IOException {
        List ret = new ArrayList();
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(serialized));
        ret.add(is.readUTF());
        ret.add(is.readUTF());
        return ret;
    }
    
    public static byte[] serializeFn(Map<String, byte[]> env, String namespace, String source) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(env.size());
        for(String name: env.keySet()) {
            byte[] val = env.get(name);
            dos.writeUTF(name);
            writeBinary(dos, val);
        }
        dos.writeUTF(namespace);
        dos.writeUTF(source);        
        return bos.toByteArray();        
    }
    
    public static List deserializeFn(byte[] serialized) throws IOException {
        List ret = new ArrayList();
        Map<String, byte[]> env = new HashMap();
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(serialized));
        int envAmt = is.readInt();
        for(int i=0; i<envAmt; i++) {
            String name = is.readUTF();
            byte[] val = readBinary(is);
            env.put(name, val);
        }
        ret.add(env);
        ret.add(is.readUTF());
        ret.add(is.readUTF());
        return ret;
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
        tryRequire(ns_name);
        return (IFn) RT.var(ns_name, fn_name).deref();
    }
    
    private static void writeBinary(DataOutputStream dos, byte[] bytes) throws IOException {
        dos.write(bytes.length);
        dos.write(bytes);
    }
    
    private static byte[] readBinary(DataInputStream dos) throws IOException {
        int len = dos.readInt();
        byte[] ret = new byte[len];
        dos.readFully(ret);
        return ret;
    }
}