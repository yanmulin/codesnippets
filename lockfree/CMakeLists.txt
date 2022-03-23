cmake_minimum_required(VERSION 3.14)
project(lockfree-examples)

set(CMAKE_BUILD_TYPE Debug)
set(CMAKE_CXX_STANDARD 11)

# Google Test
include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/609281088cfefc76f9d0ce82e1ff6c30cc3591e5.zip
)
# For Windows: Prevent overriding the parent project's compiler/linker settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)
include(GoogleTest)

# Google Benchmark
# find_package(benchmark REQUIRED)

# Valgrind
find_program(VALGRIND_BIN valgrind)
if ("${VALGRIND_BIN}" STREQUAL "VALGRIND_BIN-NOTFOUND")
    message(WARNING "Couldn't find valgrind.")
else()
    message(STATUS "Found valgrind at ${VALGRIND_BIN}")
endif()

set(VALGRIND_OPTIONS
        --error-exitcode=1
        --leak-check=full
        --soname-synonyms=somalloc=*jemalloc*
        --trace-children=yes
        --track-origins=yes
        )

enable_testing()

add_subdirectory(stack)
