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
	"fmt"
	"testing"

	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/strategy"
	"github.com/stretchr/testify/suite"
)

// Test suite

type ConnectionStrategy_Test struct {
	suite.Suite
	sut                    *strategy.Connection
	connectionInfoProvider *mocked.ConnectionInfoProvider
	allConnectors          []peer.Connector
	configuration          *peer.Configuration
}

func TestConnectionStrategy(t *testing.T) {
	suite.Run(t, new(ConnectionStrategy_Test))
}

func (suite *ConnectionStrategy_Test) SetupTest() {
	suite.connectionInfoProvider = mocked.NewConnectionInfoProvider()
	suite.configuration = peer.DefaultConfiguration()
	suite.sut = &strategy.Connection{
		ConnectionInfoProvider: suite.connectionInfoProvider,
		Configuration:          suite.configuration}
}

func (suite *ConnectionStrategy_Test) TearDownTest() {
	suite.sut = nil
	suite.connectionInfoProvider = nil
	suite.allConnectors = nil
	suite.configuration = nil
}

// Tests

func (suite *ConnectionStrategy_Test) Test_PeersToConnect_EmptyInputEmptyOutput() {
	result := suite.sut.PeersToConnect(nil)
	suite.Assert().Empty(result)
}

func (suite *ConnectionStrategy_Test) Test_PeersToConnect_SkipAlreadyConnectedPeers() {
	suite.addConnectedPeer()
	result := suite.sut.PeersToConnect(suite.connectionInfoProvider.AllConnectedPeers)
	suite.Assert().Empty(result)
}

func (suite *ConnectionStrategy_Test) Test_PeersToConnect_ConnectAllIfNotExceedingMaximum() {
	suite.configuration.MaxConnections = 3
	suite.createConnectors(suite.configuration.MaxConnections)
	result := suite.sut.PeersToConnect(suite.allConnectors)
	suite.Assert().Equal(suite.allConnectors[0:3], result)
}

func (suite *ConnectionStrategy_Test) Test_PeersToConnect_ConnectToMaxNumberIfNotYetConnected() {
	suite.configuration.MaxConnections = 3
	suite.createConnectors(suite.configuration.MaxConnections + 1)

	result := suite.sut.PeersToConnect(suite.allConnectors)
	suite.Assert().Equal(suite.allConnectors[0:suite.configuration.MaxConnections], result)
}

// Private

func (suite *ConnectionStrategy_Test) addConnectedPeer() {
	suite.connectionInfoProvider.AllConnectedPeers = append(
		suite.connectionInfoProvider.AllConnectedPeers,
		suite.createPeerConnector())
}

func (suite *ConnectionStrategy_Test) addConnectingPeer() {
	suite.connectionInfoProvider.AllConnectingPeers = append(
		suite.connectionInfoProvider.AllConnectingPeers,
		suite.createPeerConnector())
}

var nextPeerId = 0

func (suite *ConnectionStrategy_Test) createPeerConnector() *mocked.PeerConnector {
	result := &mocked.PeerConnector{Identifier: fmt.Sprintf("%v", nextPeerId)}
	suite.allConnectors = append(suite.allConnectors, result)
	nextPeerId++
	return result
}

func (suite *ConnectionStrategy_Test) createConnectors(count int) {
	for i := 0; i < count; i++ {
		suite.createPeerConnector()
	}
}
