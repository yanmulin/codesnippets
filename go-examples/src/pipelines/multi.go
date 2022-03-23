package pipelines

import "sync"

func fanIn(done <-chan interface{}, channels []<-chan interface{}) <-chan interface{} {
	var wg sync.WaitGroup

	multiplexedStream := make(chan interface{}, len(channels))

	multiplex := func(c <-chan interface{}) {
		defer wg.Done()
		for v := range c {
			select {
			case <-done:
				return
			case multiplexedStream <- v:
			}
		}
	}
	wg.Add(len(channels))
	for _, c := range channels {
		go multiplex(c)
	}

	go func() {
		defer close(multiplexedStream)
		wg.Wait()
	}()
	return multiplexedStream
}