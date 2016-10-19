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

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/peer"

	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type SeedNodeTest struct {
	suite.Suite
	*TestSeedNodeContext
}

func TestSeedNode(t *testing.T) {
	suite.Run(t, new(SeedNodeTest))
}

func (suite *SeedNodeTest) SetupTest() {
	suite.TestSeedNodeContext = NewSeedNodeContext()
	suite.SetUp()
	suite.node.Startup()
}

func (suite *SeedNodeTest) TearDownTest() {
	suite.ShutDownNode()
	suite.TestSeedNodeContext = nil
}

// Tests

func (suite *SeedNodeTest) Test_Startup_MakesNodeStarted() {
	suite.Assert().True(suite.node.IsStarted())
}

func (suite *SeedNodeTest) Test_Shutdown_MakesNodeNotStarted() {
	suite.node.ShutDown()
	suite.Assert().False(suite.node.IsStarted())
}

func (suite *SeedNodeTest) Test_Push_IsIgnored() {
	suite.Assert().NotPanics(func() {
		suite.node.Push(nil, nil)
	})
}

func (suite *SeedNodeTest) Test_Query_IsIgnored() {
	suite.Assert().NotPanics(func() {
		suite.node.Query(data.Query{}, nil)
	})
}

func (suite *SeedNodeTest) Test_AnnouncePeersFrom_DoesNothing() {
	from := peer.NewConnectorMock()
	announced := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(from)
	suite.stateStorage.Calls = nil
	suite.node.AnnouncePeersFrom(from, []peer.Connector{announced})
	suite.stateStorage.AssertNotCalled(suite.T(), "AddKnownPeer", mock.Anything)
	announced.AssertNotCalled(suite.T(), "RequestConnectionWith", mock.Anything)
}

func (suite *SeedNodeTest) Test_RequestPeers_DoesNothing() {
	otherConnected := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(otherConnected)
	receiver := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(receiver)
	receiver.Calls = nil
	suite.node.RequestPeers(receiver)
	receiver.AssertNotCalled(suite.T(), "AnnouncePeersFrom", mock.Anything, mock.Anything)
}

func (suite *SeedNodeTest) Test_RequestConnectionWith_IsAcknowledged() {
	otherPeer := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(otherPeer)
	otherPeer.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
}

func (suite *SeedNodeTest) Test_RequestConnectionWith_MakesPeerKnown() {
	otherPeer := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(otherPeer)
	suite.Assert().Equal([]peer.Connector{otherPeer}, suite.stateStorage.KnownPeers)
}

func (suite *SeedNodeTest) Test_RequestConnectionWith_AnnouncesKnownPeers() {
	otherPeer := peer.NewConnectorMock()
	suite.AddKnownUnconnectedPeer()
	suite.AddKnownUnconnectedPeer()
	knownPeersBefore := append([]peer.Connector(nil), suite.stateStorage.KnownPeers...)
	suite.node.RequestConnectionWith(otherPeer)
	otherPeer.AssertCalledOnce(suite.T(), "AnnouncePeersFrom", suite.node, knownPeersBefore)
}

func (suite *SeedNodeTest) Test_RequestConnectionWith_AnnouncesNothingIfNoKnownPeers() {
	otherPeer := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(otherPeer)
	otherPeer.AssertNotCalled(suite.T(), "AnnouncePeersFrom", mock.Anything, mock.Anything)
}

func (suite *SeedNodeTest) Test_RequestConnectionWith_HasEstablishedConnectionWhileAnnouncingPeers() {
	otherPeer := peer.NewConnectorMock()
	otherPeer.OnNew("AnnouncePeersFrom", suite.node, mock.Anything).Run(func(mock.Arguments) {
		suite.Assert().True(suite.node.IsConnectedWith(otherPeer))
		otherPeer.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
		otherPeer.AssertNotCalled(suite.T(), "CloseConnectionWith", mock.Anything)
	})

	suite.node.RequestConnectionWith(otherPeer)
}

func (suite *SeedNodeTest) Test_RequestConnectionWith_IsImmediatelyClosed() {
	otherPeer := peer.NewConnectorMock()
	suite.node.RequestConnectionWith(otherPeer)
	otherPeer.AssertCalledOnce(suite.T(), "CloseConnectionWith", suite.node)
}
