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
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simc/activity"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

type SimulationQuery_Test struct {
	SimulationActionTestBase
	sut                *activity.Query
	queryPauseDuration time.Duration
}

func TestSimulationQuery(t *testing.T) {
	suite.Run(t, new(SimulationQuery_Test))
}

func (suite *SimulationQuery_Test) SetupTest() {
	suite.SimulationActionTestBase.SetupTest()
	suite.queryPauseDuration = duration.Parse("30m")
	suite.sut = &activity.Query{
		Scheduler:          suite.scheduler,
		User:               suite.user,
		QueryPauseDuration: mocked.NewDurationRandVar(suite.queryPauseDuration)}
}

func (suite *SimulationQuery_Test) TearDownTest() {
	suite.sut = nil
	suite.SimulationActionTestBase.TearDownTest()
}

// Tests

func (suite *SimulationQuery_Test) Test_ScheduleUntil_SchedulesNoQueryActionIfNoAttractivQueryAreAvailable() {
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
	suite.node.AssertNotCalled(suite.T(), "Query", mock.Anything, mock.Anything)
}

func (suite *SimulationQuery_Test) Test_ScheduleUntil_QueriesAttractiveData() {
	query := data.Query{Id: "1234", TimeFrom: 10, TimeTo: 20}
	suite.user.AttractTo(query)
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.node.AssertNotCalled(suite.T(), "Query", mock.Anything, mock.Anything)
	suite.scheduler.Run()
	suite.node.AssertCalled(suite.T(), "Query", query, suite.user)
}

func (suite *SimulationQuery_Test) Test_ScheduleUntil_QueriesAttractiveDataComingAfterSchedule() {
	query := data.Query{Id: "1234", TimeFrom: 10, TimeTo: 20}
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.user.AttractTo(query)
	suite.scheduler.Run()
	suite.node.AssertCalled(suite.T(), "Query", query, suite.user)
}

func (suite *SimulationQuery_Test) Test_ScheduleUntil_QueriesAtSpecifiedTime() {
	query := data.Query{Id: "1234", TimeFrom: 10, TimeTo: 20}
	suite.user.AttractTo(query)
	suite.sut.ScheduleUntil(suite.offlineTime)
	expectedQueryTime := suite.scheduler.Time().Add(suite.queryPauseDuration)
	suite.node.OnNew("Query", mock.Anything, mock.Anything).Run(func(mock.Arguments) {
		suite.scheduler.Stop()
		suite.Assert().Equal(expectedQueryTime, suite.scheduler.Time())
	})
	suite.scheduler.Run()
	suite.node.AssertCalledOnce(suite.T(), "Query", mock.Anything, mock.Anything)
}

func (suite *SimulationQuery_Test) Test_ScheduleUntil_QueriesUntilOffline() {
	query := data.Query{Id: "1234", TimeFrom: 10, TimeTo: 20}
	for i := 0; i < 5; i++ {
		suite.user.AttractTo(query)
	}
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
	suite.node.AssertNumberOfCalls(suite.T(), "Query", 4)
}
