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
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
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
	suite.node.Query(peer.Query{Id: queryKey.Id}, queryPeer)
	queryPeer.AssertCalledOnce(suite.T(), "Push", &dataChunk)
}

func (suite *Node_Query_Test) Test_Query_NotLocallyStoredItemIsNotDirectlyPushedBack() {
	queryPeer := mocked.CreatePeerConnector()
	suite.node.Startup()
	suite.node.Query(peer.Query{Id: queryKey.Id}, queryPeer)
	queryPeer.AssertNotCalled(suite.T(), "Push", mock.Anything)
}

func (suite *Node_Query_Test) Test_Query_LocallyFailedQueryIsForwarded() {
	queryPeer := mocked.CreatePeerConnector()
	fwdPeer := suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Startup()
	query := peer.Query{Id: queryKey.Id}
	suite.node.Query(query, queryPeer)
	fwdPeer.AssertCalledOnce(suite.T(), "Query", query, suite.node)
}

func (suite *Node_Query_Test) Test_Query_ReceivedQueryResultIsForwardedOnce() {
	queryPeer := mocked.CreatePeerConnector()
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Startup()
	suite.node.Query(peer.Query{Id: queryKey.Id}, queryPeer)
	suite.node.Push(&dataChunk)
	queryPeer.AssertCalledOnce(suite.T(), "Push", &dataChunk)
	suite.node.Push(&dataChunk)
	queryPeer.AssertCalledOnce(suite.T(), "Push", &dataChunk) // Not called again
}

func (suite *Node_Query_Test) Test_Query_ReceivedQueryResultIsForwardedToMultipleReceivers() {
	queryPeer1 := mocked.CreatePeerConnector()
	queryPeer2 := mocked.CreatePeerConnector()
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Startup()
	suite.node.Query(peer.Query{Id: queryKey.Id}, queryPeer1)
	suite.node.Query(peer.Query{Id: queryKey.Id}, queryPeer2)
	suite.node.Push(&dataChunk)
	queryPeer1.AssertCalledOnce(suite.T(), "Push", &dataChunk)
	queryPeer2.AssertCalledOnce(suite.T(), "Push", &dataChunk)
}

func (suite *Node_Query_Test) Test_Query_ResultIsSentOnceIfPeerIsAlsoForwardTarget() {
	queryPeer := suite.AddKnownConnectedPeer(DoForward(true))
	suite.dataForwardStrategy.
		On("ForwardTargetsFor", suite.connectedPeers, queryKey).
		Return(mocked.IPeerConnectors(suite.connectedPeers))
	suite.node.Startup()
	suite.node.Query(peer.Query{Id: queryKey.Id}, queryPeer)
	suite.node.Push(&dataChunk)
	queryPeer.AssertCalledOnce(suite.T(), "Push", &dataChunk)
}

func (suite *Node_Query_Test) Test_Query_IsDiscardedAfterTimeout() {
	queryPeer := mocked.CreatePeerConnector()
	suite.node.Configuration.QueryTimeout = time.Duration(20)
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Startup()
	suite.node.Query(peer.Query{Id: queryKey.Id}, queryPeer)
	suite.timer.CurrentTime = suite.timer.CurrentTime.Add(suite.node.Configuration.QueryTimeout)
	suite.clearTimedOutQueries()

	suite.node.Push(&dataChunk)

	queryPeer.AssertNotCalled(suite.T(), "Push", mock.Anything)
}

func (suite *Node_Query_Test) Test_Query_IsNotDiscardedBeforeTimeout() {
	queryPeer := mocked.CreatePeerConnector()
	suite.node.Configuration.QueryTimeout = time.Duration(20)
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.node.Startup()
	suite.node.Query(peer.Query{Id: queryKey.Id}, queryPeer)
	suite.timer.CurrentTime = suite.timer.CurrentTime.Add(time.Duration(10))
	suite.clearTimedOutQueries()

	suite.node.Push(&dataChunk)

	queryPeer.AssertCalledOnce(suite.T(), "Push", &dataChunk)
}

func (suite *Node_Query_Test) clearTimedOutQueries() {
	suite.node.Push(&data.Chunk{Key: data.Key{Id: "Other Key"}})
}
