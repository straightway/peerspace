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
	"github.com/stretchr/testify/suite"

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/peer"
)

// Test suite

type Forward_TestBase struct {
	suite.Suite
	configuration          *app.Configuration
	connectionInfoProvider *peer.ConnectionInfoProviderMock
	distanceCalculator     *PeerDistanceCalculatorMock
	origin                 *peer.ConnectorMock
}

func (suite *Forward_TestBase) SetupTest() {
	suite.configuration = app.DefaultConfiguration()
	suite.connectionInfoProvider = peer.NewConnectionInfoProviderMock()
	suite.origin = peer.NewConnectorMock()
	suite.distanceCalculator = NewPeerDistanceCalculatorMock()
}

func (suite *Forward_TestBase) TearDownTest() {
	suite.configuration = nil
	suite.connectionInfoProvider = nil
	suite.origin = nil
	suite.distanceCalculator = nil
}

// Private

func (suite *Forward_TestBase) createConnectedPeer() *peer.ConnectorMock {
	connectedPeer := peer.NewConnectorMock()
	suite.connectionInfoProvider.AllConnectedPeers =
		append(suite.connectionInfoProvider.AllConnectedPeers, connectedPeer)
	return connectedPeer
}
