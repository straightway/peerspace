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

	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Node_Connection_Test struct {
	suite.Suite
	*NodeContext
}

func TestNodeConnection(t *testing.T) {
	suite.Run(t, new(Node_Connection_Test))
}

func (suite *Node_Connection_Test) SetupTest() {
	suite.NodeContext = NewNodeContext()
	suite.SetUp()
}

func (suite *Node_Connection_Test) TearDownTest() {
	suite.ShutDownNode()
	suite.NodeContext = nil
}

// Tests

func (suite *Node_Connection_Test) TestSuccessfulConnectionIsAcknowledged() {
	peerNode := mocked.NewPeerConnector()
	suite.node.RequestConnectionWith(peerNode)
	peerNode.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
}

func (suite *Node_Connection_Test) TestRefusedConnectionIsClosed() {
	peerNode := mocked.NewPeerConnector()
	suite.connectionStrategy.ExpectedCalls = nil
	suite.connectionStrategy.On("IsConnectionAcceptedWith", mock.Anything).Return(false)

	suite.node.RequestConnectionWith(peerNode)

	peerNode.AssertNotCalled(suite.T(), "RequestConnectionWith", mock.Anything)
	peerNode.AssertCalledOnce(suite.T(), "CloseConnectionWith", suite.node)
}

func (suite *Node_Connection_Test) TestRefusedPeerIsNotConnected() {
	acceptedPeerNode := mocked.NewPeerConnector()
	refusedPeerNode := mocked.NewPeerConnector()
	suite.connectionStrategy.ExpectedCalls = nil
	suite.connectionStrategy.On("IsConnectionAcceptedWith", refusedPeerNode).Return(false)
	suite.connectionStrategy.On("IsConnectionAcceptedWith", acceptedPeerNode).Return(true)

	suite.node.RequestConnectionWith(refusedPeerNode)
	suite.node.RequestConnectionWith(acceptedPeerNode)
	suite.Assert().False(slice.Contains(suite.node.ConnectedPeers(), refusedPeerNode))
}

func (suite *Node_Connection_Test) TestRequestForAlreadyAcceptedConnectionIsIgnored() {
	peerNode := mocked.NewPeerConnector()
	suite.node.RequestConnectionWith(peerNode)
	suite.node.RequestConnectionWith(peerNode)
	peerNode.AssertCalledOnce(suite.T(), "RequestConnectionWith", mock.Anything)
}

func (suite *Node_Connection_Test) TestPeersAreIdentifiedByIdOnConnectionRequestCheck() {
	peerNode := mocked.NewPeerConnector()
	samePeerNode := mocked.NewPeerConnector()
	samePeerNode.Identifier = peerNode.Identifier
	suite.node.RequestConnectionWith(peerNode)
	suite.node.RequestConnectionWith(samePeerNode)
	peerNode.AssertCalledOnce(suite.T(), "RequestConnectionWith", mock.Anything)
	samePeerNode.AssertNotCalled(suite.T(), "RequestConnectionWith", mock.Anything)
}

func (suite *Node_Connection_Test) TestConfirmedConnectionsAreNotReconnected() {
	suite.AddKnownConnectedPeer(DoForward(false))
	suite.node.Startup()
	for _, p := range suite.connectedPeers {
		p.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
		suite.node.RequestConnectionWith(p)
		p.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
	}
}

func (suite *Node_Connection_Test) TestInitialConnectionsArePending() {
	peer := suite.AddKnownConnectedPeer(DoForward(false))
	peer.OnNew("RequestConnectionWith", suite.node).Run(func(mock.Arguments) {
		suite.Assert().True(suite.node.IsConnectionPendingWith(peer))
	})
	suite.node.Startup()
	for _, p := range suite.connectedPeers {
		assert.True(suite.T(), suite.node.IsConnectionPendingWith(p))
	}
}

