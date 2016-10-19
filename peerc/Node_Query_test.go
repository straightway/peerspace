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
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Node_Query_Test struct {
	suite.Suite
	*TestNodeContext
	queryPeer *peer.ConnectorMock
}

func TestNodeQuery(t *testing.T) {
	suite.Run(t, new(Node_Query_Test))
}

func (suite *Node_Query_Test) SetupTest() {
	suite.TestNodeContext = NewNodeContext()
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.queryStrategy.On("TimeoutFor", mock.Anything).Return(time.Duration(30) * time.Second)
	suite.queryPeer = peer.NewConnectorMock()
	suite.node.Startup()
	suite.ConfirmConnectedPeers()
}

func (suite *Node_Query_Test) TearDownTest() {
	suite.ShutDownNode()
	suite.TestNodeContext = nil
	suite.queryPeer = nil
}

// Tests

func (suite *Node_Query_Test) Test_Query_LocallyStoredItemIsPushedToQueryNode() {
	suite.SetDataStorage(data.NewStorageMock(&data.UntimedChunk))
	suite.Query(data.Query{Id: data.UntimedKey.Id})
	suite.assertQueryResult(&data.UntimedChunk)
}

func (suite *Node_Query_Test) Test_Query_NotLocallyStoredItemIsNotDirectlyPushedBack() {
	suite.Query(data.Query{Id: data.UntimedKey.Id})
	suite.assertQueryResult( /*nothing*/ )
}

func (suite *Node_Query_Test) Test_Query_LocallyFailedQueryIsForwarded() {
	fwdPeer := suite.AddKnownConnectedPeer(DoForward(true))
	query := data.Query{Id: data.UntimedKey.Id}
	suite.Query(query)
	fwdPeer.AssertCalledOnce(suite.T(), "Query", query, suite.node)
}

func (suite *Node_Query_Test) Test_Query_UntimedQueryIsNotForwardedAfterImmediateResult() {
	suite.Push(&data.UntimedChunk)
	fwdPeer := suite.AddKnownConnectedPeer(DoForward(true))
	query := data.Query{Id: data.UntimedKey.Id}
	suite.Query(query)
	fwdPeer.AssertNotCalled(suite.T(), "Query", query, suite.node)
}

func (suite *Node_Query_Test) Test_Query_ForTimeStampRangeIsForwardedAfterImmediateResult() {
	suite.Push(&data.TimedChunk10)
	fwdPeer := suite.AddKnownConnectedPeer(DoForward(true))
	query := data.Query{Id: data.TimedKey10.Id, TimeFrom: 5, TimeTo: 30}
	suite.Query(query)
	fwdPeer.AssertCalledOnce(suite.T(), "Query", query, suite.node)
}

func (suite *Node_Query_Test) Test_Query_ReceivedQueryResultIsForwardedOnce() {
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.Query(data.Query{Id: data.UntimedKey.Id})
	suite.Push(&data.UntimedChunk)
	suite.assertQueryResult(&data.UntimedChunk)
	suite.Push(&data.UntimedChunk)
	suite.assertQueryResult( /*nothing*/ )
}

func (suite *Node_Query_Test) Test_Query_ReceivedQueryResultIsForwardedToMultipleReceivers() {
	otherQueryPeer := peer.NewConnectorMock()
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.Query(data.Query{Id: data.UntimedKey.Id})
	suite.node.Query(data.Query{Id: data.UntimedKey.Id}, otherQueryPeer)

	suite.Push(&data.UntimedChunk)

	suite.assertQueryResult(&data.UntimedChunk)
	assertPushed(suite.T(), otherQueryPeer, &data.UntimedChunk)
}

func (suite *Node_Query_Test) Test_Query_ResultIsSentOnceIfPeerIsAlsoForwardTarget() {
	suite.queryPeer = suite.AddKnownConnectedPeer(DoForward(true))
	suite.Query(data.Query{Id: data.UntimedKey.Id})
	suite.Push(&data.UntimedChunk)
	suite.assertQueryResult(&data.UntimedChunk)
}

