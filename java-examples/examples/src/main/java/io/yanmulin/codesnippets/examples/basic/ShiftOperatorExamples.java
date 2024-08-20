package io.yanmulin.codesnippets.examples.basic;

public class ShiftOperatorExamples {
    public void run() {
        System.out.println("127 << 1 = " + (127 << 1));
        System.out.println("127 >> 1 = " + (127 >> 1));
        System.out.println("127 >>> 1 = " + (127 >>> 1));
        System.out.println("-127 >> 1 = " + (-127 >> 1));       // signed shift, expect -64
        System.out.println("-127 >>> 1 = " + (-127 >>> 1));     // unsigned shift, expect 2147483584
    }

    public static void main(String[] args) {
        new ShiftOperatorExamples().run();
    }
}
