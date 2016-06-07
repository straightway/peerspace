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

	"github.com/straightway/straightway/mock"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Node_init_Test struct {
	suite.Suite
	sut          peer.Node
	stateStorage *mock.MockedStateStorage
	knownPeers   []peer.Connector
}

func TestPeerNode(t *testing.T) {
	suite.Run(t, new(Node_init_Test))
}

func (suite *Node_init_Test) SetupTest() {
	suite.knownPeers = make([]peer.Connector, 0)
	suite.createSut()
}

func (suite *Node_init_Test) TearDownTest() {
	suite.sut = nil
}

// Construction

func (suite *Node_init_Test) Test_NewNode_WithoutStateStoragePanics() {
	suite.Assert().Panics(func() {
		suite.sut = peer.NewNode(nil, &mock.MockedForwardStrategy{})
	})
}

// Startup

func (suite Node_init_Test) Test_Startup_NilNodePanics() {
	var nilSut peer.Node
	suite.Assert().Panics(func() {
		nilSut.Startup()
	})
}

func (suite *Node_init_Test) Test_Startup_GetsPeersFromStateStorage() {
	suite.sut.Startup()
	suite.stateStorage.AssertNumberOfCalls(suite.T(), "GetAllKnownPeers", 1)
}

func (suite *Node_init_Test) Test_Startup_ConnectsToPeers() {
	peer := mock.MockedPeerConnector{}
	suite.knownPeers = append(suite.knownPeers, &peer)
	suite.createSut()
	peer.On("Connect", suite.sut).Return()
	suite.sut.Startup()
	peer.AssertNumberOfCalls(suite.T(), "Connect", 1)
}

// Private

func (suite *Node_init_Test) createSut() {
	suite.stateStorage = new(mock.MockedStateStorage)
	suite.stateStorage.On("GetAllKnownPeers").Return(suite.knownPeers)
	suite.sut = peer.NewNode(suite.stateStorage, &mock.MockedForwardStrategy{})
}
