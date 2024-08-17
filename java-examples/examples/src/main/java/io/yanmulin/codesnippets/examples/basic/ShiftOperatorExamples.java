package io.yanmulin.codesnippets.examples.basic;

public class ShiftOperatorExamples {
    public void run() {
        System.out.println("127 << 1 = " + (127 << 1));
        System.out.println("127 >> 1 = " + (127 >> 1));
        System.out.println("127 >>> 1 = " + (127 >>> 1));
        System.out.println("-127 >> 1 = " + (-127 >> 1));
        System.out.println("-127 >>> 1 = " + (-127 >>> 1));
    }

    public static void main(String[] args) {
        new ShiftOperatorExamples().run();
    }
}
