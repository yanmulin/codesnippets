package io.yanmulin.codesnippets.basic;

import java.io.*;

public class IOExamples {

    public void buffer() throws IOException {
        File file = new File("out.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write("hello world\n");
        bw.close();

        BufferedReader br = new BufferedReader(new FileReader(file));
        System.out.print(br.readLine());
        br.close();

        file.delete();
    }

    public static void main(String[] args) throws IOException {
        new IOExamples().buffer();
    }
}
