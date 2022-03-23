#include <gtest/gtest.h>
#include <vector>
#include <future>
#include <memory>
#include "stack.h"
#include "stack_gc.cc"
#include "stack_hp.cc"
#include "hazard_pointer.h"

void basic_test(stack<int> *stk) {
    EXPECT_EQ(nullptr, stk->pop());
    stk->push(1); 
    stk->push(2); 
    stk->push(3);
    auto x = stk->pop(), y = stk->pop(), z = stk->pop();
    ASSERT_NE(nullptr, x); ASSERT_NE(nullptr, y); ASSERT_NE(nullptr, z);
    EXPECT_EQ(3, *x); EXPECT_EQ(2, *y); EXPECT_EQ(1, *z);
}

void concurrency_test(stack<int> *stk, size_t num_pop_threads, 
    size_t num_push_threads, size_t num_ops) {

    ASSERT_LE(num_pop_threads + num_push_threads, MAX_HAZARD_POINTERS);

    for (size_t i=0;i<num_ops*num_pop_threads;i++) {
        stk->push(0);
    }

    std::vector<std::promise<void>> pop_ready(num_pop_threads);
    std::vector<std::promise<void>> push_ready(num_push_threads);
    std::promise<void> start;
    std::shared_future<void> go(start.get_future());
    std::vector<std::future<void>> pop_done(num_pop_threads);
    std::vector<std::future<void>> push_done(num_push_threads);

    for (size_t i=0;i<num_pop_threads;i++) {
        pop_done[i] = std::async(std::launch::async, [num_ops, go, i, &pop_ready, &stk] {
            pop_ready[i].set_value();
            go.wait();
            for (size_t j=0;j<num_ops;j++) {
                EXPECT_NE(nullptr, stk->pop());
            }
        });
    }

    for (size_t i=0;i<num_push_threads;i++) {
        push_done[i] = std::async(std::launch::async, [num_ops, go, i, &push_ready, &stk] {
            push_ready[i].set_value();
            go.wait();
            for (size_t j=0;j<num_ops;j++) {
                stk->push(0);
            }
        });
    }

    for (auto &ready: pop_ready)
        ready.get_future().wait();
    for (auto &ready: push_ready)
        ready.get_future().wait();
    start.set_value();
    for (auto &done: pop_done)
        done.wait();
    for (auto &done: push_done)
        done.wait();
}

TEST(LockFreeStack_gc, Basic) {
    std::unique_ptr<stack<int>> stk{new stack_gc<int>{}};
    basic_test(stk.get());
}

TEST(LockFreeStack_gc, ConcurrentPop) {
    std::unique_ptr<stack<int>> stk{new stack_gc<int>{}};
    concurrency_test(stk.get(), 30, 0, 10000);
}

TEST(LockFreeStack_gc, Concurrency) {
    std::unique_ptr<stack_gc<int>> stk{new stack_gc<int>{}};
    concurrency_test(stk.get(), 20, 20, 10000);
    // ASSERT_EQ(stk->nodes_created.load(), stk->nodes_deleted.load());
}

TEST(LockFreeStack_hp, DISABLED_Basic) {
    std::unique_ptr<stack<int>> stk{new stack_hp<int>{}};
    basic_test(stk.get());
}

TEST(LockFreeStack_hp, DISABLED_ConcurrentPop) {
    std::unique_ptr<stack<int>> stk{new stack_hp<int>{}};
    concurrency_test(stk.get(), 30, 0, 10000);
}

TEST(LockFreeStack_hp, DISABLED_Concurrency) {
    std::unique_ptr<stack<int>> stk{new stack_hp<int>{}};
    concurrency_test(stk.get(), 20, 20, 10000);
}
