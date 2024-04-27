package io.yanmulin.codesnippets.examples.patterns.singleton;

import java.io.*;
import java.lang.reflect.Constructor;

public class SimpleEagerSingleton implements Serializable {

    private static final SimpleEagerSingleton INSTANCE = new SimpleEagerSingleton();

    private SimpleEagerSingleton() {}

    public static SimpleEagerSingleton getInstance() { return INSTANCE; }

    public static void main(String[] args) {
        SimpleEagerSingleton singleton = SimpleEagerSingleton.getInstance();

        // jeopardize with Java reflection API
        try {
            Constructor c = SimpleEagerSingleton.class.getDeclaredConstructor(null);
            c.setAccessible(true);
            assert c.newInstance() != singleton;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // jeopardize with serialization & deserialization
        try {
            FileOutputStream fos = new FileOutputStream("SimpleEagerSingleton.obj");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(singleton);
            oos.flush();
            oos.close();

            FileInputStream fis = new FileInputStream("SimpleEagerSingleton.obj");
            ObjectInputStream ois = new ObjectInputStream(fis);
            SimpleEagerSingleton deserialized = (SimpleEagerSingleton) ois.readObject();
            ois.close();

            assert deserialized != singleton;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