func (suite *Node_Connection_Test) TestConfirmedConnectionsAreNotPending() {
	suite.AddKnownConnectedPeer(DoForward(false))
	suite.node.Startup()
	for _, p := range suite.connectedPeers {
		suite.node.RequestConnectionWith(p)
		assert.False(suite.T(), suite.node.IsConnectionPendingWith(p))
	}
}

func (suite *Node_Connection_Test) TestRefusedConnectionsAreNotPending() {
	suite.AddKnownConnectedPeer(DoForward(false))
	suite.node.Startup()
	for _, p := range suite.connectedPeers {
		suite.node.CloseConnectionWith(p)
		assert.False(suite.T(), suite.node.IsConnectionPendingWith(p))
	}
}

func (suite *Node_Connection_Test) TestSuccessfulConnectionsAreConnected() {
	peerNode := mocked.NewPeerConnector()
	suite.node.RequestConnectionWith(peerNode)
	assert.True(suite.T(), suite.node.IsConnectedWith(peerNode))
}

func (suite *Node_Connection_Test) TestClosedConnectionsAreNotConnected() {
	peerNode := mocked.NewPeerConnector()
	suite.node.RequestConnectionWith(peerNode)
	suite.node.CloseConnectionWith(peerNode)
	assert.False(suite.T(), suite.node.IsConnectedWith(peerNode))
}

func (suite *Node_Connection_Test) Test_ConnectedPeers_ContainsConnectedPeers() {
	suite.AddKnownConnectedPeer(DoForward(false))
	connectedPeer := suite.AddKnownConnectedPeer(DoForward(false))
	suite.node.Startup()
	suite.node.RequestConnectionWith(connectedPeer)
	result := suite.node.ConnectedPeers()
	suite.Assert().Equal(1, len(result))
	suite.Assert().True(slice.Contains(result, connectedPeer))
}

func (suite *Node_Connection_Test) Test_ConnectingPeers_ContainsConnectingPeers() {
	connectingPeer := suite.AddKnownConnectedPeer(DoForward(false))
	connectedPeer := suite.AddKnownConnectedPeer(DoForward(false))
	suite.node.Startup()
	suite.node.RequestConnectionWith(connectedPeer)
	result := suite.node.ConnectingPeers()
	suite.Assert().Equal(1, len(result))
	suite.Assert().True(slice.Contains(result, connectingPeer))
}

func (suite *Node_Connection_Test) Test_AnnouncePeers_NewPeersAreStoredAsKnownPeers() {
	suite.node.Startup()
	announcedPeer1 := mocked.NewPeerConnector()
	announcedPeer2 := mocked.NewPeerConnector()
	suite.node.AnnouncePeers([]peer.Connector{announcedPeer1, announcedPeer2})
	suite.stateStorage.AssertCalled(suite.T(), "AddKnownPeer", announcedPeer1)
	suite.stateStorage.AssertCalled(suite.T(), "AddKnownPeer", announcedPeer2)
	suite.stateStorage.AssertNumberOfCalls(suite.T(), "AddKnownPeer", 2)
}

func (suite *Node_Connection_Test) Test_AnnouncePeers_SelfReferenceIsNotStored() {
	suite.node.Startup()
	suite.node.AnnouncePeers([]peer.Connector{suite.node})
	suite.stateStorage.AssertNotCalled(suite.T(), "AddKnownPeer", mock.Anything)
}

func (suite *Node_Connection_Test) Test_AnnouncePeers_AlreadyKnownPeersAreIgnored() {
	announcedPeer := suite.AddKnownUnconnectedPeer()
	suite.node.Startup()
	suite.node.AnnouncePeers([]peer.Connector{announcedPeer})
	suite.stateStorage.AssertNotCalled(suite.T(), "AddKnownPeer", mock.Anything)
	suite.stateStorage.AssertNotCalled(suite.T(), "RequestConnectionWith", mock.Anything)
}

