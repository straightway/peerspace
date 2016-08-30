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

type DataStrategy_ForwardTargetsFor_Test struct {
	DataStrategy_TestBase
}

func TestDataStrategy_ForwardTargetsFor(t *testing.T) {
	suite.Run(t, new(DataStrategy_ForwardTargetsFor_Test))
}

func (suite *DataStrategy_ForwardTargetsFor_Test) SetupTest() {
	suite.DataStrategy_TestBase.SetupTest()
	suite.sut.PeerDistanceCalculator = suite.distanceCalculator
}

func (suite *DataStrategy_ForwardTargetsFor_Test) TearDownTest() {
	suite.DataStrategy_TestBase.TearDownTest()
}

// Tests

func (suite *DataStrategy_ForwardTargetsFor_Test) TestNoConnectionNoForwardTarget() {
	result := suite.sut.ForwardTargetsFor(untimedKey, suite.origin)
	suite.Assert().Empty(result)
}

func (suite *DataStrategy_ForwardTargetsFor_Test) TestChunkIsNotForwardedBack() {
	suite.connectionInfoProvider.AllConnectedPeers =
		append(suite.connectionInfoProvider.AllConnectedPeers, suite.origin)
	result := suite.sut.ForwardTargetsFor(untimedKey, suite.origin)
	suite.Assert().Empty(result)
}

func (suite *DataStrategy_ForwardTargetsFor_Test) TestSingleConnectionIsSelected() {
	connectedPeer := suite.createConnectedPeer()
	result := suite.sut.ForwardTargetsFor(untimedKey, suite.origin)
	suite.Assert().Equal([]peer.Pusher{connectedPeer}, result)
}

func (suite *DataStrategy_ForwardTargetsFor_Test) TestNearestPeerIsSelected() {
	nearPeer := suite.createConnectedPeer()
	farPeer := suite.createConnectedPeer()
	suite.distanceCalculator.ExpectedCalls = nil
	suite.distanceCalculator.On("Distance", nearPeer, untimedKey).Return(uint64(1))
	suite.distanceCalculator.On("Distance", farPeer, untimedKey).Return(uint64(2))
	result := suite.sut.ForwardTargetsFor(untimedKey, suite.origin)
	suite.Assert().Equal([]peer.Pusher{nearPeer}, result)
}
