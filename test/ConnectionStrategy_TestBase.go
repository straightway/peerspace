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
	"math/rand"

	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/strategy"
	"github.com/stretchr/testify/suite"
)

type ConnectionStrategy_TestBase struct {
	suite.Suite
	sut                    *strategy.Connection
	connectionInfoProvider *mocked.ConnectionInfoProvider
	allConnectors          []peer.Connector
	configuration          *peer.Configuration
}

func (suite *ConnectionStrategy_TestBase) SetupTest() {
	suite.connectionInfoProvider = mocked.NewConnectionInfoProvider()
	suite.configuration = peer.DefaultConfiguration()
	suite.sut = &strategy.Connection{
		ConnectionInfoProvider: suite.connectionInfoProvider,
		Configuration:          suite.configuration,
		RandSource:             rand.NewSource(12345)}
}

func (suite *ConnectionStrategy_TestBase) TearDownTest() {
	suite.sut = nil
	suite.connectionInfoProvider = nil
	suite.allConnectors = nil
	suite.configuration = nil
}

func (suite *ConnectionStrategy_TestBase) addConnectedPeer() {
	suite.connectionInfoProvider.AllConnectedPeers = append(
		suite.connectionInfoProvider.AllConnectedPeers,
		suite.createPeerConnector())
}

func (suite *ConnectionStrategy_TestBase) addConnectingPeer() {
	suite.connectionInfoProvider.AllConnectingPeers = append(
		suite.connectionInfoProvider.AllConnectingPeers,
		suite.createPeerConnector())
}

var nextPeerId = 0

func (suite *ConnectionStrategy_TestBase) createPeerConnector() *mocked.PeerConnector {
	result := &mocked.PeerConnector{Identifier: fmt.Sprintf("%v", nextPeerId)}
	suite.allConnectors = append(suite.allConnectors, result)
	nextPeerId++
	return result
}

func (suite *ConnectionStrategy_TestBase) createConnectors(count int) {
	for i := 0; i < count; i++ {
		suite.createPeerConnector()
	}
}
