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

package peerc

import (
	"testing"

	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Node_Connection_Test struct {
	suite.Suite
	*TestNodeContext
}

func TestNodeConnection(t *testing.T) {
	suite.Run(t, new(Node_Connection_Test))
}

func (suite *Node_Connection_Test) SetupTest() {
	suite.TestNodeContext = NewNodeContext()
	suite.SetUp()
}

func (suite *Node_Connection_Test) TearDownTest() {
	suite.ShutDownNode()
	suite.TestNodeContext = nil
}

// Tests

func (suite *Node_Connection_Test) TestSuccessfulConnectionIsAcknowledged() {
	peerNode := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(peerNode)
	peerNode.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
}

func (suite *Node_Connection_Test) TestSuccessfulConnectionAddsKnownPeer() {
	peerNode := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(peerNode)
	suite.stateStorage.AssertCalledOnce(suite.T(), "AddKnownPeer", peerNode)
}

func (suite *Node_Connection_Test) TestRefusedConnectionIsClosed() {
	peerNode := peer.NewConnectorMock()
	suite.connectionStrategy.ExpectedCalls = nil
	suite.connectionStrategy.On("IsConnectionAcceptedWith", mock.Anything).Return(false)

	suite.node.RequestConnectionWith(peerNode)

	peerNode.AssertNotCalled(suite.T(), "RequestConnectionWith", mock.Anything)
	peerNode.AssertCalledOnce(suite.T(), "CloseConnectionWith", suite.node)
}

func (suite *Node_Connection_Test) TestRefusedPeerIsNotConnected() {
	acceptedPeerNode := peer.NewConnectorMock()
	refusedPeerNode := peer.NewConnectorMock()
	suite.connectionStrategy.ExpectedCalls = nil
	suite.connectionStrategy.On("IsConnectionAcceptedWith", refusedPeerNode).Return(false)
	suite.connectionStrategy.On("IsConnectionAcceptedWith", acceptedPeerNode).Return(true)

	suite.node.RequestConnectionWith(refusedPeerNode)
	suite.node.RequestConnectionWith(acceptedPeerNode)
	suite.Assert().False(slice.Contains(suite.node.ConnectedPeers(), refusedPeerNode))
}

func (suite *Node_Connection_Test) TestRequestForAlreadyAcceptedConnectionIsIgnored() {
	peerNode := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(peerNode)
	suite.node.RequestConnectionWith(peerNode)
	peerNode.AssertCalledOnce(suite.T(), "RequestConnectionWith", mock.Anything)
}

func (suite *Node_Connection_Test) TestPeersAreIdentifiedByIdOnConnectionRequestCheck() {
	peerNode := peer.NewConnectorMock()
	samePeerNode := peer.NewConnectorMock()
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
	peerNode := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(peerNode)
	assert.True(suite.T(), suite.node.IsConnectedWith(peerNode))
}

func (suite *Node_Connection_Test) TestClosedConnectionsAreNotConnected() {
	peerNode := peer.NewConnectorMock()
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

func (suite *Node_Connection_Test) Test_AnnouncePeersFrom_NewPeersAreStoredAsKnownPeers() {
	suite.node.Startup()
	announcedPeer1 := peer.NewConnectorMock()
	announcedPeer2 := peer.NewConnectorMock()
	suite.node.AnnouncePeersFrom(nil, []peer.Connector{announcedPeer1, announcedPeer2})
	suite.stateStorage.AssertCalled(suite.T(), "AddKnownPeer", announcedPeer1)
	suite.stateStorage.AssertCalled(suite.T(), "AddKnownPeer", announcedPeer2)
	suite.stateStorage.AssertNumberOfCalls(suite.T(), "AddKnownPeer", 2)
}

func (suite *Node_Connection_Test) Test_AnnouncePeersFrom_SelfReferenceIsNotStored() {
	suite.node.Startup()
	suite.node.AnnouncePeersFrom(nil, []peer.Connector{suite.node})
	suite.stateStorage.AssertNotCalled(suite.T(), "AddKnownPeer", mock.Anything)
}

func (suite *Node_Connection_Test) Test_AnnouncePeersFrom_AlreadyKnownPeersAreIgnored() {
	announcedPeer := suite.AddKnownUnconnectedPeer()
	suite.node.Startup()
	suite.node.AnnouncePeersFrom(nil, []peer.Connector{announcedPeer})
	suite.stateStorage.AssertNotCalled(suite.T(), "AddKnownPeer", mock.Anything)
	suite.stateStorage.AssertNotCalled(suite.T(), "RequestConnectionWith", mock.Anything)
}

func (suite *Node_Connection_Test) Test_AnnouncePeersFrom_NewAnnouncedPeerIsConnectedIfAccepted() {
	suite.node.Startup()
	announcedPeer := peer.NewConnectorMock()
	announcedPeers := []peer.Connector{announcedPeer}
	suite.node.AnnouncePeersFrom(nil, announcedPeers)
	announcedPeer.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
	suite.Assert().Equal(announcedPeers, suite.node.ConnectingPeers())
}

func (suite *Node_Connection_Test) Test_AnnouncePeersFrom_NewAnnouncedPeerIsNotConnectedIfNotAccepted() {
	suite.node.Startup()
	suite.connectionStrategy.ExpectedCalls = nil
	suite.connectionStrategy.On("IsConnectionAcceptedWith", mock.Anything).Return(false)
	announcedPeer := peer.NewConnectorMock()
	suite.node.AnnouncePeersFrom(nil, []peer.Connector{announcedPeer})
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
	connectingPeer := peer.NewConnectorMock()
	suite.node.Startup()
	connectingPeer.AssertNotCalled(suite.T(), "RequestPeers", mock.Anything)
	suite.node.RequestConnectionWith(connectingPeer)
	connectingPeer.AssertCalledOnce(suite.T(), "RequestPeers", suite.node)
}

func (suite *Node_Connection_Test) TestPeersAreNotRequestedAfterPeerConnectIsRejected() {
	connectingPeer := peer.NewConnectorMock()
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
	connectedPeer.AssertCalledOnce(suite.T(), "AnnouncePeersFrom", mock.Anything, announcedPeers)
}
