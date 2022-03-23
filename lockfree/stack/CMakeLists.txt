add_executable(stack-test test.cc hazard_pointer.cc)
target_link_libraries(stack-test gtest_main)
add_test(stack-test ${VALGRIND_BIN} ${VALGRIND_OPTIONS} 
    ${CMAKE_BINARY_DIR}/stack/stack-test --gtest_color=yes)

# add_executable(stack-benchmark benchmark.cc)
# target_link_libraries(stack-benchmark benchmark::benchmark)