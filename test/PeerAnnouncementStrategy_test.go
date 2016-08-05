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

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/strategy"
	"github.com/stretchr/testify/suite"
)

// Test suite

type PeerAnnouncementStrategy_Test struct {
	suite.Suite
	sut           *strategy.Announcement
	configuration *app.Configuration
	stateStorage  *mocked.StateStorage
}

func TestPeerAnnouncementStrategy(t *testing.T) {
	suite.Run(t, new(PeerAnnouncementStrategy_Test))
}

func (suite *PeerAnnouncementStrategy_Test) SetupTest() {
	suite.configuration = app.DefaultConfiguration()
	suite.configuration.MaxAnnouncedPeers = 3
	suite.stateStorage = mocked.NewStateStorage()
	suite.sut = &strategy.Announcement{
		RandomSource:  rand.NewSource(12345),
		Configuration: suite.configuration,
		StateStorage:  suite.stateStorage}
}

func (suite *PeerAnnouncementStrategy_Test) TearDownTest() {
	suite.stateStorage = nil
	suite.sut = nil
}

// Tests

func (suite *PeerAnnouncementStrategy_Test) Test_NoKnownPeers_NoPeersToAnnounce() {
	result := suite.sut.AnnouncedPeers()
	suite.Assert().Empty(result)
}

func (suite *PeerAnnouncementStrategy_Test) Test_SingleKnownPeer_IsReturned() {
	suite.stateStorage.KnownPeers = append(suite.stateStorage.KnownPeers, mocked.CreatePeerConnector())
	result := suite.sut.AnnouncedPeers()
	suite.Assert().Equal(suite.stateStorage.KnownPeers, result)
}

func (suite *PeerAnnouncementStrategy_Test) Test_ManyKnownPeers_ReturnsNotMoreThanMaximumNumber() {
	suite.createKnownPeers(5)
	result := suite.sut.AnnouncedPeers()
	suite.Assert().Equal(suite.configuration.MaxAnnouncedPeers, len(result))
}

func (suite *PeerAnnouncementStrategy_Test) Test_ManyKnownPeers_ReturnsRandomizedItems() {
	suite.createKnownPeers(5)
	result := suite.sut.AnnouncedPeers()
	suite.Assert().NotEqual(suite.stateStorage.KnownPeers[0:suite.configuration.MaxAnnouncedPeers], result)
}

func (suite *PeerAnnouncementStrategy_Test) Test_ManyKnownPeers_ReturnsKnownPeers() {
	suite.createKnownPeers(5)
	result := suite.sut.AnnouncedPeers()
	for _, peer := range result {
		suite.Assert().Contains(suite.stateStorage.KnownPeers, peer)
	}
}

func (suite *PeerAnnouncementStrategy_Test) Test_ManyKnownPeers_ReturnsDifferentResultsWithEachCall() {
	suite.createKnownPeers(5)
	result1 := suite.sut.AnnouncedPeers()
	result2 := suite.sut.AnnouncedPeers()
	suite.Assert().NotEqual(result1, result2)
}

// Private

func (suite *PeerAnnouncementStrategy_Test) createKnownPeers(count int) {
	for i := 0; i < count; i++ {
		suite.stateStorage.KnownPeers = append(suite.stateStorage.KnownPeers, mocked.CreatePeerConnector())
	}
}
