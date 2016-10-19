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

package simc

import (
	"io/ioutil"
	"log"
	"os"
	"testing"
	"time"

	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/duration"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/sim/measure"
	"github.com/straightway/straightway/sim/randvar"
)

type User_Test struct {
	suite.Suite
	sut                          *User
	scheduler                    *EventScheduler
	node                         *peer.NodeMock
	activity                     *sim.UserActivityMock
	queryDurationSampleCollector *measure.SampleCollectorMock
	querySuccessSampleCollector  *measure.SampleCollectorMock
	querySelector                *randvar.IntnerMock
}

var OnlineDuration = duration.Parse("2h")

func TestUser(t *testing.T) {
	suite.Run(t, new(User_Test))
}

var startupDuration = duration.Parse("8h")
var stopDuration = duration.Parse("1000h")
var queryTimeout = duration.Parse("1h")

func (suite *User_Test) SetupTest() {
	log.SetOutput(ioutil.Discard)
	suite.scheduler = &EventScheduler{}
	suite.node = peer.NewNodeMock("nodeId")
	suite.activity = sim.NewUserActivityMock()
	suite.queryDurationSampleCollector = measure.NewSampleCollectorMock()
	suite.querySuccessSampleCollector = measure.NewSampleCollectorMock()
	suite.querySelector = randvar.NewIntnerMock(0)
	suite.sut = &User{
		SchedulerInstance:            suite.scheduler,
		NodeInstance:                 suite.node,
		StartupDuration:              randvar.NewDurationMock(startupDuration),
		OnlineDuration:               randvar.NewDurationMock(OnlineDuration),
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

func (suite *User_Test) TearDownTest() {
	suite.sut = nil
	suite.scheduler = nil
	suite.node = nil
	suite.queryDurationSampleCollector = nil
	log.SetOutput(os.Stderr)
}

func (suite *User_Test) TestNodeStartupIsScheduled() {
	suite.assertScheduledAfter(startupDuration, "Startup")
	suite.scheduler.Run()
	suite.node.AssertCalledOnce(suite.T(), "Startup")
}

func (suite *User_Test) TestNodeIsShutDown() {
	shutdownTime := startupDuration + OnlineDuration
	suite.assertScheduledAfter(shutdownTime, "ShutDown")
	suite.scheduler.Run()
	suite.node.AssertCalledOnce(suite.T(), "ShutDown")
}

func (suite *User_Test) TestNodeIsRestartedAfterShutDown() {
	restartTime := 2*startupDuration + OnlineDuration
	suite.assertScheduledAfter(restartTime, "Startup")
	suite.scheduler.Run()
	suite.node.AssertNumberOfCalls(suite.T(), "Startup", 2)
}

func (suite *User_Test) TestOnlineActionIsExecutedWhenOnline() {
	shutdownTime := suite.scheduler.Time().Add(startupDuration + OnlineDuration)
	suite.activity.AssertNotCalled(suite.T(), "ScheduleUntil", mock.Anything)
	suite.runOneOnlineOfflineCycle()
	suite.activity.AssertCalledOnce(suite.T(), "ScheduleUntil", shutdownTime)
}

func (suite *User_Test) TestOnlineActionIsNotExecutedWhenOffline() {
	suite.scheduler.Schedule(startupDuration+OnlineDuration+1, func() {
		suite.activity.Calls = nil
	})
	suite.scheduler.Schedule(2*startupDuration+OnlineDuration-1, func() {
		suite.scheduler.Stop()
	})
	suite.scheduler.Run()
	suite.activity.AssertNotCalled(suite.T(), "ScheduleUntil", mock.Anything)
}

func (suite *User_Test) TestUserIsIdentifyable() {
	var identifyable id.Holder = suite.sut
	suite.Assert().Equal("UserOf_"+suite.sut.Node().Id(), identifyable.Id())
}

func (suite *User_Test) TestUserCanBeAttractedToData() {
	query := data.Query{Id: data.QueryId}
	suite.sut.AttractTo(query)

	poppedQuery, ok := suite.sut.PopAttractiveQuery()
	suite.Assert().True(ok)
	suite.Assert().Equal(query, poppedQuery)

	poppedQuery, ok = suite.sut.PopAttractiveQuery()
	suite.Assert().False(ok)
	suite.Assert().Equal(data.Query{}, poppedQuery)
}

func (suite *User_Test) Test_Equal_UsersWithSameIdAreEqual() {
	user1 := &User{NodeInstance: peer.NewNodeMock("nodeId")}
	user2 := &User{NodeInstance: peer.NewNodeMock("nodeId")}
	suite.Assert().True(user1.Equal(user2))
}

func (suite *User_Test) Test_Equal_UsersWithDifferentIdsAreNotEqual() {
	user1 := &User{NodeInstance: peer.NewNodeMock("nodeId1")}
	user2 := &User{NodeInstance: peer.NewNodeMock("nodeId2")}
	suite.Assert().False(user1.Equal(user2))
}

func (suite *User_Test) Test_Equal_UsersDifferFromOtherTypesInstances() {
	user := &User{NodeInstance: peer.NewNodeMock("nodeId")}
	other := peer.NewNodeMock("nodeId")
	suite.Assert().False(user.Equal(other))
}

func (suite *User_Test) Test_Push_AddsToQueryTimeSampleCollector() {
	suite.generatedPendingQuery(data.UntimedKey.Id)
	d := duration.Parse("10m")
	suite.scheduler.Schedule(d, func() {
		suite.sut.Push(&data.UntimedChunk, nil)
		suite.scheduler.Stop()
	})
	suite.scheduler.Run()
	suite.queryDurationSampleCollector.AssertCalledOnce(suite.T(), "AddSample", d.Seconds())
}

func (suite *User_Test) Test_Push_AddsToQueryTimeSampleCollectorOnlyForMatchingQueries() {
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.generatedPendingQuery(data.OtherId)
	suite.sut.Push(&data.UntimedChunk, nil)
	suite.queryDurationSampleCollector.AssertCalledOnce(suite.T(), "AddSample", mock.Anything)
}

func (suite *User_Test) Test_Push_SamplesQueryDurationOnlyOnce() {
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.sut.Push(&data.UntimedChunk, nil)
	suite.sut.Push(&data.UntimedChunk, nil)
	suite.queryDurationSampleCollector.AssertCalledOnce(suite.T(), "AddSample", mock.Anything)
}

func (suite *User_Test) Test_Push_LeavesOtherQueriesActive() {
	suite.generatedPendingQuery(data.OtherId + "1")
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.generatedPendingQuery(data.OtherId + "2")
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: data.OtherId + "1"}}, nil)
	suite.sut.Push(&data.UntimedChunk, nil)
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: data.OtherId + "2"}}, nil)
	suite.queryDurationSampleCollector.AssertNumberOfCalls(suite.T(), "AddSample", 3)
}

