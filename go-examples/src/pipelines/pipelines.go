package pipelines

func multiply(done <-chan interface{}, intStream <-chan int, multiplier int) <-chan int {
	multipliedStream := make(chan int)
	go func() {
		defer close(multipliedStream)
		for v := range intStream {
			select {
			case <-done:
				return
			case multipliedStream <- v * multiplier:
			}
		}
	}()
	return multipliedStream
}

func add(done <-chan interface{}, intStream <-chan int, additive int) <-chan int {
	addedStream := make(chan int)
	go func() {
		defer close(addedStream)
		for v := range intStream {
			select {
			case <-done:
				return
			case addedStream <- v + additive:
			}
		}
	}()
	return addedStream
}

func take(done <-chan interface{}, valueStream <-chan interface{}, num int) <-chan interface{} {
	takeStream := make(chan interface{})
	go func() {
		defer close(takeStream)
		for i:=0;i<num;i++ {
			select {
			case <-done:
				return
			case takeStream <- <- valueStream:
			}
		}
	}()
	return takeStream
}

func toInt(done <-chan interface{}, inStream <-chan interface{}) <-chan int {
	outStream := make(chan int)
	go func() {
		defer close(outStream)
		for v := range inStream {
			select {
			case <-done:
				return
			case outStream <- v.(int):
			}
		}
	}()
	return outStream
}

func primeFinder(done <-chan interface{}, intStream <-chan int) <-chan interface{} {
	primeStream := make(chan interface{})

	isPrime := func(x int) bool {
		if x % 2 == 0 {
			return false
		}
		for i:=3;i<x;i+=2 {
			if x % i == 0 {
				return false
			}
		}
		return true
	}

	go func() {
		defer close(primeStream)
		for v := range intStream {
			if isPrime(v) {
				select {
				case <-done:
					return
				case primeStream <- v:
				}
			}
		}
	}()

	return primeStream
}