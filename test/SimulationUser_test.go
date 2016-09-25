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
	"io/ioutil"
	"log"
	"os"
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
	sut                          *simc.User
	scheduler                    *simc.EventScheduler
	node                         *mocked.Node
	activity                     *mocked.SimulationUserActivity
	queryDurationSampleCollector *mocked.SimulationMeasureSampleCollector
	querySuccessSampleCollector  *mocked.SimulationMeasureSampleCollector
	querySelector                *mocked.SimulationRandVarIntner
}

func TestSimulationUser(t *testing.T) {
	suite.Run(t, new(SimulationUser_Test))
}

var startupDuration = duration.Parse("8h")
var stopDuration = duration.Parse("1000h")
var queryTimeout = duration.Parse("1h")

func (suite *SimulationUser_Test) SetupTest() {
	log.SetOutput(ioutil.Discard)
	suite.scheduler = &simc.EventScheduler{}
	suite.node = mocked.NewNode("nodeId")
	suite.activity = mocked.NewSimulationUserActivity()
	suite.queryDurationSampleCollector = mocked.NewSimulationMeasureSampleCollector()
	suite.querySuccessSampleCollector = mocked.NewSimulationMeasureSampleCollector()
	suite.querySelector = mocked.NewSimulationRandVarIntner(0)
	suite.sut = &simc.User{
		SchedulerInstance:            suite.scheduler,
		NodeInstance:                 suite.node,
		StartupDuration:              mocked.NewDurationRandVar(startupDuration),
		OnlineDuration:               mocked.NewDurationRandVar(onlineDuration),
		OnlineActivity:               suite.activity,
		QueryDurationSampleCollector: suite.queryDurationSampleCollector,
		QuerySuccessSampleCollector:  suite.querySuccessSampleCollector,
		QueryWaitingTimeout:          queryTimeout,
		QuerySelectionSelector:       suite.querySelector}
	suite.sut.Activate()
	suite.scheduler.Schedule(stopDuration, func() {
		panic("Simulation did not stop")
	})
}

func (suite *SimulationUser_Test) TearDownTest() {
	suite.sut = nil
	suite.scheduler = nil
	suite.node = nil
	suite.queryDurationSampleCollector = nil
	log.SetOutput(os.Stderr)
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
	suite.runOneOnlineOfflineCycle()
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
	suite.generatedPendingQuery(untimedKey.Id)
	d := duration.Parse("10m")
	suite.scheduler.Schedule(d, func() {
		suite.sut.Push(&untimedChunk, nil)
		suite.scheduler.Stop()
	})
	suite.scheduler.Run()
	suite.queryDurationSampleCollector.AssertCalledOnce(suite.T(), "AddSample", d.Seconds())
}

func (suite *SimulationUser_Test) Test_Push_AddsToQueryTimeSampleCollectorOnlyForMatchingQueries() {
	suite.generatedPendingQuery(untimedKey.Id)
	suite.generatedPendingQuery(otherId)
	suite.sut.Push(&untimedChunk, nil)
	suite.queryDurationSampleCollector.AssertCalledOnce(suite.T(), "AddSample", mock.Anything)
}

func (suite *SimulationUser_Test) Test_Push_SamplesQueryDurationOnlyOnce() {
	suite.generatedPendingQuery(untimedKey.Id)
	suite.sut.Push(&untimedChunk, nil)
	suite.sut.Push(&untimedChunk, nil)
	suite.queryDurationSampleCollector.AssertCalledOnce(suite.T(), "AddSample", mock.Anything)
}

func (suite *SimulationUser_Test) Test_Push_LeavesOtherQueriesActive() {
	suite.generatedPendingQuery(otherId + "1")
	suite.generatedPendingQuery(untimedKey.Id)
	suite.generatedPendingQuery(otherId + "2")
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: otherId + "1"}}, nil)
	suite.sut.Push(&untimedChunk, nil)
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: otherId + "2"}}, nil)
	suite.queryDurationSampleCollector.AssertNumberOfCalls(suite.T(), "AddSample", 3)
}

func (suite *SimulationUser_Test) Test_Push_UnqueriedDataChunksAreNotSampled() {
	suite.generatedPendingQuery(untimedKey.Id)
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: otherId}}, nil)
	suite.queryDurationSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
}

