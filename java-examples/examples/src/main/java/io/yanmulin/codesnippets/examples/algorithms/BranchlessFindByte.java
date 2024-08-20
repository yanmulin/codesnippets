package io.yanmulin.codesnippets.examples.algorithms;

public class BranchlessFindByte {
    public static int findZero(long word) {
        long tmp = (word & 0x7F7F7F7F7F7F7F7FL) + 0x7F7F7F7F7F7F7F7FL;
        tmp = ~(tmp | word | 0x7F7F7F7F7F7F7F7FL);
        return Long.numberOfTrailingZeros(tmp) >>> 3;
    }

    public static int findByte(long word, byte target) {
        long pattern = compilePattern(target);
        return findZero(word ^ pattern);
    }

    private static long compilePattern(byte target) {
        long pattern = target & 0x7FL;
        return pattern | (pattern << 8) | (pattern << 16) | (pattern << 24)
                | (pattern << 32) | (pattern << 40) | (pattern << 48) | (pattern << 56);
    }

    public static void main(String[] args) {
        // find zero
        System.out.println(findZero(0x00801001538015L)); // expect 6
        System.out.println(findZero(0x53800001538000L)); // expect 0
        System.out.println(findZero(0x53800001538012L)); // expect 4
        System.out.println(findZero(0x0034567890123456L)); // expect 7
        System.out.println(findZero(0x1234567890123456L)); // expect 8
        System.out.println("---");

        // find arbitrary byte
        System.out.println(findByte(0x00801001538015L, (byte) 0x53)); // expect 2
        System.out.println(findByte(0x53800001538000L, (byte) 0x01)); // expect 3
        System.out.println(findByte(0x53800001548012L, (byte) 0x53)); // expect 6
        System.out.println(findByte(0x001234567890133456L, (byte) 0x12)); // expect 7
    }


}