func (suite *Node_Query_Test) Test_Query_ReceiverIsPassedToForwardStrategy() {
	suite.queryPeer = suite.AddKnownConnectedPeer(DoForward(true))
	query := data.Query{Id: data.UntimedKey.Id}
	suite.Query(query)
	suite.queryStrategy.AssertCalledOnce(suite.T(), "ForwardTargetsFor", suite.queryPeer, query)
}

func (suite *Node_Query_Test) Test_Query_IsDiscardedAfterTimeout() {
	suite.AddKnownConnectedPeer(DoForward(true))
	query := data.Query{Id: data.UntimedKey.Id}
	suite.Query(query)
	suite.advanceTimeByQueryTimeoutFactor(query, 1.1)
	suite.clearTimedOutQueries()

	suite.Push(&data.UntimedChunk)

	suite.assertQueryResult( /*nothing*/ )
}

func (suite *Node_Query_Test) Test_Query_IsNotDiscardedBeforeTimeout() {
	suite.AddKnownConnectedPeer(DoForward(true))
	query := data.Query{Id: data.UntimedKey.Id}
	suite.Query(query)
	suite.advanceTimeByQueryTimeoutFactor(query, 0.5)
	suite.clearTimedOutQueries()

	suite.Push(&data.UntimedChunk)

	suite.assertQueryResult(&data.UntimedChunk)
}

func (suite *Node_Query_Test) Test_Query_ForTimeStampRangeYieldsMultipleResults() {
	suite.Query(data.Query{Id: data.QueryId, TimeFrom: 10, TimeTo: 20})
	suite.Push(&data.TimedChunk10)
	suite.Push(&data.TimedChunk20)
	suite.assertQueryResult(&data.TimedChunk10, &data.TimedChunk20)
}

func (suite *Node_Query_Test) Test_Query_ForTimeStampRangeIsNotDiscardedAfterImmediateResult() {
	suite.Push(&data.TimedChunk10)
	suite.Query(data.Query{Id: data.QueryId, TimeFrom: 10, TimeTo: 20})
	suite.Push(&data.TimedChunk20)
	suite.assertQueryResult(&data.TimedChunk10, &data.TimedChunk20)
}

func (suite *Node_Query_Test) Test_Query_ForTimeStampRangeIsDiscardedAfterTimeout() {
	query := data.Query{Id: data.QueryId, TimeFrom: 10, TimeTo: 20}
	suite.Query(query)
	suite.advanceTimeByQueryTimeoutFactor(query, 1.1)
	suite.Push(&data.TimedChunk10)
	suite.Push(&data.TimedChunk20)
	suite.assertQueryResult(&data.TimedChunk10)
}

func (suite *Node_Query_Test) Test_Query_NotAcceptedQueryIsImmediatelyDiscarded() {
	query := data.Query{Id: data.QueryId, TimeFrom: 10, TimeTo: 20}
	suite.queryStrategy.ExpectedCalls = nil
	suite.queryStrategy.On("IsQueryAccepted", query, suite.queryPeer).Return(false)
	suite.Query(query)
	suite.dataStorage.AssertNotCalled(suite.T(), "Query", mock.Anything)
}

// Private

func assertPushed(t *testing.T, receiver *peer.ConnectorMock, chunks ...*data.Chunk) {
	for _, chunk := range chunks {
		receiver.AssertCalled(t, "Push", chunk, mock.Anything)
	}
	receiver.AssertNumberOfCalls(t, "Push", len(chunks))
}

func (suite *Node_Query_Test) advanceTimeByQueryTimeoutFactor(query data.Query, factor float32) {
	timeout := suite.queryStrategy.TimeoutFor(query)
	suite.AdvanceTimeBy(time.Duration(float32(timeout) * factor))
}

func (suite *Node_Query_Test) clearTimedOutQueries() {
	suite.Push(&data.Chunk{Key: data.Key{Id: "Other Key"}})
}

func (suite *Node_Query_Test) assertQueryResult(chunks ...*data.Chunk) {
	assertPushed(suite.T(), suite.queryPeer, chunks...)
	suite.queryPeer.Calls = nil
}

func (suite *Node_Query_Test) Query(query data.Query) {
	suite.node.Query(query, suite.queryPeer)
}