func (suite *SimulationUser_Test) Test_Shutdown_SamplesAllPendingQueriesAsFailed() {
	suite.sut.QueryWaitingTimeout = duration.Parse("1000h")
	suite.generatedPendingQuery(untimedKey.Id)
	suite.runOneOnlineOfflineCycle()
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *SimulationUser_Test) Test_Shutdown_DiscardsAllPendingQueries() {
	suite.generatedPendingQuery(untimedKey.Id)
	suite.runOneOnlineOfflineCycle()
	suite.querySuccessSampleCollector.Calls = nil
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: untimedKey.Id}}, nil)
	suite.querySuccessSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
}

func (suite *SimulationUser_Test) Test_PendingQueryIsSampledAsFailureAfterTimeout() {
	suite.sut.QueryWaitingTimeout = duration.Parse("1h")
	suite.generatedPendingQuery(untimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("2h"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *SimulationUser_Test) Test_NotPendingQueryIsNotSampled() {
	suite.sut.QueryWaitingTimeout = duration.Parse("1h")
	suite.generatedPendingQuery(untimedKey.Id)
	suite.sut.Push(&untimedChunk, nil)
	suite.querySuccessSampleCollector.Calls = nil
	suite.advanceSimulationTimeBy(duration.Parse("2h"))
	suite.querySuccessSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
}

func (suite *SimulationUser_Test) Test_DiscardedQueryIsNotSampledAsSuccessWithFollowingPush() {
	suite.sut.QueryWaitingTimeout = duration.Parse("1h")
	suite.generatedPendingQuery(untimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("2h"))
	suite.querySuccessSampleCollector.Calls = nil
	suite.sut.Push(&untimedChunk, nil)
	suite.querySuccessSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
}

func (suite *SimulationUser_Test) Test_TwoExpiredQueryAreBothSampledAsFailed() {
	suite.sut.QueryWaitingTimeout = duration.Parse("60m")
	suite.generatedPendingQuery(untimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("10m"))
	suite.generatedPendingQuery(otherId)
	suite.advanceSimulationTimeBy(duration.Parse("51m"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
	suite.querySuccessSampleCollector.Calls = nil
	suite.advanceSimulationTimeBy(duration.Parse("10m"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *SimulationUser_Test) Test_SameQueryArrivingAgainRestartsTimeout() {
	suite.sut.QueryWaitingTimeout = duration.Parse("60m")
	suite.generatedPendingQuery(untimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("10m"))
	suite.generatedPendingQuery(untimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("51m"))
	suite.querySuccessSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
	suite.querySuccessSampleCollector.Calls = nil
	suite.advanceSimulationTimeBy(duration.Parse("10m"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *SimulationUser_Test) Test_OtherNotExpiredQueryRemains() {
	suite.sut.QueryWaitingTimeout = duration.Parse("60m")
	suite.generatedPendingQuery(untimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("30m"))
	suite.generatedPendingQuery(otherId)
	suite.advanceSimulationTimeBy(duration.Parse("31m"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *SimulationUser_Test) Test_PopAttractiveQuery_PicksRandomItem() {
	suite.generatedAttractiveQuery(otherId + "0")
	suite.generatedAttractiveQuery(otherId + "1")
	suite.generatedAttractiveQuery(otherId + "2")
	suite.querySelector.OnNew("Intn", 3)
	suite.querySelector.SetValues(1)
	query, _ := suite.sut.PopAttractiveQuery()
	suite.Assert().Equal(otherId+"1", query.Id)
}

// Private

func (suite *SimulationUser_Test) generatedPendingQuery(id string) {
	suite.generatedAttractiveQuery(id)
	_, _ = suite.sut.PopAttractiveQuery()
}

func (suite *SimulationUser_Test) generatedAttractiveQuery(id string) {
	query := data.Query{Id: id}
	suite.sut.AttractTo(query)
}

func (suite *SimulationUser_Test) runOneOnlineOfflineCycle() {
	suite.advanceSimulationTimeBy(startupDuration + onlineDuration + 1)
}

func (suite *SimulationUser_Test) advanceSimulationTimeBy(duration time.Duration) {
	suite.scheduler.Schedule(duration, func() { suite.scheduler.Stop() })
	suite.scheduler.Run()
}

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