func (suite *Node_Connection_Test) Test_AnnouncePeers_NewAnnouncedPeerIsConnectedIfAccepted() {
	suite.node.Startup()
	announcedPeer := mocked.NewPeerConnector()
	announcedPeers := []peer.Connector{announcedPeer}
	suite.node.AnnouncePeers(announcedPeers)
	announcedPeer.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
	suite.Assert().Equal(announcedPeers, suite.node.ConnectingPeers())
}

func (suite *Node_Connection_Test) Test_AnnouncePeers_NewAnnouncedPeerIsNotConnectedIfNotAccepted() {
	suite.node.Startup()
	suite.connectionStrategy.ExpectedCalls = nil
	suite.connectionStrategy.On("IsConnectionAcceptedWith", mock.Anything).Return(false)
	announcedPeer := mocked.NewPeerConnector()
	suite.node.AnnouncePeers([]peer.Connector{announcedPeer})
	announcedPeer.AssertNotCalled(suite.T(), "RequestConnectionWith", suite.node)
}

func (suite *Node_Connection_Test) TestPeersAreRequestedAfterConnectionIsConfirmed() {
	connectedPeer := suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Startup()
	connectedPeer.AssertNotCalled(suite.T(), "RequestPeers", mock.Anything)
	suite.node.RequestConnectionWith(connectedPeer)
	connectedPeer.AssertCalledOnce(suite.T(), "RequestPeers", suite.node)
}

func (suite *Node_Connection_Test) TestPeersAreNotRequestedAfterConnectionIsConfirmedAgain() {
	connectedPeer := suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Startup()
	connectedPeer.AssertNotCalled(suite.T(), "RequestPeers", mock.Anything)
	suite.node.RequestConnectionWith(connectedPeer)
	connectedPeer.Calls = nil
	suite.node.RequestConnectionWith(connectedPeer)
	connectedPeer.AssertNotCalled(suite.T(), "RequestPeers", mock.Anything)
}

func (suite *Node_Connection_Test) TestPeersAreRequestedAfterUnknownPeerConnectsSuccessfully() {
	connectingPeer := mocked.NewPeerConnector()
	suite.node.Startup()
	connectingPeer.AssertNotCalled(suite.T(), "RequestPeers", mock.Anything)
	suite.node.RequestConnectionWith(connectingPeer)
	connectingPeer.AssertCalledOnce(suite.T(), "RequestPeers", suite.node)
}

func (suite *Node_Connection_Test) TestPeersAreNotRequestedAfterPeerConnectIsRejected() {
	connectingPeer := mocked.NewPeerConnector()
	suite.connectionStrategy.ExpectedCalls = nil
	suite.connectionStrategy.On("IsConnectionAcceptedWith", mock.Anything).Return(false)
	suite.connectionStrategy.On("PeersToConnect", mock.Anything).Return([]peer.Connector{})
	suite.node.Startup()
	connectingPeer.AssertNotCalled(suite.T(), "RequestPeers", mock.Anything)
	suite.node.RequestConnectionWith(connectingPeer)
	connectingPeer.AssertNotCalled(suite.T(), "RequestPeers", mock.Anything)
}

func (suite *Node_Connection_Test) Test_RequestPeers_SelectsAnnouncedPeersByStrategy() {
	connectedPeer := suite.AddKnownConnectedPeer(DoForward(true))
	announcedPeers := []peer.Connector{suite.AddKnownConnectedPeer(DoForward(true))}
	suite.announcementStrategy.ExpectedCalls = nil
	suite.announcementStrategy.On("AnnouncedPeers").Return(announcedPeers)
	suite.node.Startup()
	suite.node.RequestPeers(connectedPeer)
	suite.announcementStrategy.AssertCalledOnce(suite.T(), "AnnouncedPeers")
	connectedPeer.AssertCalledOnce(suite.T(), "AnnouncePeers", announcedPeers)
}
