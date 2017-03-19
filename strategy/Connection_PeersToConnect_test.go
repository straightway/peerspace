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

package strategy

import (
	"testing"

	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/suite"
)

// Test suite

type ConnectionStrategy_PeersToConnect_Test struct {
	ConnectionStrategy_TestBase
}

func TestConnectionStrategy_PeersToConnect(t *testing.T) {
	suite.Run(t, new(ConnectionStrategy_PeersToConnect_Test))
}

// Tests

func (suite *ConnectionStrategy_PeersToConnect_Test) Test_EmptyInput_EmptyOutput() {
	result := suite.sut.PeersToConnect(nil)
	suite.Assert().Empty(result)
}

func (suite *ConnectionStrategy_PeersToConnect_Test) Test_SkipAlreadyConnectedPeers() {
	suite.addConnectedPeer()
	result := suite.sut.PeersToConnect(suite.connectionInfoProvider.AllConnectedPeers)
	suite.Assert().Empty(result)
}

func (suite *ConnectionStrategy_PeersToConnect_Test) Test_ConnectAll_IfNotExceedingMaximum() {
	suite.configuration.MaxConnections = 3
	suite.createConnectors(suite.configuration.MaxConnections)
	result := suite.sut.PeersToConnect(suite.allConnectors)
	suite.Assert().Equal(suite.allConnectors[0:3], result)
}

func (suite *ConnectionStrategy_PeersToConnect_Test) Test_ConnectToMaxNumber_IfNotYetConnected() {
	suite.configuration.MaxConnections = 3
	suite.createConnectors(suite.configuration.MaxConnections + 1)

	result := suite.sut.PeersToConnect(suite.allConnectors)
	suite.Assert().Equal(suite.configuration.MaxConnections, len(result))
	for _, p := range result {
		suite.Assert().True(slice.Contains(suite.allConnectors, p))
	}

	suite.Assert().Equal(slice.SetUnion(result).([]peer.Connector), result)
}
/*
func (suite *ConnectionStrategy_PeersToConnect_Test) Test_ConnectionsAreRandomized() {
	suite.configuration.MaxConnections = 3
	suite.createConnectors(suite.configuration.MaxConnections + 10)

	result1 := suite.sut.PeersToConnect(suite.allConnectors)
	result2 := suite.sut.PeersToConnect(suite.allConnectors)
	suite.Assert().Equal(len(result1), len(result2))
	suite.Assert().NotEqual(result1, result2)
}*/

func (suite *ConnectionStrategy_PeersToConnect_Test) Test_ClosestPeersAreConnected() {
	suite.configuration.MaxConnections = 3
	suite.createConnectors(suite.configuration.MaxConnections + 10)
	distanceCaluclator := NewPeerDistanceCalculatorMock()
	distanceCaluclator.ExpectedCalls = nil
	for index, peer := range suite.allConnectors {
		distanceCaluclator.On("Distance", suite.sut, peer.Id()).Return(uint64(100-index))
	}

	suite.sut.PeerDistanceCalculator = distanceCaluclator
	result := suite.sut.PeersToConnect(suite.allConnectors)
	suite.Assert().Equal(suite.allConnectors[10:12], result)
}
