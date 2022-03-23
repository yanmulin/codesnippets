package pipelines

import (
	"fmt"
	"testing"
	"time"
)


func sig(sleep time.Duration) <- chan interface{} {
	terminated := make(chan interface{})

	go func() {
		defer fmt.Println("close sig.")
		defer close(terminated)
		time.Sleep(sleep)
	}()

	return terminated
}

func TestOrChannel(*testing.T) {
	now := time.Now()
	<- or(
		sig(1 * time.Hour),
		sig(30 * time.Minute),
		sig(5 * time.Minute),
		sig(1 * time.Minute),
		sig(30 * time.Second),
		sig(10 * time.Second),
		sig(1 * time.Second),
	)
	fmt.Println("Done after,", time.Since(now))
}