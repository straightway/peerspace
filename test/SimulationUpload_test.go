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
	"math/rand"
	"testing"
	"time"

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/duration"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/simc"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

type SimulationUpload_Test struct {
	SimulationActionTestBase
	sut             *simc.Upload
	rawStorage      *simc.RawStorage
	sizeRandVar     *mocked.Float64RandVar
	durationRandVar *mocked.DurationRandVar
}

func TestSimulationUpload(t *testing.T) {
	suite.Run(t, new(SimulationUpload_Test))
}

const (
	defaultDataSize = uint64(1000)
)

var (
	activityDuration = onlineDuration/3 + 10
)

func (suite *SimulationUpload_Test) SetupTest() {
	suite.SimulationActionTestBase.SetupTest()
	suite.rawStorage = &simc.RawStorage{}
	suite.sizeRandVar = mocked.NewFloat64RandVar(float64(defaultDataSize))
	suite.durationRandVar = mocked.NewDurationRandVar(activityDuration)
	suite.scheduler.Schedule(duration.Parse("1000h"), func() {
		suite.scheduler.Stop()
	})
	randSource := rand.NewSource(12345)
	suite.sut = &simc.Upload{
		User:               suite.user,
		Configuration:      app.DefaultConfiguration(),
		Delay:              suite.durationRandVar,
		DataSize:           suite.sizeRandVar,
		IdGenerator:        &simc.IdGenerator{RandSource: randSource},
		ChunkCreator:       suite.rawStorage,
		AttractionRatio:    mocked.NewFloat64RandVar(1.0),
		AudienceProvider:   mocked.NewSimulationAudienceProvider(),
		AudiencePermutator: rand.New(randSource)}
}

func (suite *SimulationUpload_Test) TearDownTest() {
	suite.SimulationActionTestBase.TearDownTest()
	suite.sut = nil
	suite.rawStorage = nil
	suite.durationRandVar = nil
	suite.sizeRandVar = nil
}

// Tests

func (suite *SimulationUpload_Test) Test_ScheduleUntil_SchedulesPushAction() {
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.node.AssertNotCalled(suite.T(), "Push", mock.Anything, mock.Anything)
	suite.scheduler.Run()
	suite.node.AssertCalled(suite.T(), "Push", mock.Anything, suite.user)
}

func (suite *SimulationUpload_Test) Test_ScheduleUntil_PushesDataOfConfiguredSize() {
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		suite.Assert().Equal(defaultDataSize, suite.rawStorage.SizeOf(chunk))
	})
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
	suite.node.AssertCalled(suite.T(), "Push", mock.Anything, suite.user)
}

func (suite *SimulationUpload_Test) Test_ScheduleUntil_PushesDataAtDefinedTimes() {
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

func (suite *SimulationUpload_Test) Test_ScheduleUntil_AnnouncesPushedChunksToAudience() {
	var expectedQueries []data.Query = nil
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		expectedQueries = append(expectedQueries, data.Query{Id: chunk.Key.Id})
	})
	consumer := mocked.NewSimulationDataConsumer()
	suite.addAudience(consumer)
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
	for _, q := range expectedQueries {
		consumer.AssertCalled(suite.T(), "AttractTo", q)
	}
}

func (suite *SimulationUpload_Test) Test_ScheduleUntil_AnnouncesPushedChunksToPartialAudience() {
	consumers := suite.createConsumers(10)

	attractionRatio := 0.5
	suite.sut.AttractionRatio = mocked.NewFloat64RandVar(attractionRatio)

	suite.sut.ScheduleUntil(suite.scheduler.Time().Add(activityDuration))
	suite.scheduler.Run()

	numberOfAttractions := 0
	for _, consumer := range consumers {
		if consumer.WasCalled("AttractTo") {
			numberOfAttractions++
		}
	}

	expectedNumberOfAttractions := int(float64(len(consumers)) * attractionRatio)
	suite.Assert().Equal(expectedNumberOfAttractions, numberOfAttractions)
}

func (suite *SimulationUpload_Test) Test_ScheduleUntil_AttractedParialAudienceIsRandom() {
	consumers := suite.createConsumers(10)

	attractionRatio := 0.5
	suite.sut.AttractionRatio = mocked.NewFloat64RandVar(attractionRatio)
	suite.sut.AudiencePermutator = mocked.NewSimulationRandVarPermutator(
		[]int{1, 3, 5, 7, 9, 0, 2, 4, 6, 8})

	suite.sut.ScheduleUntil(suite.scheduler.Time().Add(activityDuration))
	suite.scheduler.Run()

	for i, c := range consumers {
		if i%2 == 0 {
			c.AssertNotCalled(suite.T(), "AttractTo", mock.Anything)
		} else {
			c.AssertCalledOnce(suite.T(), "AttractTo", mock.Anything)
		}
	}
}

func (suite *SimulationUpload_Test) Test_ScheduleUntil_CreatesUntimedUniqueKeysForPushedChunks() {
	var lastKey data.Key
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		suite.Assert().Equal(int64(0), chunk.Key.TimeStamp)
		if lastKey.Id != "" {
			suite.Assert().NotEqual(lastKey, chunk.Key)
		}

		lastKey = chunk.Key
	})
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
}

func (suite *SimulationUpload_Test) Test_ScheduleUntil_DoesNotCreateTooLargeChunks() {
	maxChunkSize := uint64(suite.sut.Configuration.MaxChunkSize)
	suite.sut.DataSize = mocked.NewFloat64RandVar(float64(maxChunkSize + 1))
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		suite.Assert().True(suite.rawStorage.SizeOf(chunk) <= maxChunkSize)
	})
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
}

func (suite *SimulationUpload_Test) Test_ScheduleUntil_DoesNotCreateEmptyChunks() {
	suite.sut.DataSize = mocked.NewFloat64RandVar(float64(0))
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		suite.Assert().True(0 < suite.rawStorage.SizeOf(chunk))
	})
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
}

// Private

func (suite *SimulationUpload_Test) createConsumers(count int) (consumers []*mocked.SimulationDataConsumer) {
	for i := 0; i < count; i++ {
		consumer := mocked.NewSimulationDataConsumer()
		suite.addAudience(consumer)
		consumers = append(consumers, consumer)
	}

	return
}

func (suite *SimulationUpload_Test) addAudience(consumer sim.DataConsumer) {
	audience := append(suite.sut.AudienceProvider.Audience(), consumer)
	suite.sut.AudienceProvider = mocked.NewSimulationAudienceProvider(audience...)
}
