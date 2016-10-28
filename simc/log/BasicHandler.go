// Package text implements a development-friendly textual handler.
package log

import (
	"fmt"
	"io"
	"os"
	"sort"
	"sync"
	"time"

	"github.com/apex/log"

	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/general/times"
)

// Default handler outputting to stderr.
var DefaultBasicHandler = NewBasicHandler(os.Stderr)

// New handler.
func NewBasicHandler(w io.Writer) *BasicHandler {
	return &BasicHandler{Writer: w, lastTimestamp: times.Max()}
}

// Strings mapping.
var levels = [...]string{
	log.DebugLevel: "D",
	log.InfoLevel:  "I",
	log.WarnLevel:  "W",
	log.ErrorLevel: "E",
	log.FatalLevel: "F"}

// field used for sorting.
type field struct {
	Name  string
	Value interface{}
}

func (this field) String() string {
	return fmt.Sprintf("%v: %v", this.Name, this.Value)
}

// by sorts projects by call count.
type byName []field

func (a byName) Len() int           { return len(a) }
func (a byName) Swap(i, j int)      { a[i], a[j] = a[j], a[i] }
func (a byName) Less(i, j int) bool { return a[i].Name < a[j].Name }

// BasicHandler implementation.
type BasicHandler struct {
	mu            sync.Mutex
	Writer        io.Writer
	lastTimestamp time.Time
}

// HandleLog implements log.BasicHandler.
func (h *BasicHandler) HandleLog(e *log.Entry) error {
	if IsEnabled() == false {
		return nil
	}

	var fields []field

	for k, v := range e.Fields {
		fields = append(fields, field{k, v})
	}

	sort.Sort(byName(fields))

	joinedFields := ""
	if 0 < len(fields) {
		joinedFields = fmt.Sprintf(" [%s]", slice.ToString(fields, "; "))
	}

	var timestamp string
	eTimestamp := e.Timestamp.In(time.UTC)
	if eTimestamp.Equal(h.lastTimestamp) == false {
		timestamp = eTimestamp.Format("2006-01-02 15:04:05.9999")
		h.lastTimestamp = eTimestamp
	}

	h.mu.Lock()
	defer h.mu.Unlock()

	fmt.Fprintf(h.Writer, "%v %-24s %-25s%s\n", levels[e.Level], timestamp, e.Message, joinedFields)

	return nil
}
