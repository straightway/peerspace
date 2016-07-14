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

	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/simulation"
	"github.com/stretchr/testify/suite"
)

type SimulationEventScheduler_Test struct {
	suite.Suite
	sut *simulation.EventScheduler
}

func TestSimulationEventScheduler(t *testing.T) {
	suite.Run(t, new(SimulationEventScheduler_Test))
}

func (suite *SimulationEventScheduler_Test) SetupTest() {
	suite.sut = &simulation.EventScheduler{}
}

func (suite *SimulationEventScheduler_Test) TearDownTest() {
	suite.sut = nil
}

func (suite *SimulationEventScheduler_Test) Test_Run_OnInitialStateDoesNothing() {
	suite.sut.Run()
}

func (suite *SimulationEventScheduler_Test) Test_Run_ExecutesPreScheduledEvent() {
	wasCalled := false
	suite.sut.Schedule(general.ParseDuration("10s"), func() { wasCalled = true })
	suite.Assert().False(wasCalled)
	suite.sut.Run()
	suite.Assert().True(wasCalled)
}

func (suite *SimulationEventScheduler_Test) Test_Run_ActionAtScheduledTime() {
	eventDuration := general.ParseDuration("10s")
	suite.sut.Schedule(eventDuration, func() {
		suite.Assert().Equal(time.Time{}.Add(eventDuration), suite.sut.Time())
	})
	suite.sut.Run()
}

func (suite *SimulationEventScheduler_Test) Test_Run_AdvancesTimeWithEvent() {
	eventDuration := general.ParseDuration("10s")
	suite.sut.Schedule(eventDuration, func() {})
	suite.Assert().Zero(suite.sut.Time())
	suite.sut.Run()
	suite.Assert().Equal(time.Time{}.Add(eventDuration), suite.sut.Time())
}

func (suite *SimulationEventScheduler_Test) Test_Run_ExecutesEventsInTimedOrder() {
	eventIndex := 0
	duration2 := general.ParseDuration("15s")
	suite.sut.Schedule(duration2, func() {
		suite.Assert().Equal(1, eventIndex)
		suite.Assert().Equal(time.Time{}.Add(duration2), suite.sut.Time())
		eventIndex++
	})
	duration1 := general.ParseDuration("10s")
	suite.sut.Schedule(duration1, func() {
		suite.Assert().Equal(0, eventIndex)
		suite.Assert().Equal(time.Time{}.Add(duration1), suite.sut.Time())
		eventIndex++
	})
	suite.sut.Run()
	suite.Assert().Equal(2, eventIndex)
}

func (suite *SimulationEventScheduler_Test) Test_Schedule_ChainedEvents() {
	eventDuration := general.ParseDuration("10s")
	wasCalled := false
	suite.sut.Schedule(eventDuration, func() {
		suite.sut.Schedule(eventDuration, func() { wasCalled = true })
	})
	suite.sut.Run()
	suite.Assert().True(wasCalled)
}

func (suite *SimulationEventScheduler_Test) Test_Stop_StopsAfterCurrentEvent() {
	suite.sut.Schedule(general.ParseDuration("10s"), func() {
		suite.sut.Stop()
	})
	suite.sut.Schedule(general.ParseDuration("20s"), func() {
		suite.Assert().Fail("Should not be executed as the scheduler is stopped before")
	})
	suite.sut.Run()
}
