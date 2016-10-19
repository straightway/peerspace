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
	"testing"
	"time"

	"github.com/apex/log"

	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"

	"github.com/straightway/straightway/general/times"
)

// Test suite

type SimulationTimeHandler_Test struct {
	suite.Suite
	sut         *SimulationTimeHandler
	baseHandler *handlerMock
	timer       *times.ProviderMock
}

func TestSimulationTimeHandler(t *testing.T) {
	suite.Run(t, new(SimulationTimeHandler_Test))
}

func (suite *SimulationTimeHandler_Test) SetupTest() {
	suite.baseHandler = newHandlerMock()
	suite.timer = &times.ProviderMock{}
	suite.sut = NewSimulationTimeHandler(suite.baseHandler, suite.timer)
	log.SetHandler(suite.sut)
	log.SetLevel(log.DebugLevel)
}

func (suite *SimulationTimeHandler_Test) TearDownTest() {
	log.SetHandler(nil)
	suite.baseHandler = nil
	suite.timer = nil
	suite.sut = nil
}

// Tests

func (suite *SimulationTimeHandler_Test) Test() {
	logEntryTime := time.Unix(12345, 0)
	simulationTime := time.Unix(23456, 0)
	suite.timer.CurrentTime = simulationTime
	logEntry := &log.Entry{
		Logger:    &log.Logger{},
		Timestamp: logEntryTime,
		Message:   "Message",
		Level:     log.ErrorLevel,
		Fields:    log.Fields{"key": "value"}}
	suite.baseHandler.OnNew("HandleLog", mock.Anything).Run(func(args mock.Arguments) {
		entryArg := args.Get(0).(*log.Entry)
		suite.Assert().Equal(simulationTime, entryArg.Timestamp)
		suite.Assert().Equal(logEntry.Message, entryArg.Message)
		suite.Assert().Equal(logEntry.Fields, entryArg.Fields)
		suite.Assert().Equal(logEntry.Level, entryArg.Level)
		suite.Assert().Equal(logEntry.Logger, entryArg.Logger)
	})
	suite.Assert().Nil(suite.sut.HandleLog(logEntry))
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}
