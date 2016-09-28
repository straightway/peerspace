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

	"github.com/straightway/straightway/data"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Node_Sending_Test struct {
	suite.Suite
	*NodeContext
}

func TestNodeSending(t *testing.T) {
	suite.Run(t, new(Node_Sending_Test))
}

func (suite *Node_Sending_Test) SetupTest() {
	suite.NodeContext = NewNodeContext()
	suite.AddKnownConnectedPeer(DoForward(false))
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.AddKnownConnectedPeer(DoForward(false))
	suite.node.Startup()
	suite.ConfirmConnectedPeers()
}

func (suite *Node_Sending_Test) TearDownTest() {
	suite.NodeContext = nil
}

// Tests

func (suite *Node_Sending_Test) Test_PushedData_IgnoresNil() {
	suite.Push(nil)
	suite.dataStrategy.AssertNumberOfCalls(suite.T(), "ForwardTargetsFor", 0)
}

func (suite *Node_Sending_Test) Test_PushedData_IsForwardedToProperPeer() {
	firstConnectedPeer := suite.node.ConnectedPeers()[0]
	suite.node.Push(&untimedChunk, firstConnectedPeer)
	suite.Assert().NotEmpty(suite.forwardPeers)

	for _, p := range suite.forwardPeers {
		p.AssertCalledOnce(suite.T(), "Push", &untimedChunk, suite.node)
	}
}

func (suite *Node_Sending_Test) Test_PushedData_IsHandedToDataStorage() {
	assert.Empty(suite.T(), suite.dataStorage.Query(data.Query{Id: untimedKey.Id}))
	suite.Push(&untimedChunk)
	assert.NotEmpty(suite.T(), suite.dataStorage.Query(data.Query{Id: untimedKey.Id}))
}

func (suite *Node_Sending_Test) Test_Push_DoesNotQueryStateStorage() {
	suite.stateStorage.AssertCalledOnce(suite.T(), "GetAllKnownPeers")
	suite.Push(&untimedChunk)
	suite.stateStorage.AssertCalledOnce(suite.T(), "GetAllKnownPeers")
}

func (suite *Node_Sending_Test) Test_Push_NotAcceptedChunkIsImmediatelyDiscarded() {
	suite.dataStrategy.ExpectedCalls = nil
	suite.dataStrategy.
		On("IsChunkAccepted", &untimedChunk, suite.FirstConnectedPeer()).
		Return(false)
	suite.Push(&untimedChunk)
	suite.dataStrategy.AssertNotCalled(
		suite.T(), "ForwardTargetsFor", mock.Anything, mock.Anything)
}

func (suite *Node_Sending_Test) Test_Push_AlreadyStoredChunkIsImmediatelyDiscarded() {
	suite.dataStorage.ConsiderStorage(&untimedChunk)
	suite.Push(&untimedChunk)
	suite.dataStrategy.AssertNotCalled(
		suite.T(), "ForwardTargetsFor", mock.Anything, mock.Anything)
}
