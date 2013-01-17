package serializable.fn.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import static carbonite.JavaBridge.enhanceRegistry;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class KryoSerialization {

    private static final ThreadLocal<Kryo> kryo = new ThreadLocal<Kryo>();
    private static final ThreadLocal<ByteArrayOutputStream> byteStream = new ThreadLocal<ByteArrayOutputStream>();

    public KryoSerialization() {
    }

    private Kryo freshKryo() {
        Kryo k = new Kryo();
        k.setInstantiatorStrategy(new StdInstantiatorStrategy());
        k.setRegistrationRequired(false);
        
        try {
            enhanceRegistry(k);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        k.register(ArrayList.class);
        k.register(HashMap.class);
        k.register(HashSet.class);

        return k;
    }

    public Kryo getKryo() {
        if (kryo.get() == null)
            kryo.set(freshKryo());

        return kryo.get();
    }

    public ByteArrayOutputStream getByteStream() {
        if (byteStream.get() == null)
            byteStream.set(new ByteArrayOutputStream());

        return byteStream.get();
    }

    public byte[] serialize(Object o) {
        getByteStream().reset();
        Output ko = new Output(getByteStream());
        getKryo().writeClassAndObject(ko, o);
        ko.flush();
        byte[] bytes = getByteStream().toByteArray();

        return bytes;
    }

    public Object deserialize(byte[] bytes) {
        return getKryo().readClassAndObject(new Input(bytes));
    }

    public <T> T deserialize(byte[] bytes, Class<T> klass) {
        return getKryo().readObject(new Input(bytes), klass);
    }
}
