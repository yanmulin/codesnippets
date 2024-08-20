package io.yanmulin.codesnippets.benchmark;

import io.yanmulin.codesnippets.examples.algorithms.BranchlessFindByte;
import org.openjdk.jmh.annotations.*;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class FindByteBenchmark {

    @State(Scope.Benchmark)
    public static class FindByteState {

        int seed = 1;
        @Param({"8", "256", "1024"})
        int size;
        @Param({"7", "15"})
        int logPermutation;
        int permutations;
        byte[][] data;
        int i;

        @Setup(Level.Trial)
        public void init() {
            SplittableRandom random = new SplittableRandom(seed);
            permutations = 1 << logPermutation;
            data = new byte[permutations][];
            for (int i=0;i<permutations;i++) {
                data[i] = new byte[size];
                random.nextBytes(data[i]);
                for (int j = 0; j < size; j++) {
                    if (data[i][j] == 0) data[i][j] = 1;
                }
                data[i][random.nextInt(size - 8, size)] = 0;
            }
        }

        byte[] getData() {
            return data[i ++ & (permutations - 1)];
        }
    }

    @Benchmark
    public int baseline() {
        return 0;
    }

    @Benchmark
    public int swar(FindByteState state) {
        byte[] data = state.getData();
        for (int i=0;i<data.length;i+=Long.BYTES) {
            long word = getWord(data, i);
            int off = BranchlessFindByte.findZero(word);
            if (off < Long.BYTES) {
                return i + off;
            }
        }
        return -1;
    }

    @Benchmark
    public int scan(FindByteState state) {
        byte[] data = state.getData();
        for (int i=0;i<data.length;i++) {
            if (data[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    private long getWord(byte[] data, int i) {
        return data[i] | data[i + 1] << 8 | data[i + 2] << 16 | data[i + 3] << 24 |
                data[i + 4] << 32 | data[i + 5] << 40 | data[i + 6] << 48 | data[i + 7] << 56;
    }

}
