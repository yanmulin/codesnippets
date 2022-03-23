#include <benchmark/benchmark.h>

#include "stack_gc.cc"

static void BM_stack_gc(benchmark::State& state) {
    stack_gc<int> stk;
    for (auto _: state) {
        stk.push(0);
        stk.pop();
    }
}
BENCHMARK(BM_stack)->Threads(16)->Threads(32)->Threads(64);

BENCHMARK_MAIN();
