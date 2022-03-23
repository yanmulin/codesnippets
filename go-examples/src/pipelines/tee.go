package pipelines

func tee(done <-chan interface{}, inStream <-chan interface{}) (<-chan interface{}, <-chan interface{}) {
	out1, out2 := make(chan interface{}), make(chan interface{})
	go func() {
		defer close(out1)
		defer close(out2)
		for v := range inStream {
			out1, out2 := out1, out2
			for i:=0;i<2;i++ {
				select {
				case <-done:
					return
				case out1 <- v:
					out1 = nil
				case out2 <- v:
					out2 = nil
				}
			}
		}
	}()
	return out1, out2
}
