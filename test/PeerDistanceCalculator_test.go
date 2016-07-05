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

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/strategy"
	"github.com/stretchr/testify/suite"
)

const (
	peerId   = "12345"
	peerHash = uint64(0x1)
	keyId    = "67890"
	keyHash  = uint64(0x2)
)

// Test suite

type PeerDistanceCalculator_Test struct {
	suite.Suite
	hasher *mocked.Hash64
	sut    *strategy.PeerDistanceCalculatorImpl
	peer   *mocked.PeerConnector
}

func TestPeerDistanceCalculator(t *testing.T) {
	suite.Run(t, new(PeerDistanceCalculator_Test))
}

func (suite *PeerDistanceCalculator_Test) SetupTest() {
	suite.peer = mocked.CreatePeerConnector()
	suite.peer.Identifier = peerId

	suite.hasher = mocked.NewHash64()
	suite.hasher.SetupHashSum([]byte(peerId), peerHash)
	suite.hasher.SetupHashSum([]byte(keyId), keyHash)

	suite.sut = &strategy.PeerDistanceCalculatorImpl{Hasher: suite.hasher}
}

func (suite *PeerDistanceCalculator_Test) TearDownTest() {
	suite.hasher = nil
	suite.sut = nil
	suite.peer = nil
}

// Tests

func (suite *PeerDistanceCalculator_Test) Test_UntimedKey_DistanceOfEqualHashesIsZero() {
	distance := suite.sut.Distance(suite.peer, data.Key{Id: data.Id(peerId)})
	suite.Assert().Zero(distance)
}

func (suite *PeerDistanceCalculator_Test) Test_UntimedKey_DistanceOfNotEqualHashesBitwise() {
	distance := suite.sut.Distance(suite.peer, data.Key{Id: data.Id(keyId)})
	suite.Assert().Equal(peerHash^keyHash, distance)
}
