package io.yanmulin.codesnippets.examples.gc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeapInstanceTest {
    byte[] buffer = new byte[new Random().nextInt(4096 * 4096)];

    public static void main(String[] args) {
        System.out.println("process id " + ProcessHandle.current().pid());
        System.out.println("initial memory(mb) " + Runtime.getRuntime().totalMemory() / 1024 / 1024);
        System.out.println("max memory(mb) " + Runtime.getRuntime().maxMemory() / 1024 / 1024);
        List<HeapInstanceTest> list = new ArrayList<>();
        while (true) {
            list.add(new HeapInstanceTest());
            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }


    }
}
