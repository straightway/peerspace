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

	"github.com/straightway/straightway/mock"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Node_init_Test struct {
	suite.Suite
	*NodeContext
}

func TestPeerNode(t *testing.T) {
	suite.Run(t, new(Node_init_Test))
}

func (suite *Node_init_Test) SetupTest() {
	suite.NodeContext = NewNodeContext()
	suite.AddKnownConnectedPeer(nil)
	suite.AddKnownUnconnectedPeer(nil)
	suite.SetUp()
}

func (suite *Node_init_Test) TearDownTest() {
	suite.NodeContext = nil
}

// Construction

func (suite *Node_init_Test) Test_NewNode_WithoutStateStoragePanics() {
	suite.Assert().Panics(func() {
		suite.node = peer.NewNode(
			nil,
			&mock.ConnectorSelector{},
			&mock.ConnectorSelector{})
	})
}

func (suite *Node_init_Test) Test_NewNode_WithoutForwardStrategyPanics() {
	suite.Assert().Panics(func() {
		suite.node = peer.NewNode(
			&mock.StateStorage{},
			nil,
			&mock.ConnectorSelector{})
	})
}

func (suite *Node_init_Test) Test_NewNode_WithoutConnectionStrategyPanics() {
	suite.Assert().Panics(func() {
		suite.node = peer.NewNode(
			&mock.StateStorage{},
			&mock.ConnectorSelector{},
			nil)
	})
}

// Startup

func (suite Node_init_Test) Test_Startup_NilNodePanics() {
	var nilSut peer.Node
	suite.Assert().Panics(func() {
		nilSut.Startup()
	})
}

func (suite *Node_init_Test) Test_Startup_GetsPeersFromStateStorage() {
	suite.node.Startup()
	suite.stateStorage.AssertNumberOfCalls(suite.T(), "GetAllKnownPeers", 1)
}

func (suite *Node_init_Test) Test_Startup_ConnectsToPeersAccordingToStrategy() {
	suite.node.Startup()

	for _, p := range suite.connectedPeers {
		p.AssertNumberOfCalls(suite.T(), "Connect", 1)
	}
}
