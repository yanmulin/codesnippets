package io.yanmulin.codesnippets.patterns.proxy;

class Stub {
    static interface Foo {
        int bar();
    }

    static class FooImpl implements Foo {
        public int bar() { return 0; }
    }
}
