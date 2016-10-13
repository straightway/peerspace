/****************************************************************************
   Copyright 2016 github.com/straightway

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
****************************************************************************/

package log

import (
	"github.com/apex/log"

	"github.com/straightway/straightway/general/times"
)

type SimulationTimeHandler struct {
	baseHandler log.Handler
	timer       times.Provider
}

func NewSimulationTimeHandler(baseHandler log.Handler, timer times.Provider) *SimulationTimeHandler {
	return &SimulationTimeHandler{baseHandler: baseHandler, timer: timer}
}

func (this *SimulationTimeHandler) HandleLog(entry *log.Entry) error {
	entryWithSimulationTime := &log.Entry{
		Logger:    entry.Logger,
		Message:   entry.Message,
		Fields:    entry.Fields,
		Level:     entry.Level,
		Timestamp: this.timer.Time()}
	return this.baseHandler.HandleLog(entryWithSimulationTime)
}
