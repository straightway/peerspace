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

	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/suite"
)

type DataStrategy_ForwardTargetsFor_Test struct {
	DataStrategy_TestBase
}

func TestDataStrategy_ForwardTargetsFor(t *testing.T) {
	suite.Run(t, new(DataStrategy_ForwardTargetsFor_Test))
}

// Tests

func (suite *DataStrategy_ForwardTargetsFor_Test) TestNoConnectionNoForwardTarget() {
	origin := mocked.CreatePeerConnector()
	result := suite.sut.ForwardTargetsFor(untimedKey, origin)
	suite.Assert().Empty(result)
}

func (suite *DataStrategy_ForwardTargetsFor_Test) TestChunkIsNotForwardedBack() {
	origin := mocked.CreatePeerConnector()
	suite.connectionInfoProvider.AllConnectedPeers =
		append(suite.connectionInfoProvider.AllConnectedPeers, origin)
	result := suite.sut.ForwardTargetsFor(untimedKey, origin)
	suite.Assert().Empty(result)
}

func (suite *DataStrategy_ForwardTargetsFor_Test) TestOnlyConnectionIsSelected() {
	origin := mocked.CreatePeerConnector()
	connectedPeer := mocked.CreatePeerConnector()
	suite.connectionInfoProvider.AllConnectedPeers =
		append(suite.connectionInfoProvider.AllConnectedPeers, connectedPeer)
	result := suite.sut.ForwardTargetsFor(untimedKey, origin)
	suite.Assert().Equal([]peer.Connector{connectedPeer}, result)
}
