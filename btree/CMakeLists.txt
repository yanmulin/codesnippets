cmake_minimum_required(VERSION 3.14)
project(btree)

set(CMAKE_BUILD_TYPE Debug)

set(BTREE_CLANG_SEARCH_PATH "/usr/local/bin" "/usr/bin" "/usr/local/opt/llvm/bin" "/usr/local/opt/llvm@8/bin"
        "/usr/local/Cellar/llvm/8.0.1/bin")

# GoogleTest requires at least C++11
set(CMAKE_CXX_STANDARD 11)

include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/609281088cfefc76f9d0ce82e1ff6c30cc3591e5.zip
)
# For Windows: Prevent overriding the parent project's compiler/linker settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

enable_testing()

add_executable(
  btree_test
  btree.cc
  bptree.cc
  btree_test.cc
)

target_link_libraries(
  btree_test
  gtest_main
)

include(GoogleTest)

gtest_discover_tests(btree_test bplustree_test)
