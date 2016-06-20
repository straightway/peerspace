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

	"github.com/straightway/straightway/mocked"
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

func (suite *Node_Connection_Test) TestSuccessfulConnectionIsAcknowledged() {
	peerNode := mocked.CreatePeerConnector()
	suite.node.RequestConnectionWith(peerNode)
	peerNode.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
}

func (suite *Node_Connection_Test) TestRefusedConnectionIsClosed() {
	peerNode := mocked.CreatePeerConnector()
	suite.connectionStrategy.ExpectedCalls = nil
	suite.connectionStrategy.On("IsConnectionAcceptedWith", mock.Anything).Return(false)

	suite.node.RequestConnectionWith(peerNode)

	peerNode.AssertNotCalled(suite.T(), "RequestConnectionWith", mock.Anything)
	peerNode.AssertCalledOnce(suite.T(), "CloseConnectionWith", suite.node)
}

func (suite *Node_Connection_Test) TestRequestForAlreadyAcceptedConnectionIsIgnored() {
	peerNode := mocked.CreatePeerConnector()
	suite.node.RequestConnectionWith(peerNode)
	suite.node.RequestConnectionWith(peerNode)
	peerNode.AssertCalledOnce(suite.T(), "RequestConnectionWith", mock.Anything)
}

func (suite *Node_Connection_Test) TestPeersAreIdentifiedByIdOnConnectionRequestCheck() {
	peerNode := mocked.CreatePeerConnector()
	samePeerNode := mocked.CreatePeerConnector()
	samePeerNode.Identifier = peerNode.Identifier
	suite.node.RequestConnectionWith(peerNode)
	suite.node.RequestConnectionWith(samePeerNode)
	peerNode.AssertCalledOnce(suite.T(), "RequestConnectionWith", mock.Anything)
	samePeerNode.AssertNotCalled(suite.T(), "RequestConnectionWith", mock.Anything)
}
