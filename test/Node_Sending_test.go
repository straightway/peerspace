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
	"github.com/straightway/straightway/mocked"
	"github.com/stretchr/testify/suite"
)

// Test suite

var dataChunk data.Chunk = data.Chunk{0x2, 0x3, 0x5, 0x7, 0x11}

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

func (suite *Node_Sending_Test) Test_PushedData_IsForwardedToProperPeer() {
	suite.node.Push(dataChunk)

	for _, p := range suite.forwardPeers {
		p.AssertNumberOfCalls(suite.T(), "Push", 1)
		p.AssertCalled(suite.T(), "Push", dataChunk)
	}
}

func (suite *Node_Sending_Test) Test_PushedData_IsHandedToDataStorage() {
	suite.node.Push(dataChunk)

	suite.dataStorage.AssertNumberOfCalls(suite.T(), "ConsiderStorage", 1)
	suite.dataStorage.AssertCalled(suite.T(), "ConsiderStorage", dataChunk)
}

func (suite *Node_Sending_Test) Test_Push_DoesNotQueryStateStorage() {
	suite.stateStorage.AssertNumberOfCalls(suite.T(), "GetAllKnownPeers", 1)
	suite.node.Push(dataChunk)
	suite.stateStorage.AssertNumberOfCalls(suite.T(), "GetAllKnownPeers", 1)
}

func (suite *Node_Sending_Test) Test_Push_ConsidersExternallyConnectedPeers() {
	suite.NodeContext = NewNodeContext()
	suite.SetUp()
	suite.node.Startup()

	connectedPeers := [...]*mocked.PeerConnector{mocked.CreatePeerConnector()}

	suite.node.RequestConnectionWith(connectedPeers[0])
	suite.node.Push(dataChunk)

	suite.forwardStrategy.AssertNumberOfCalls(suite.T(), "SelectedConnectors", 1)
	suite.forwardStrategy.AssertCalled(
		suite.T(), "SelectedConnectors", mocked.IPeerConnectors(connectedPeers[:]))
}
