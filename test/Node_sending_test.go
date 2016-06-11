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

	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/suite"
)

// Test suite

var data peer.Data = peer.Data{0x2, 0x3, 0x5, 0x7, 0x11}

type NodeSendingTest struct {
	suite.Suite
	*NodeContext
}

func TestPeerNodeSending(t *testing.T) {
	suite.Run(t, new(NodeSendingTest))
}

func (suite *NodeSendingTest) SetupTest() {
	suite.NodeContext = NewNodeContext()
	suite.AddKnownConnectedPeer(DoForward(false))
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.AddKnownConnectedPeer(DoForward(true))
	suite.AddKnownConnectedPeer(DoForward(false))
	suite.node.Startup()
}

func (suite *NodeSendingTest) TearDownTest() {
	suite.NodeContext = nil
}

// Tests

func (suite *NodeSendingTest) Test_PushedData_IsForwardedToProperPeer() {
	suite.node.Push(data)

	for _, p := range suite.forwarsPeers {
		p.AssertNumberOfCalls(suite.T(), "Push", 1)
		p.AssertCalled(suite.T(), "Push", data)
	}
}

func (suite *NodeSendingTest) Test_PushedData_IsHandedToDataStorage() {
	suite.node.Push(data)

	suite.dataStorage.AssertNumberOfCalls(suite.T(), "ConsiderStorage", 1)
	suite.dataStorage.AssertCalled(suite.T(), "ConsiderStorage", data)
}

func (suite *NodeSendingTest) Test_Push_DoesNotQueryStateStorage() {
	suite.stateStorage.AssertNumberOfCalls(suite.T(), "GetAllKnownPeers", 1)
	suite.node.Push(data)
	suite.stateStorage.AssertNumberOfCalls(suite.T(), "GetAllKnownPeers", 1)
}
