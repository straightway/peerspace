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
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simulation"
	"github.com/straightway/straightway/simulation/activity"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

type SimulationActivityUpload_Test struct {
	suite.Suite
	sut             *activity.Upload
	scheduler       *simulation.EventScheduler
	user            *simulation.User
	node            *mocked.Node
	rawStorage      *simulation.RawStorage
	sizeRandVar     *mocked.Float64RandVar
	durationRandVar *mocked.DurationRandVar
	offlineTime     time.Time
}

func TestSimulationActivityUpload(t *testing.T) {
	suite.Run(t, new(SimulationActivityUpload_Test))
}

const (
	defaultDataSize = uint64(1000)
)

var (
	activityDuration = onlineDuration/3 + 10
)

func (suite *SimulationActivityUpload_Test) SetupTest() {
	suite.scheduler = &simulation.EventScheduler{}
	suite.rawStorage = &simulation.RawStorage{}
	suite.node = mocked.NewNode("1")
	suite.sizeRandVar = mocked.NewFloat64RandVar(float64(defaultDataSize))
	suite.durationRandVar = mocked.NewDurationRandVar(activityDuration)
	suite.user = &simulation.User{
		Scheduler: suite.scheduler,
		Node:      suite.node}
	suite.scheduler.Schedule(general.ParseDuration("1000h"), func() {
		suite.scheduler.Stop()
	})
	suite.sut = &activity.Upload{
		User:         suite.user,
		Delay:        suite.durationRandVar,
		DataSize:     suite.sizeRandVar,
		ChunkCreator: suite.rawStorage}
	now := suite.scheduler.Time()
	suite.offlineTime = now.Add(onlineDuration)
}

func (suite *SimulationActivityUpload_Test) TearDownTest() {
	suite.sut = nil
	suite.scheduler = nil
	suite.node = nil
	suite.user = nil
	suite.rawStorage = nil
	suite.durationRandVar = nil
	suite.sizeRandVar = nil
}

// Tests

func (suite *SimulationActivityUpload_Test) Test_ScheduleUntil_SchedulesPushAction() {
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.node.AssertNotCalled(suite.T(), "Push", mock.Anything, mock.Anything)
	suite.scheduler.Run()
	suite.node.AssertCalled(suite.T(), "Push", mock.Anything, suite.user)
}

func (suite *SimulationActivityUpload_Test) Test_ScheduleUntil_PushesDataOfConfiguredSize() {
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		suite.Assert().Equal(defaultDataSize, suite.rawStorage.SizeOf(chunk))
	})
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
	suite.node.AssertCalled(suite.T(), "Push", mock.Anything, suite.user)
}

func (suite *SimulationActivityUpload_Test) Test_ScheduleUntil_PushesDataAtDefinedTimes() {
	startTime := suite.scheduler.Time()
	numberOfCalls := 0
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		numberOfCalls++
		expectedCallTime := startTime.Add(activityDuration * time.Duration(numberOfCalls))
		suite.Assert().Equal(expectedCallTime, suite.scheduler.Time())
	})
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
	suite.node.AssertCalled(suite.T(), "Push", mock.Anything, suite.user)
	suite.Assert().Equal(2, numberOfCalls)
}

/*
func (suite *SimulationActivityUpload_Test) Test_ScheduleUntil_AnnouncesPushedChunksToAudience() {

}
*/
