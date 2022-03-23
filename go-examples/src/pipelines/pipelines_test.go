package pipelines

import (
	"fmt"
	"math/rand"
	"runtime"
	"testing"
)

func TestBasicPipeline(*testing.T) {
	done := make(chan interface{})
	intStream := generator(done, 1, 2, 3, 4, 5)
	pipeline := multiply(done, add(done, multiply(done, intStream, 2), 1), 2)
	for v := range pipeline {
		fmt.Print(v, " ")
	}
	fmt.Println()
}

func TestGenerateOneTenTimes(*testing.T) {
	done := make(chan interface{})
	pipeline := take(done, repeat(done, 1), 10)
	for v := range pipeline {
		fmt.Print(v, " ")
	}
	fmt.Println()
}

func TestGenerateTenRandomInts(*testing.T) {
	done := make(chan interface{})
	rand := func() interface{} { return rand.Int() }
	pipeline := take(done, repeatFn(done, rand), 10)
	for v := range pipeline {
		fmt.Print(v, " ")
	}
	fmt.Println()
}

func TestSinglePrimeFinder(*testing.T) {
	done := make(chan interface{})
	intStream := toInt(done, repeatFn(done, func() interface{} { return rand.Intn(500000000) }))
	for v := range take(done, primeFinder(done, intStream), 10) {
		fmt.Print(v, " ")
	}
	fmt.Println()
}

func TestMultiPrimeFinder(*testing.T) {
	done := make(chan interface{})
	numFinders := runtime.NumCPU()
	finders := make([]<-chan interface{}, numFinders)
	intStream := toInt(done, repeatFn(done, func() interface{} { return rand.Intn(500000000) }))
	for i := 0; i < numFinders; i ++ {
		finders[i] = primeFinder(done, intStream)
	}
	for v := range take(done, fanIn(done, finders), 10) {
		fmt.Print(v, " ")
	}
	fmt.Println()
}