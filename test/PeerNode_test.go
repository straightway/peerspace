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
package straightway

import (
	"testing"

	"github.com/straightway/straightway/core"
	"github.com/straightway/straightway/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type PeerNodeTest struct {
	suite.Suite
	sut          *core.PeerNode
	stateStorage *mock.MockedStateStorage
	knownPeers   []core.Peer
}

func TestPeerNode(t *testing.T) {
	suite.Run(t, new(PeerNodeTest))
}

func (suite *PeerNodeTest) SetupTest() {
	suite.knownPeers = make([]core.Peer, 0)
	suite.createSut()
}

func (suite *PeerNodeTest) TearDownTest() {
	suite.sut = nil
}

// Startup

func (suite *PeerNodeTest) Test_Startup_NonNilNode() {
	suite.Assert().NotPanics(func() {
		suite.sut.Startup()
	})
}

func (suite *PeerNodeTest) Test_Startup_NilNodePanics() {
	var nilSut *core.PeerNode
	suite.Assert().Panics(func() {
		nilSut.Startup()
	})
}

func (suite *PeerNodeTest) Test_Startup_WithoutStateStoragePanics() {
	suite.sut = new(core.PeerNode)
	suite.Assert().Panics(func() {
		suite.sut.Startup()
	})
}

func (suite *PeerNodeTest) Test_Startup_GetsPeersFromStateStorage() {
	suite.sut.Startup()
	suite.stateStorage.AssertNumberOfCalls(suite.T(), "GetAllKnownPeers", 1)
}

func (suite *PeerNodeTest) Test_Startup_ConnectsToPeers() {
	peer := mock.MockedPeer{}
	suite.knownPeers = append(suite.knownPeers, &peer)
	suite.createSut()
	peer.On("Connect", suite.sut).Return()
	suite.sut.Startup()
	peer.AssertNumberOfCalls(suite.T(), "Connect", 1)
}

// Set state storage

func (suite *PeerNodeTest) Test_SetStateStorage_NilPanics() {
	suite.Assert().Panics(func() {
		suite.sut.SetStateStorage(nil)
	})
}

func (suite *PeerNodeTest) Test_SetStateStorage_TwicePanics() {
	stateStorage := mock.MockedStateStorage{}
	suite.Assert().Panics(func() {
		suite.sut.SetStateStorage(&stateStorage)
	})
}

// Private

func (suite *PeerNodeTest) createSut() {
	suite.sut = new(core.PeerNode)
	suite.stateStorage = new(mock.MockedStateStorage)
	suite.stateStorage.On("GetAllKnownPeers").Return(suite.knownPeers)
	suite.sut.SetStateStorage(suite.stateStorage)
}
