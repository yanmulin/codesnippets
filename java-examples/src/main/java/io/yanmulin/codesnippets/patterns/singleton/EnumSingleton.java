package io.yanmulin.codesnippets.patterns.singleton;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;

public enum EnumSingleton {
    INSTANCE;
    EnumSingleton() {}

    public static EnumSingleton getInstance() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        EnumSingleton singleton = EnumSingleton.getInstance();

        // disable Java reflection API
        try {
            Constructor c = EnumSingleton.class.getDeclaredConstructor(null);
            c.setAccessible(true);
            assert false;
        } catch (NoSuchMethodException e) {  // expected
        } catch (Exception e) {
            e.printStackTrace();
        }


        // support serialization & deserialization
        try {
            FileOutputStream fos = new FileOutputStream("EnumSingleton.obj");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(singleton);
            oos.flush();
            oos.close();

            FileInputStream fis = new FileInputStream("EnumSingleton.obj");
            ObjectInputStream ois = new ObjectInputStream(fis);
            EnumSingleton deserialized = (EnumSingleton) ois.readObject();
            ois.close();

            assert deserialized == singleton;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
