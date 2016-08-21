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

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/duration"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simc"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

type SimulationUser_Test struct {
	suite.Suite
	sut                  *simc.User
	scheduler            *simc.EventScheduler
	node                 *mocked.Node
	activity             *mocked.SimulationUserActivity
	querySampleCollector *mocked.SimulationMeasureSampleCollector
}

func TestSimulationUser(t *testing.T) {
	suite.Run(t, new(SimulationUser_Test))
}

var startupDuration = duration.Parse("8h")
var stopDuration = duration.Parse("1000h")

func (suite *SimulationUser_Test) SetupTest() {
	suite.scheduler = &simc.EventScheduler{}
	suite.node = mocked.NewNode("nodeId")
	suite.activity = mocked.NewSimulationUserActivity()
	suite.querySampleCollector = mocked.NewSimulationMeasureSampleCollector()
	suite.sut = &simc.User{
		SchedulerInstance:    suite.scheduler,
		NodeInstance:         suite.node,
		StartupDuration:      mocked.NewDurationRandVar(startupDuration),
		OnlineDuration:       mocked.NewDurationRandVar(onlineDuration),
		OnlineActivity:       suite.activity,
		QuerySampleCollector: suite.querySampleCollector}
	suite.sut.Activate()
	suite.scheduler.Schedule(stopDuration, func() {
		panic("Simulation did not stop")
	})
}

func (suite *SimulationUser_Test) TearDownTest() {
	suite.sut = nil
	suite.scheduler = nil
	suite.node = nil
	suite.querySampleCollector = nil
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
	var identifyable id.Holder = suite.sut
	suite.Assert().Equal("UserOf_"+suite.sut.Node().Id(), identifyable.Id())
}

func (suite *SimulationUser_Test) TestUserCanBeAttractedToData() {
	query := data.Query{Id: queryId}
	suite.sut.AttractTo(query)

	poppedQuery, ok := suite.sut.PopAttractiveQuery()
	suite.Assert().True(ok)
	suite.Assert().Equal(query, poppedQuery)

	poppedQuery, ok = suite.sut.PopAttractiveQuery()
	suite.Assert().False(ok)
	suite.Assert().Equal(data.Query{}, poppedQuery)
}

func (suite *SimulationUser_Test) Test_Equal_UsersWithSameIdAreEqual() {
	user1 := &simc.User{NodeInstance: mocked.NewNode("nodeId")}
	user2 := &simc.User{NodeInstance: mocked.NewNode("nodeId")}
	suite.Assert().True(user1.Equal(user2))
}

func (suite *SimulationUser_Test) Test_Equal_UsersWithDifferentIdsAreNotEqual() {
	user1 := &simc.User{NodeInstance: mocked.NewNode("nodeId1")}
	user2 := &simc.User{NodeInstance: mocked.NewNode("nodeId2")}
	suite.Assert().False(user1.Equal(user2))
}

func (suite *SimulationUser_Test) Test_Equal_UsersDifferFromOtherTypesInstances() {
	user := &simc.User{NodeInstance: mocked.NewNode("nodeId")}
	other := mocked.NewNode("nodeId")
	suite.Assert().False(user.Equal(other))
}

func (suite *SimulationUser_Test) Test_Push_AddsToQueryTimeSampleCollector() {
	query := data.Query{Id: untimedKey.Id}
	suite.sut.AttractTo(query)
	_, _ = suite.sut.PopAttractiveQuery()
	d := duration.Parse("10m")
	suite.scheduler.Schedule(d, func() {
		suite.sut.Push(&untimedChunk, nil)
		suite.scheduler.Stop()
	})
	suite.scheduler.Run()
	suite.querySampleCollector.AssertCalledOnce(suite.T(), "AddSample", d.Seconds())
}

func (suite *SimulationUser_Test) Test_Push_AddsToQueryTimeSampleCollectorOnlyForMatchingQueries() {
	query := data.Query{Id: untimedKey.Id}
	otherQuery := data.Query{Id: otherId}
	suite.sut.AttractTo(query)
	suite.sut.AttractTo(otherQuery)
	_, _ = suite.sut.PopAttractiveQuery()
	_, _ = suite.sut.PopAttractiveQuery()
	suite.sut.Push(&untimedChunk, nil)
	suite.querySampleCollector.AssertCalledOnce(suite.T(), "AddSample", mock.Anything)
}

func (suite *SimulationUser_Test) Test_Push_SamplesQueryDurationOnlyOnce() {
	query := data.Query{Id: untimedKey.Id}
	suite.sut.AttractTo(query)
	_, _ = suite.sut.PopAttractiveQuery()
	suite.sut.Push(&untimedChunk, nil)
	suite.sut.Push(&untimedChunk, nil)
	suite.querySampleCollector.AssertCalledOnce(suite.T(), "AddSample", mock.Anything)
}

func (suite *SimulationUser_Test) Test_Push_LeavesOtherQueriesActive() {
	otherQuery1 := data.Query{Id: otherId + "1"}
	query := data.Query{Id: untimedKey.Id}
	otherQuery2 := data.Query{Id: otherId + "2"}
	suite.sut.AttractTo(otherQuery1)
	suite.sut.AttractTo(query)
	suite.sut.AttractTo(otherQuery2)
	_, _ = suite.sut.PopAttractiveQuery()
	_, _ = suite.sut.PopAttractiveQuery()
	_, _ = suite.sut.PopAttractiveQuery()
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: otherId + "1"}}, nil)
	suite.sut.Push(&untimedChunk, nil)
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: otherId + "2"}}, nil)
	suite.querySampleCollector.AssertNumberOfCalls(suite.T(), "AddSample", 3)
}

func (suite *SimulationUser_Test) Test_Push_UnqueriedDataChunksAreNotSampled() {
	query := data.Query{Id: untimedKey.Id}
	suite.sut.AttractTo(query)
	_, _ = suite.sut.PopAttractiveQuery()
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: otherId}}, nil)
	suite.querySampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
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
