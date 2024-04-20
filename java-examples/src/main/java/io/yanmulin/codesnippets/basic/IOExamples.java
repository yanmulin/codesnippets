package io.yanmulin.codesnippets.basic;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.*;

public class IOExamples {

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class Person implements Serializable {
        private String name;
        private int age;
    }

    public void buffer() throws IOException {
        String content = "hello world";
        File file = new File("out.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        try {
            bw.write(content + "\n");
        } finally {
            bw.close();
        }

        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            assert br.readLine().equals(content);
        } finally {
            br.close();
            file.delete();
        }

    }

    public void serialize() throws IOException, ClassNotFoundException {
        Object obj = new Person("John", 21);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        byte[] serialized = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object deserialized = ois.readObject();

        assert obj.equals(deserialized);
        assert obj != deserialized;

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new IOExamples().buffer();
        new IOExamples().serialize();
    }
}
