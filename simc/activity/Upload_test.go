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

package activity

import (
	"math/rand"
	"testing"
	"time"

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/duration"
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/sim/randvar"
	"github.com/straightway/straightway/simc"
	"github.com/straightway/straightway/simc/test"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

type Upload_Test struct {
	TestBase
	sut             *Upload
	rawStorage      *simc.RawStorage
	sizeRandVar     *randvar.Float64Mock
	durationRandVar *randvar.DurationMock
}

func TestUpload(t *testing.T) {
	suite.Run(t, new(Upload_Test))
}

const (
	defaultDataSize = uint64(1000)
)

var (
	activityDuration = test.OnlineDuration/3 + 10
)

func (suite *Upload_Test) SetupTest() {
	suite.TestBase.SetupTest()
	suite.rawStorage = &simc.RawStorage{}
	suite.sizeRandVar = randvar.NewFloat64Mock(float64(defaultDataSize))
	suite.durationRandVar = randvar.NewDurationMock(activityDuration)
	suite.scheduler.Schedule(duration.Parse("1000h"), func() {
		suite.scheduler.Stop()
	})
	randSource := rand.NewSource(12345)
	suite.sut = &Upload{
		User:               suite.user,
		Configuration:      app.DefaultConfiguration(),
		Delay:              suite.durationRandVar,
		DataSize:           suite.sizeRandVar,
		IdGenerator:        &simc.IdGenerator{RandSource: randSource},
		ChunkCreator:       suite.rawStorage,
		AttractionRatio:    randvar.NewFloat64Mock(1.0),
		AudienceProvider:   sim.NewAudienceProviderMock(),
		AudiencePermutator: rand.New(randSource)}
}

func (suite *Upload_Test) TearDownTest() {
	suite.TestBase.TearDownTest()
	suite.sut = nil
	suite.rawStorage = nil
	suite.durationRandVar = nil
	suite.sizeRandVar = nil
}

// Tests

func (suite *Upload_Test) Test_ScheduleUntil_SchedulesPushAction() {
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.node.AssertNotCalled(suite.T(), "Push", mock.Anything, mock.Anything)
	suite.scheduler.Run()
	suite.node.AssertCalled(suite.T(), "Push", mock.Anything, suite.user)
}

func (suite *Upload_Test) Test_ScheduleUntil_PushesDataOfConfiguredSize() {
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		suite.Assert().Equal(defaultDataSize, suite.rawStorage.SizeOf(chunk))
	})
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
	suite.node.AssertCalled(suite.T(), "Push", mock.Anything, suite.user)
}

func (suite *Upload_Test) Test_ScheduleUntil_PushesDataAtDefinedTimes() {
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

func (suite *Upload_Test) Test_ScheduleUntil_AnnouncesPushedChunksToAudience() {
	var expectedQueries []data.Query = nil
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		expectedQueries = append(expectedQueries, data.Query{Id: chunk.Key.Id})
	})
	consumer := sim.NewDataConsumerMock()
	suite.addAudience(consumer)
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
	for _, q := range expectedQueries {
		consumer.AssertCalled(suite.T(), "AttractTo", q)
	}
}

func (suite *Upload_Test) Test_ScheduleUntil_AnnouncesPushedChunksToPartialAudience() {
	suite.assertPartialAttaction(0.5, 10, 5)
}

func (suite *Upload_Test) Test_ScheduleUntil_DoesNotAnnouncePushedChunksIfAtteactionRatioIsBelowZero() {
	suite.assertPartialAttaction(-0.1, 10, 0)
}

func (suite *Upload_Test) Test_ScheduleUntil_AnnouncesPushedChunksToAllIfAttractionRatioIsAboveOne() {
	suite.assertPartialAttaction(1.1, 10, 10)
}

func (suite *Upload_Test) Test_ScheduleUntil_AttractedParialAudienceIsRandom() {
	consumers := suite.createConsumers(10)

	attractionRatio := 0.5
	suite.sut.AttractionRatio = randvar.NewFloat64Mock(attractionRatio)
	suite.sut.AudiencePermutator = randvar.NewPermutatorMock(1, 3, 5, 7, 9, 0, 2, 4, 6, 8)

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

func (suite *Upload_Test) Test_ScheduleUntil_CreatesUntimedUniqueKeysForPushedChunks() {
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

func (suite *Upload_Test) Test_ScheduleUntil_DoesNotCreateTooLargeChunks() {
	maxChunkSize := uint64(suite.sut.Configuration.MaxChunkSize)
	suite.sut.DataSize = randvar.NewFloat64Mock(float64(maxChunkSize + 1))
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		suite.Assert().True(suite.rawStorage.SizeOf(chunk) <= maxChunkSize)
	})
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
}

func (suite *Upload_Test) Test_ScheduleUntil_DoesNotCreateEmptyChunks() {
	suite.sut.DataSize = randvar.NewFloat64Mock(float64(0))
	suite.node.OnNew("Push", mock.Anything, suite.user).Run(func(args mock.Arguments) {
		chunk := args[0].(*data.Chunk)
		suite.Assert().True(0 < suite.rawStorage.SizeOf(chunk))
	})
	suite.sut.ScheduleUntil(suite.offlineTime)
	suite.scheduler.Run()
}

// Private

func (suite *Upload_Test) assertPartialAttaction(
	ratio float64, numConsumers int, expectedAttraction int) {
	consumers := suite.createConsumers(numConsumers)

	suite.sut.AttractionRatio = randvar.NewFloat64Mock(ratio)

	suite.sut.ScheduleUntil(suite.scheduler.Time().Add(activityDuration))
	suite.scheduler.Run()

	numberOfAttractions := 0
	for _, consumer := range consumers {
		if consumer.WasCalled("AttractTo") {
			numberOfAttractions++
		}
	}

	suite.Assert().Equal(expectedAttraction, numberOfAttractions)
}

func (suite *Upload_Test) createConsumers(count int) (consumers []*sim.DataConsumerMock) {
	for i := 0; i < count; i++ {
		consumer := sim.NewDataConsumerMock()
		suite.addAudience(consumer)
		consumers = append(consumers, consumer)
	}

	return
}

func (suite *Upload_Test) addAudience(consumer sim.DataConsumer) {
	audience := append(suite.sut.AudienceProvider.Audience(), consumer)
	suite.sut.AudienceProvider = sim.NewAudienceProviderMock(audience...)
}
