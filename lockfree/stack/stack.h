#pragma once

#include <atomic>
#include <memory>

template <typename T>
class stack {
public:
    virtual std::shared_ptr<T> pop() = 0;
    virtual void push(const T &data) = 0;
};