#!/bin/bash

[[ ! -d build ]] && mkdir build
cd build
cmake ..
make all && ctest --verbose