func (suite *User_Test) Test_Push_UnqueriedDataChunksAreNotSampled() {
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: data.OtherId}}, nil)
	suite.queryDurationSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
}

func (suite *User_Test) Test_Shutdown_SamplesAllPendingQueriesAsFailed() {
	suite.sut.QueryWaitingTimeout = duration.Parse("1000h")
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.runOneOnlineOfflineCycle()
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *User_Test) Test_Shutdown_DiscardsAllPendingQueries() {
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.runOneOnlineOfflineCycle()
	suite.querySuccessSampleCollector.Calls = nil
	suite.sut.Push(&data.Chunk{Key: data.Key{Id: data.UntimedKey.Id}}, nil)
	suite.querySuccessSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
}

func (suite *User_Test) Test_PendingQueryIsSampledAsFailureAfterTimeout() {
	suite.sut.QueryWaitingTimeout = duration.Parse("1h")
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("2h"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *User_Test) Test_NotPendingQueryIsNotSampled() {
	suite.sut.QueryWaitingTimeout = duration.Parse("1h")
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.sut.Push(&data.UntimedChunk, nil)
	suite.querySuccessSampleCollector.Calls = nil
	suite.advanceSimulationTimeBy(duration.Parse("2h"))
	suite.querySuccessSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
}

func (suite *User_Test) Test_DiscardedQueryIsNotSampledAsSuccessWithFollowingPush() {
	suite.sut.QueryWaitingTimeout = duration.Parse("1h")
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("2h"))
	suite.querySuccessSampleCollector.Calls = nil
	suite.sut.Push(&data.UntimedChunk, nil)
	suite.querySuccessSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
}

