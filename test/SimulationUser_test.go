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
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simulation"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

type SimulationUser_Test struct {
	suite.Suite
	sut       *simulation.User
	scheduler *simulation.EventScheduler
	node      *mocked.Node
}

func TestSimulationUser(t *testing.T) {
	suite.Run(t, new(SimulationUser_Test))
}

var startupDuration = general.ParseDuration("8h")
var onlineDuration = general.ParseDuration("2h")
var actionDuration = general.ParseDuration("45m")
var stopDuration = general.ParseDuration("1000h")

func (suite *SimulationUser_Test) SetupTest() {
	suite.scheduler = &simulation.EventScheduler{}
	suite.node = mocked.NewNode("nodeId")
	suite.sut = &simulation.User{
		Scheduler:       suite.scheduler,
		Node:            suite.node,
		StartupDuration: mocked.NewDurationRandVar(startupDuration),
		OnlineDuration:  mocked.NewDurationRandVar(onlineDuration),
		ActionDuration:  mocked.NewDurationRandVar(actionDuration),
		OnlineAction:    func(*simulation.User) {}}
	suite.sut.Activate()
	suite.scheduler.Schedule(stopDuration, func() { panic("Simulation did not stop") })
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
	suite.sut.OnlineAction = func(user *simulation.User) {
		suite.Assert().Equal(suite.sut, user)
		suite.sut.Node.Push(nil, nil)
	}
	expectedActionTime := startupDuration + actionDuration
	suite.assertScheduledAfter(expectedActionTime, "Push", mock.Anything, mock.Anything)
	suite.scheduler.Run()
	suite.node.AssertNumberOfCalls(suite.T(), "Startup", 1)
}

func (suite *SimulationUser_Test) TestOnlineActionIsRepeatedWhenOnline() {
	suite.sut.OnlineAction = func(user *simulation.User) {
		suite.Assert().Equal(suite.sut, user)
		suite.sut.Node.Push(nil, nil)
	}
	repeatedActionTime := startupDuration + 2*actionDuration
	suite.assertScheduledAfter(repeatedActionTime, "Push", mock.Anything, mock.Anything)
	suite.scheduler.Run()
	suite.node.AssertNumberOfCalls(suite.T(), "Push", 2)
}

func (suite *SimulationUser_Test) TestOnlineActionIsNotExecutedWhenOffline() {
	suite.sut.OnlineAction = func(user *simulation.User) {
		suite.Assert().Equal(suite.sut, user)
		suite.sut.Node.Push(nil, nil)
	}
	nextOnlineTime := 2*startupDuration + onlineDuration
	suite.assertScheduledAfter(nextOnlineTime, "Startup", mock.Anything, mock.Anything)
	suite.scheduler.Run()
	suite.node.AssertNumberOfCalls(suite.T(), "Push", 2)
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
