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

type Node_LifeCycle_Test struct {
	suite.Suite
	*NodeContext
}

func TestNodeLifeCycle(t *testing.T) {
	suite.Run(t, new(Node_LifeCycle_Test))
}

func (suite *Node_LifeCycle_Test) SetupTest() {
	suite.NodeContext = NewNodeContext()
	suite.AddKnownConnectedPeer(DoForward(false))
	suite.AddKnownUnconnectedPeer()
	suite.SetUp()
}

func (suite *Node_LifeCycle_Test) TearDownTest() {
	suite.ShutDownNode()
	suite.NodeContext = nil
}

// Startup

func (suite *Node_LifeCycle_Test) Test_Startup_WithoutStateStoragePanics() {
	suite.node.StateStorage = nil
	suite.Assert().Panics(func() { suite.node.Startup() })
}

func (suite *Node_LifeCycle_Test) Test_Startup_WithoutForwardStrategyPanics() {
	suite.node.DataForwardStrategy = nil
	suite.Assert().Panics(func() { suite.node.Startup() })
}

func (suite *Node_LifeCycle_Test) Test_Startup_WithoutConnectionStrategyPanics() {
	suite.node.ConnectionStrategy = nil
	suite.Assert().Panics(func() { suite.node.Startup() })
}

func (suite *Node_LifeCycle_Test) Test_Startup_WithoutDataStoragePanics() {
	suite.node.DataStorage = nil
	suite.Assert().Panics(func() { suite.node.Startup() })
}

func (suite Node_LifeCycle_Test) Test_Startup_NilNodePanics() {
	var nilSut peer.Node
	suite.Assert().Panics(func() {
		nilSut.Startup()
	})
}

func (suite *Node_LifeCycle_Test) Test_Startup_GetsPeersFromStateStorage() {
	suite.node.Startup()
	suite.stateStorage.AssertCalledOnce(suite.T(), "GetAllKnownPeers")
}

func (suite *Node_LifeCycle_Test) Test_Startup_ConnectsToPeersAccordingToStrategy() {
	suite.node.Startup()

	for _, p := range suite.connectedPeers {
		p.AssertCalledOnce(suite.T(), "RequestConnectionWith", suite.node)
	}
}

func (suite *Node_LifeCycle_Test) Test_ShutDown_ClosesAllOpenConnections() {
	suite.node.Startup()
	suite.node.ShutDown()

	for _, p := range suite.connectedPeers {
		p.AssertCalledOnce(suite.T(), "CloseConnectionWith", suite.node)
	}
}
