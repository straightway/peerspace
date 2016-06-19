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

type Node_Query_Test struct {
	suite.Suite
	*NodeContext
}

func TestNodeQuery(t *testing.T) {
	suite.Run(t, new(Node_Query_Test))
}

func (suite *Node_Query_Test) SetupTest() {
	suite.NodeContext = NewNodeContext()
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.SetUp()
}

func (suite *Node_Query_Test) TearDownTest() {
	suite.ShutDownNode()
	suite.NodeContext = nil
}

func (suite *Node_Query_Test) Test_Query_LocallyStoredItemIsPushedToQueryNode() {
	queryPeer := mocked.CreatePeerConnector()
	suite.dataStorage = mocked.NewDataStorage(&dataChunk)
	suite.createSut()
	suite.node.Query(queryKey, queryPeer)
	queryPeer.AssertCalledOnce(suite.T(), "Push", &dataChunk)
}

func (suite *Node_Query_Test) Test_Query_NotLocallyStoredItemIsNotDirectlyPushedBack() {
	queryPeer := mocked.CreatePeerConnector()
	suite.node.Query(queryKey, queryPeer)
	queryPeer.AssertNotCalled(suite.T(), "Push", mock.Anything)
}

func (suite *Node_Query_Test) Test_Query_LocallyFailedQueryIsForwarded() {
	queryPeer := mocked.CreatePeerConnector()
	fwdPeer := suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Query(queryKey, queryPeer)
	fwdPeer.AssertCalledOnce(suite.T(), "Query", queryKey, suite.node)
}

func (suite *Node_Query_Test) Test_Query_ReceivedQueryResultIsForwardedOnce() {
	queryPeer := mocked.CreatePeerConnector()
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Query(queryKey, queryPeer)
	suite.node.Push(&dataChunk)
	queryPeer.AssertCalledOnce(suite.T(), "Push", &dataChunk)
	suite.node.Push(&dataChunk)
	queryPeer.AssertCalledOnce(suite.T(), "Push", &dataChunk)
}

func (suite *Node_Query_Test) Test_Query_ReceivedQueryResultIsForwardedToMultipleReceivers() {
	queryPeer1 := mocked.CreatePeerConnector()
	queryPeer2 := mocked.CreatePeerConnector()
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Query(queryKey, queryPeer1)
	suite.node.Query(queryKey, queryPeer2)
	suite.node.Push(&dataChunk)
	queryPeer1.AssertCalledOnce(suite.T(), "Push", &dataChunk)
	queryPeer2.AssertCalledOnce(suite.T(), "Push", &dataChunk)
}

func (suite *Node_Query_Test) Test_Query_QueryResultIsSentOnceIfPeerIsAlsoForwardTarget() {
	queryPeer := suite.AddKnownConnectedPeer(DoForward(true))
	suite.dataForwardStrategy.
		On("ForwardTargetsFor", mock.Anything).
		Return(mocked.IPeerConnectors(suite.connectedPeers))
	suite.node.Query(queryKey, queryPeer)
	suite.node.Push(&dataChunk)
	queryPeer.AssertCalledOnce(suite.T(), "Push", &dataChunk)
}
