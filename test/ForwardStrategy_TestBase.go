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
	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/mocked"
	"github.com/stretchr/testify/suite"
)

// Test suite

type ForwardStrategy_TestBase struct {
	suite.Suite
	configuration          *app.Configuration
	connectionInfoProvider *mocked.ConnectionInfoProvider
	distanceCalculator     *mocked.PeerDistanceCalculator
	origin                 *mocked.PeerConnector
}

func (suite *ForwardStrategy_TestBase) SetupTest() {
	suite.configuration = app.DefaultConfiguration()
	suite.connectionInfoProvider = mocked.NewConnectionInfoProvider()
	suite.origin = mocked.CreatePeerConnector()
	suite.distanceCalculator = mocked.NewPeerDistanceCalculator()
}

func (suite *ForwardStrategy_TestBase) TearDownTest() {
	suite.configuration = nil
	suite.connectionInfoProvider = nil
	suite.origin = nil
	suite.distanceCalculator = nil
}

// Private

func (suite *ForwardStrategy_TestBase) createConnectedPeer() *mocked.PeerConnector {
	connectedPeer := mocked.CreatePeerConnector()
	suite.connectionInfoProvider.AllConnectedPeers =
		append(suite.connectionInfoProvider.AllConnectedPeers, connectedPeer)
	return connectedPeer
}
