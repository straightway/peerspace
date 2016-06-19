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
	"github.com/stretchr/testify/assert"
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
}

func (suite *Node_Sending_Test) TearDownTest() {
	suite.NodeContext = nil
}

// Tests

func (suite *Node_Sending_Test) Test_PushedData_IgnoresNil() {
	suite.node.Push(nil)
	suite.dataForwardStrategy.AssertNumberOfCalls(suite.T(), "ForwardTargetsFor", 0)
}

func (suite *Node_Sending_Test) Test_PushedData_IsForwardedToProperPeer() {
	suite.node.Push(&dataChunk)

	for _, p := range suite.forwardPeers {
		p.AssertCalledOnce(suite.T(), "Push", &dataChunk)
	}
}

func (suite *Node_Sending_Test) Test_PushedData_IsHandedToDataStorage() {
	assert.Nil(suite.T(), suite.dataStorage.Query(queryKey))
	suite.node.Push(&dataChunk)
	assert.NotNil(suite.T(), suite.dataStorage.Query(queryKey))
}

func (suite *Node_Sending_Test) Test_Push_DoesNotQueryStateStorage() {
	suite.stateStorage.AssertCalledOnce(suite.T(), "GetAllKnownPeers")
	suite.node.Push(&dataChunk)
	suite.stateStorage.AssertCalledOnce(suite.T(), "GetAllKnownPeers")
}

func (suite *Node_Sending_Test) Test_Push_ConsidersExternallyConnectedPeers() {
	suite.NodeContext = NewNodeContext()
	suite.SetUp()
	suite.node.Startup()

	connectedPeers := [...]*mocked.PeerConnector{mocked.CreatePeerConnector()}

	suite.node.RequestConnectionWith(connectedPeers[0])
	suite.node.Push(&dataChunk)

	suite.dataForwardStrategy.AssertCalledOnce(
		suite.T(), "ForwardTargetsFor", mocked.IPeerConnectors(connectedPeers[:]))
}