func (suite *User_Test) Test_TwoExpiredQueryAreBothSampledAsFailed() {
	suite.sut.QueryWaitingTimeout = duration.Parse("60m")
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("10m"))
	suite.generatedPendingQuery(data.OtherId)
	suite.advanceSimulationTimeBy(duration.Parse("51m"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
	suite.querySuccessSampleCollector.Calls = nil
	suite.advanceSimulationTimeBy(duration.Parse("10m"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *User_Test) Test_SameQueryArrivingAgainRestartsTimeout() {
	suite.sut.QueryWaitingTimeout = duration.Parse("60m")
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("10m"))
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("51m"))
	suite.querySuccessSampleCollector.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
	suite.querySuccessSampleCollector.Calls = nil
	suite.advanceSimulationTimeBy(duration.Parse("10m"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *User_Test) Test_OtherNotExpiredQueryRemains() {
	suite.sut.QueryWaitingTimeout = duration.Parse("60m")
	suite.generatedPendingQuery(data.UntimedKey.Id)
	suite.advanceSimulationTimeBy(duration.Parse("30m"))
	suite.generatedPendingQuery(data.OtherId)
	suite.advanceSimulationTimeBy(duration.Parse("31m"))
	suite.querySuccessSampleCollector.AssertCalledOnce(suite.T(), "AddSample", 0.0)
}

func (suite *User_Test) Test_PopAttractiveQuery_PicksRandomItem() {
	suite.generatedAttractiveQuery(data.OtherId + "0")
	suite.generatedAttractiveQuery(data.OtherId + "1")
	suite.generatedAttractiveQuery(data.OtherId + "2")
	suite.querySelector.OnNew("Intn", 3)
	suite.querySelector.SetValues(1)
	query, _ := suite.sut.PopAttractiveQuery()
	suite.Assert().Equal(data.OtherId+"1", query.Id)
}

// Private

func (suite *User_Test) generatedPendingQuery(id string) {
	suite.generatedAttractiveQuery(id)
	_, _ = suite.sut.PopAttractiveQuery()
}

func (suite *User_Test) generatedAttractiveQuery(id string) {
	query := data.Query{Id: id}
	suite.sut.AttractTo(query)
}

func (suite *User_Test) runOneOnlineOfflineCycle() {
	suite.advanceSimulationTimeBy(startupDuration + OnlineDuration + 1)
}

func (suite *User_Test) advanceSimulationTimeBy(duration time.Duration) {
	suite.scheduler.Schedule(duration, func() { suite.scheduler.Stop() })
	suite.scheduler.Run()
}

func (suite *User_Test) assertScheduledAfter(duration time.Duration, methodName string, arguments ...interface{}) {
	suite.node.AssertNotCalled(suite.T(), methodName, arguments...)
	expectedCallDuration := suite.scheduler.Time().Add(duration)
	suite.node.OnNew(methodName, arguments...).Run(func(mock.Arguments) {
		if false == suite.scheduler.Time().Before(expectedCallDuration) {
			suite.Assert().Equal(expectedCallDuration, suite.scheduler.Time())
			suite.scheduler.Stop()
		}
	})
}
