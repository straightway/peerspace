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

	"github.com/stretchr/testify/suite"
)

// Test suite

type ConnectionStrategy_PeersToConnect_Test struct {
	ConnectionStrategy_Base
}

func TestConnectionStrategy_PeersToConnect(t *testing.T) {
	suite.Run(t, new(ConnectionStrategy_PeersToConnect_Test))
}

// Tests

func (suite *ConnectionStrategy_PeersToConnect_Test) TestEmptyInputEmptyOutput() {
	result := suite.sut.PeersToConnect(nil)
	suite.Assert().Empty(result)
}

func (suite *ConnectionStrategy_PeersToConnect_Test) TestSkipAlreadyConnectedPeers() {
	suite.addConnectedPeer()
	result := suite.sut.PeersToConnect(suite.connectionInfoProvider.AllConnectedPeers)
	suite.Assert().Empty(result)
}

func (suite *ConnectionStrategy_PeersToConnect_Test) TestConnectAllIfNotExceedingMaximum() {
	suite.configuration.MaxConnections = 3
	suite.createConnectors(suite.configuration.MaxConnections)
	result := suite.sut.PeersToConnect(suite.allConnectors)
	suite.Assert().Equal(suite.allConnectors[0:3], result)
}

func (suite *ConnectionStrategy_PeersToConnect_Test) TestConnectToMaxNumberIfNotYetConnected() {
	suite.configuration.MaxConnections = 3
	suite.createConnectors(suite.configuration.MaxConnections + 1)

	result := suite.sut.PeersToConnect(suite.allConnectors)
	suite.Assert().Equal(suite.allConnectors[0:suite.configuration.MaxConnections], result)
}
