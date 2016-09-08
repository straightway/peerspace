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

package test

import (
	"testing"
	"time"

	"github.com/straightway/straightway/general/duration"
	"github.com/straightway/straightway/simc"
	"github.com/stretchr/testify/suite"
)

type SimulationEventScheduler_Test struct {
	suite.Suite
	sut *simc.EventScheduler
}

const timeFormat = "2006-01-02 15:04:05"

func TestSimulationEventScheduler(t *testing.T) {
	suite.Run(t, new(SimulationEventScheduler_Test))
}

func (suite *SimulationEventScheduler_Test) SetupTest() {
	suite.sut = &simc.EventScheduler{}
}

func (suite *SimulationEventScheduler_Test) TearDownTest() {
	suite.sut = nil
}

// Tests

func (suite *SimulationEventScheduler_Test) Test_Run_OnInitialStateDoesNothing() {
	suite.sut.Run()
}

func (suite *SimulationEventScheduler_Test) Test_Run_ExecutesPreScheduledEvent() {
	wasCalled := false
	suite.sut.Schedule(duration.Parse("10s"), func() { wasCalled = true })
	suite.Assert().False(wasCalled)
	suite.sut.Run()
	suite.Assert().True(wasCalled)
}

func (suite *SimulationEventScheduler_Test) Test_Run_ActionAtScheduledTime() {
	eventDuration := duration.Parse("10s")
	suite.sut.Schedule(eventDuration, func() {
		suite.Assert().Equal(time.Time{}.In(time.UTC).Add(eventDuration), suite.sut.Time())
	})
	suite.sut.Run()
}

func (suite *SimulationEventScheduler_Test) Test_Run_AdvancesTimeWithEvent() {
	eventDuration := duration.Parse("10s")
	suite.sut.Schedule(eventDuration, func() {})
	suite.Assert().Equal(time.Time{}.In(time.UTC), suite.sut.Time())
	suite.sut.Run()
	suite.Assert().Equal(time.Time{}.In(time.UTC).Add(eventDuration), suite.sut.Time())
}

func (suite *SimulationEventScheduler_Test) Test_Run_ExecutesEventsInTimedOrder() {
	eventIndex := 0
	duration2 := duration.Parse("15s")
	suite.sut.Schedule(duration2, func() {
		suite.Assert().Equal(1, eventIndex)
		suite.Assert().Equal(time.Time{}.In(time.UTC).Add(duration2), suite.sut.Time())
		eventIndex++
	})
	duration1 := duration.Parse("10s")
	suite.sut.Schedule(duration1, func() {
		suite.Assert().Equal(0, eventIndex)
		suite.Assert().Equal(time.Time{}.In(time.UTC).Add(duration1), suite.sut.Time())
		eventIndex++
	})
	suite.sut.Run()
	suite.Assert().Equal(2, eventIndex)
}

func (suite *SimulationEventScheduler_Test) Test_Schedule_ChainedEvents() {
	eventDuration := duration.Parse("10s")
	wasCalled := false
	suite.sut.Schedule(eventDuration, func() {
		suite.sut.Schedule(eventDuration, func() { wasCalled = true })
	})
	suite.sut.Run()
	suite.Assert().True(wasCalled)
}

func (suite *SimulationEventScheduler_Test) Test_Schedule_IgnoresNegativeTimes() {
	eventDuration := time.Duration(-1)
	suite.sut.Schedule(eventDuration, func() {
		panic("Shall not be called")
	})
	suite.sut.Run()
}

func (suite *SimulationEventScheduler_Test) Test_ScheduleAbsolute() {
	wasCalled := false
	targetDateTime, err := time.Parse(timeFormat, "2000-01-01 08:45:13")
	suite.Assert().Nil(err)
	suite.sut.ScheduleAbsolute(targetDateTime, func() {
		wasCalled = true
		suite.Assert().Equal(targetDateTime, suite.sut.Time())
	})

	suite.sut.Run()

	suite.Assert().True(wasCalled)
}

func (suite *SimulationEventScheduler_Test) Test_Stop_StopsAfterCurrentEvent() {
	suite.sut.Schedule(duration.Parse("10s"), func() {
		suite.sut.Stop()
	})
	suite.sut.Schedule(duration.Parse("20s"), func() {
		suite.Assert().Fail("Should not be executed as the scheduler is stopped before")
	})
	suite.sut.Run()
}

