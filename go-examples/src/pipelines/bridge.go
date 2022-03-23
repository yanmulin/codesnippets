package pipelines

func bridge(done <-chan interface{}, chanStream <-chan <-chan interface{}) <-chan interface{} {
	valStream := make(chan interface{})
	go func() {
		defer close(valStream)
		for {
			var stream <-chan interface{}
			select {
			case <-done:
				return
			case maybeStream, ok := <- chanStream:
				if !ok {
					return
				}
				stream = maybeStream
			}
			for v := range orDone(done, stream) {
				select {
				case <-done:
					return
				case valStream <- v:
				}
			}
		}
	}()
	return valStream
}
