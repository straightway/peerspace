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
	"github.com/straightway/straightway/impl/simulation"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

type SimulationUser_Test struct {
	suite.Suite
	sut       *simulation.User
	scheduler *simulation.EventScheduler
	node      *mocked.Node
	activity  *mocked.SimulationUserActivity
}

func TestSimulationUser(t *testing.T) {
	suite.Run(t, new(SimulationUser_Test))
}

var startupDuration = general.ParseDuration("8h")
var stopDuration = general.ParseDuration("1000h")

func (suite *SimulationUser_Test) SetupTest() {
	suite.scheduler = &simulation.EventScheduler{}
	suite.node = mocked.NewNode("nodeId")
	suite.activity = mocked.NewSimulationUserActivity()
	suite.sut = &simulation.User{
		Scheduler:       suite.scheduler,
		Node:            suite.node,
		StartupDuration: mocked.NewDurationRandVar(startupDuration),
		OnlineDuration:  mocked.NewDurationRandVar(onlineDuration),
		OnlineActivity:  suite.activity}
	suite.sut.Activate()
	suite.scheduler.Schedule(stopDuration, func() {
		panic("Simulation did not stop")
	})
}

func (suite *SimulationUser_Test) TearDownTest() {
	suite.sut = nil
	suite.scheduler = nil
	suite.node = nil
}

func (suite *SimulationUser_Test) TestNodeStartupIsScheduled() {
	suite.assertScheduledAfter(startupDuration, "Startup")
	suite.scheduler.Run()
	suite.node.AssertCalledOnce(suite.T(), "Startup")
}

func (suite *SimulationUser_Test) TestNodeIsShutDown() {
	shutdownTime := startupDuration + onlineDuration
	suite.assertScheduledAfter(shutdownTime, "ShutDown")
	suite.scheduler.Run()
	suite.node.AssertCalledOnce(suite.T(), "ShutDown")
}

func (suite *SimulationUser_Test) TestNodeIsRestartedAfterShutDown() {
	restartTime := 2*startupDuration + onlineDuration
	suite.assertScheduledAfter(restartTime, "Startup")
	suite.scheduler.Run()
	suite.node.AssertNumberOfCalls(suite.T(), "Startup", 2)
}

func (suite *SimulationUser_Test) TestOnlineActionIsExecutedWhenOnline() {
	shutdownTime := suite.scheduler.Time().Add(startupDuration + onlineDuration)
	suite.activity.AssertNotCalled(suite.T(), "ScheduleUntil", mock.Anything)
	suite.scheduler.Schedule(startupDuration+onlineDuration+time.Duration(1), func() {
		suite.scheduler.Stop()
	})
	suite.scheduler.Run()
	suite.activity.AssertCalledOnce(suite.T(), "ScheduleUntil", shutdownTime)
}

func (suite *SimulationUser_Test) TestOnlineActionIsNotExecutedWhenOffline() {
	suite.scheduler.Schedule(startupDuration+onlineDuration+1, func() {
		suite.activity.Calls = nil
	})
	suite.scheduler.Schedule(2*startupDuration+onlineDuration-1, func() {
		suite.scheduler.Stop()
	})
	suite.scheduler.Run()
	suite.activity.AssertNotCalled(suite.T(), "ScheduleUntil", mock.Anything)
}

func (suite *SimulationUser_Test) TestUserIsIdentifyable() {
	var identifyable general.Identifyable = suite.sut
	suite.Assert().Equal("UserOf_"+suite.sut.Node.Id(), identifyable.Id())
}

func (suite *SimulationUser_Test) TestUserCanBeAttractedToData() {
	query := peer.Query{Id: queryId}
	suite.sut.AttractTo(query)

	poppedQuery, ok := suite.sut.PopAttractiveQuery()
	suite.Assert().True(ok)
	suite.Assert().Equal(query, poppedQuery)

	poppedQuery, ok = suite.sut.PopAttractiveQuery()
	suite.Assert().False(ok)
	suite.Assert().Equal(peer.Query{}, poppedQuery)
}

// Private

func (suite *SimulationUser_Test) assertScheduledAfter(duration time.Duration, methodName string, arguments ...interface{}) {
	suite.node.AssertNotCalled(suite.T(), methodName, arguments...)
	expectedCallDuration := suite.scheduler.Time().Add(duration)
	suite.node.OnNew(methodName, arguments...).Run(func(mock.Arguments) {
		if false == suite.scheduler.Time().Before(expectedCallDuration) {
			suite.Assert().Equal(expectedCallDuration, suite.scheduler.Time())
			suite.scheduler.Stop()
		}
	})
}
