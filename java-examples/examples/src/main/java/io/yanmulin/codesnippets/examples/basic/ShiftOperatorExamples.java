package io.yanmulin.codesnippets.examples.basic;

public class ShiftOperatorExamples {
    public void run() {
        System.out.println("127 << 1 = " + (127 << 1));
        System.out.println("127 >> 1 = " + (127 >> 1));
        System.out.println("127 >>> 1 = " + (127 >>> 1));

        System.out.println(String.format("-127 = 0b%s", Integer.toBinaryString(-127)));
        // signed shift, expect -64
        System.out.println(String.format("-127 >> 1 = %d(0b%s)", -127 >> 1, Integer.toBinaryString(-127 >> 1)));
        // unsigned shift, expect 2147483584
        System.out.println(String.format("-127 >>> 1 = %d(0b%s)", -127 >>> 1, Integer.toBinaryString(-127 >>> 1)));
    }

    public static void main(String[] args) {
        new ShiftOperatorExamples().run();
    }
}
