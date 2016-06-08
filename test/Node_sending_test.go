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
	tmock "github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type NodeSendingTest struct {
	suite.Suite
	sut             peer.Node
	peers           []*mock.MockedPeerConnector
	stateStorage    *mock.MockedStateStorage
	forwardStrategy *mock.MockedForwardStrategy
}

func TestPeerNodeSending(t *testing.T) {
	suite.Run(t, new(NodeSendingTest))
}

func (suite *NodeSendingTest) SetupTest() {
	suite.createSut()
	suite.peers = []*mock.MockedPeerConnector{
		createMockedPeerConnector(),
		createMockedPeerConnector(),
		createMockedPeerConnector(),
		createMockedPeerConnector(),
	}
	peerInterfaces := make([]peer.Connector, len(suite.peers))
	for i, peer := range suite.peers {
		peerInterfaces[i] = peer
	}

	suite.stateStorage.On("GetAllKnownPeers").Return(peerInterfaces)
}

func (suite *NodeSendingTest) TearDownTest() {
	suite.peers = nil
}

// Send data

func (suite *NodeSendingTest) Test_PushedData_IsForwardedToProperPeer() {
	targetPeers := []*mock.MockedPeerConnector{suite.peers[1], suite.peers[2]}
	var targetConnectors []peer.Connector = []peer.Connector{targetPeers[0], targetPeers[1]}
	suite.forwardStrategy.On("ForwardedPeer", tmock.AnythingOfTypeArgument("[]peer.Connector")).Return(targetConnectors)
	data := peer.Data{0x2, 0x3, 0x5, 0x7, 0x11}
	suite.sut.Push(data)
	for _, p := range targetPeers {
		p.AssertNumberOfCalls(suite.T(), "Push", 1)
		p.AssertCalled(suite.T(), "Push", data)
	}
}

// Private

func (suite *NodeSendingTest) createSut() {
	suite.stateStorage = new(mock.MockedStateStorage)
	suite.forwardStrategy = new(mock.MockedForwardStrategy)
	suite.sut = peer.NewNode(
		suite.stateStorage,
		suite.forwardStrategy)
}

func createMockedPeerConnector() *mock.MockedPeerConnector {
	mockedPeer := new(mock.MockedPeerConnector)
	mockedPeer.On("Push", tmock.AnythingOfTypeArgument("peer.Data"))
	return mockedPeer
}