func (suite *SimulationEventScheduler_Test) Test_Stop_IgnoresFollowingEvents() {
	suite.sut.Schedule(duration.Parse("10s"), func() {
		suite.sut.Stop()
		suite.sut.Schedule(duration.Parse("20s"), func() {
			suite.Assert().Fail("Should not be executed as the scheduler is stopped before")
		})
	})
	suite.sut.Run()
}

func (suite *SimulationEventScheduler_Test) Test_Reset_ClearsRemainingEvents() {
	suite.sut.Schedule(duration.Parse("20s"), func() {
		suite.Assert().Fail("Should not be executed as the scheduler is stopped before")
	})
	suite.sut.Reset()
	suite.sut.Run()
}

func (suite *SimulationEventScheduler_Test) Test_Reset_ResumesForNewEvents() {
	suite.sut.Schedule(time.Duration(0), func() {
		suite.Assert().Fail("Should not be executed as the scheduler is stopped before")
	})
	suite.sut.Stop()
	suite.sut.Reset()
	isExecuted := false
	suite.sut.Schedule(time.Duration(1), func() { isExecuted = true })
	suite.Assert().True(suite.sut.ExecNext())
	suite.Assert().True(isExecuted)
}

func (suite *SimulationEventScheduler_Test) Test_ExecNext_ReturnsFalseWithoutEvents() {
	suite.Assert().False(suite.sut.ExecNext())
}

func (suite *SimulationEventScheduler_Test) Test_ExecNext_TriggersNoExecEventWithoutSimulationEvents() {
	suite.sut.RegisterForExecEvent(func() { suite.Fail("Unexpected event") })
	suite.Assert().NotPanics(func() { suite.sut.ExecNext() })
}

func (suite *SimulationEventScheduler_Test) Test_ExecNext_ReturnsTrueWithEvents() {
	suite.sut.Schedule(time.Duration(0), func() {})
	suite.Assert().True(suite.sut.ExecNext())
}

func (suite *SimulationEventScheduler_Test) Test_ExecNext_TriggersExecEventWithSimulationEvents() {
	execEventHandlerCalls := 0
	suite.sut.RegisterForExecEvent(func() { execEventHandlerCalls++ })
	suite.sut.RegisterForExecEvent(func() { execEventHandlerCalls++ })
	suite.sut.Schedule(time.Duration(0), func() {})
	suite.sut.ExecNext()
	suite.Assert().Equal(2, execEventHandlerCalls)
}
func (suite *SimulationEventScheduler_Test) Test_ExecNext_ExecutesNextEvent() {
	isExecuted := false
	suite.sut.Schedule(time.Duration(0), func() { isExecuted = true })
	suite.sut.ExecNext()
	suite.Assert().True(isExecuted)
}

func (suite *SimulationEventScheduler_Test) Test_ExecNext_ExecutesOneEvent() {
	suite.sut.Schedule(time.Duration(0), func() {})
	suite.sut.Schedule(time.Duration(1), func() {
		suite.Assert().Fail("Second event should not be executed")
	})
	suite.Assert().True(suite.sut.ExecNext())
}

func (suite *SimulationEventScheduler_Test) Test_ExecNext_SetsTime() {
	duration := time.Duration(1)
	expectedExecutionTime := suite.sut.Time().Add(duration)
	suite.sut.Schedule(duration, func() {})
	suite.sut.ExecNext()
	suite.Assert().Equal(expectedExecutionTime, suite.sut.Time())
}

func (suite *SimulationEventScheduler_Test) Test_ExecNext_ExecutesNotIfStopped() {
	suite.sut.Schedule(time.Duration(0), func() {
		suite.Assert().Fail("Event should not be executed")
	})
	suite.sut.Stop()
	suite.Assert().False(suite.sut.ExecNext())
}

func (suite *SimulationEventScheduler_Test) Test_ExecNext_ExecutesNextEventIsStoppedAndResumed() {
	isExecuted := false
	suite.sut.Schedule(time.Duration(0), func() { isExecuted = true })
	suite.sut.Stop()
	suite.sut.Resume()
	suite.sut.ExecNext()
	suite.Assert().True(isExecuted)
}
