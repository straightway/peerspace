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
	"github.com/straightway/straightway/strategy"
	"github.com/stretchr/testify/suite"
)

const (
	currentTime     = int64(2000000000)
	peerId          = "12345"
	peerHash        = uint64(0x1)
	peerIdNegated   = "-12345"
	peerHashNegated = ^uint64(0x1)
	keyId           = "67890"
	keyHash         = uint64(0x2)
	key2Id          = "78901"
	key2Hash        = uint64(0x3)
)

var recentKey = data.Key{Id: data.Id(keyId), TimeStamp: currentTime}

// Test suite

type PeerDistanceRelated_Test struct {
	suite.Suite
	hasher *mocked.Hash64
	sut    *strategy.PeerDistanceRelated
	peer   *mocked.PeerConnector
	timer  *mocked.Timer
}

func TestPeerDistanceCalculator(t *testing.T) {
	suite.Run(t, new(PeerDistanceRelated_Test))
}

func (suite *PeerDistanceRelated_Test) SetupTest() {
	suite.peer = mocked.CreatePeerConnector()
	suite.peer.Identifier = peerId

	suite.hasher = mocked.NewHash64()
	suite.hasher.SetupHashSum([]byte(peerId), peerHash)
	suite.hasher.SetupHashSum([]byte(peerIdNegated), peerHashNegated)
	suite.hasher.SetupHashSum([]byte(keyId), keyHash)
	suite.hasher.SetupHashSum([]byte(key2Id), key2Hash)
	for i := byte(1); i < 16; i++ {
		suite.hasher.SetupHashSum(append([]byte(keyId), i), keyHash+uint64(i)*0x10)
		suite.hasher.SetupHashSum(append([]byte(key2Id), i), key2Hash+uint64(i)*0x10)
	}

	suite.timer = &mocked.Timer{CurrentTime: time.Unix(currentTime, 0)}

	suite.sut = &strategy.PeerDistanceRelated{
		Hasher:      suite.hasher,
		Timer:       suite.timer,
		LocalPeerId: peerId}
}

func (suite *PeerDistanceRelated_Test) TearDownTest() {
	suite.hasher = nil
	suite.sut = nil
	suite.peer = nil
	suite.timer = nil
}

// Tests

func (suite *PeerDistanceRelated_Test) Test_UntimedKey_DistanceOfEqualHashesIsZero() {
	distance := suite.sut.Distance(suite.peer, data.Key{Id: data.Id(peerId)})
	suite.Assert().Zero(distance)
}

func (suite *PeerDistanceRelated_Test) Test_UntimedKey_DistanceOfNotEqualHashesBitwise() {
	distance := suite.sut.Distance(suite.peer, data.Key{Id: data.Id(keyId)})
	suite.Assert().Equal(peerHash^keyHash, distance)
}

func (suite *PeerDistanceRelated_Test) Test_TimedKey_DifferesFromUntimedKeyWithSameId() {
	distanceUntimed := suite.sut.Distance(suite.peer, data.Key{Id: data.Id(keyId)})
	distanceTimed := suite.sut.Distance(suite.peer, data.Key{Id: data.Id(keyId), TimeStamp: 1})
	suite.Assert().NotEqual(distanceTimed, distanceUntimed)
}

func (suite *PeerDistanceRelated_Test) Test_TimedKey_HasSameIdDuringFirstDay() {
	suite.testTimedKeyAge(0, 1)
}

func (suite *PeerDistanceRelated_Test) Test_TimedKey_HasSameIdNext7Days() {
	suite.testTimedKeyAge(1, 7)
}

func (suite *PeerDistanceRelated_Test) Test_TimedKey_HasSameIdNextMonth() {
	suite.testTimedKeyAge(7, 30)
}

func (suite *PeerDistanceRelated_Test) Test_TimedKey_HasSameIdNextYear() {
	suite.testTimedKeyAge(30, 365)
}

func (suite *PeerDistanceRelated_Test) Test_TimedKey_HasSameIdNext10Years() {
	suite.testTimedKeyAge(365, 3650)
}

func (suite *PeerDistanceRelated_Test) Test_TimedKey_HasAlwaysSameIdAfter10Years() {
	suite.advanceTimeByDays(3650, 0)
	earlyDistance := suite.sut.Distance(suite.peer, recentKey)
	suite.timer.CurrentTime = general.MaxTime()
	lateDistance := suite.sut.Distance(suite.peer, recentKey)
	suite.Assert().Equal(earlyDistance, lateDistance)
}

func (suite *PeerDistanceRelated_Test) Test_LaterPriorityIsHigher() {
	prio1 := suite.sut.Priority(&data.Chunk{Key: recentKey})
	suite.advanceTimeByDays(1, 0)
	prio2 := suite.sut.Priority(&data.Chunk{Key: recentKey})
	suite.Assert().True(prio1 < prio2)
}

func (suite *PeerDistanceRelated_Test) Test_NearerToCurrentPeerHasHigherPriority() {
	// distance key1: 1 ^ 2 = 3 --> lower prio
	prio1 := suite.sut.Priority(&data.Chunk{Key: data.Key{Id: data.Id(keyId)}})

	// distance key2: 1 ^ 3 = 2 --> higher prio
	prio2 := suite.sut.Priority(&data.Chunk{Key: data.Key{Id: data.Id(key2Id)}})

	suite.Assert().True(prio1 < prio2)
}

func (suite *PeerDistanceRelated_Test) Test_ExactlyMatchingKeyIsSameAsKeyWithDistance1() {
	suite.sut.LocalPeerId = key2Id

	// distance key1: 3 ^ 2 = 1
	prio1 := suite.sut.Priority(&data.Chunk{Key: data.Key{Id: data.Id(keyId)}})

	// distance key2: 3 ^ 3 = 0 --> avoid division by zero
	prio2 := suite.sut.Priority(&data.Chunk{Key: data.Key{Id: data.Id(key2Id)}})

	suite.Assert().True(prio1 == prio2)
}

func (suite *PeerDistanceRelated_Test) Test_MaxDistanceYieldsPositivePrio() {
	prio := suite.sut.Priority(&data.Chunk{Key: data.Key{Id: data.Id(peerIdNegated)}})
	suite.Assert().True(0.0 < prio)
}

// Private

func (suite *PeerDistanceRelated_Test) testTimedKeyAge(startAgeDays, endAgeDays int) {
	suite.advanceTimeByDays(startAgeDays, 0)
	earlyDistance := suite.sut.Distance(suite.peer, recentKey)
	suite.advanceTimeByDays(endAgeDays-startAgeDays, -1)
	lateDistance := suite.sut.Distance(suite.peer, recentKey)
	suite.Assert().Equal(earlyDistance, lateDistance)
	suite.advanceTimeByDays(0, 1)
	lateDistance = suite.sut.Distance(suite.peer, recentKey)
	suite.Assert().NotEqual(earlyDistance, lateDistance)
}

func (suite *PeerDistanceRelated_Test) advanceTimeByDays(days int, seconds int64) {
	suite.timer.CurrentTime =
		time.Unix(suite.timer.CurrentTime.AddDate(0, 0, days).Unix()+seconds, 0)
}
