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

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/suite"
)

type Query_ForwardTargetsFor_Test struct {
	Query_TestBase
	origin *peer.ConnectorMock
}

func (suite *Query_ForwardTargetsFor_Test) SetupTest() {
	suite.Query_TestBase.SetupTest()
	suite.origin = peer.NewConnectorMock()
}

func (suite *Query_ForwardTargetsFor_Test) TearDownTest() {
	suite.Query_TestBase.TearDownTest()
	suite.origin = nil
}

func TestQueryStrategyForwardTargetsFor(t *testing.T) {
	suite.Run(t, new(Query_ForwardTargetsFor_Test))
}

// Tests

func (suite *Query_ForwardTargetsFor_Test) TestNoConnectionNoForwardTarget() {
	result := suite.sut.ForwardTargetsFor(data.Query{Id: data.QueryId}, suite.origin)
	suite.Assert().Empty(result)
}

func (suite *Query_ForwardTargetsFor_Test) TestQueryIsNotForwardedBack() {
	suite.connectionInfoProvider.AllConnectedPeers =
		append(suite.connectionInfoProvider.AllConnectedPeers, suite.origin)
	result := suite.sut.ForwardTargetsFor(data.Query{Id: data.QueryId}, suite.origin)
	suite.Assert().Empty(result)
}

func (suite *Query_ForwardTargetsFor_Test) TestSingleConnectionIsSelected() {
	connectedPeer := suite.createConnectedPeer()
	result := suite.sut.ForwardTargetsFor(data.Query{Id: data.QueryId}, suite.origin)
	suite.Assert().Equal([]peer.Queryable{connectedPeer}, result)
}

func (suite *Query_ForwardTargetsFor_Test) TestNearestPeerIsSelectedForUntimedQuery() {
	nearPeer := suite.createConnectedPeer()
	farPeer := suite.createConnectedPeer()

	suite.distanceCalculator.ExpectedCalls = nil
	query := data.Query{Id: data.QueryId}
	suite.distanceCalculator.On("Distances", nearPeer, query).Return([]uint64{1})
	suite.distanceCalculator.On("Distances", farPeer, query).Return([]uint64{2})
	result := suite.sut.ForwardTargetsFor(query, suite.origin)
	suite.Assert().Equal([]peer.Queryable{nearPeer}, result)
}

func (suite *Query_ForwardTargetsFor_Test) TestNearestPeersAreSelectedForTimedQuery() {
	nearPeer1 := suite.createConnectedPeer()
	nearPeer2 := suite.createConnectedPeer()
	farPeer := suite.createConnectedPeer()
	suite.distanceCalculator.ExpectedCalls = nil
	query := data.Query{Id: data.QueryId, TimeFrom: 1, TimeTo: 2}
	suite.distanceCalculator.On("Distances", nearPeer1, query).Return([]uint64{1, 2})
	suite.distanceCalculator.On("Distances", nearPeer2, query).Return([]uint64{2, 1})
	suite.distanceCalculator.On("Distances", farPeer, query).Return([]uint64{3, 3})
	result := suite.sut.ForwardTargetsFor(query, suite.origin)
	suite.Assert().Equal([]peer.Queryable{nearPeer1, nearPeer2}, result)
}

func (suite *Query_ForwardTargetsFor_Test) TestNearestPeersAreNotListedTwice() {
	nearPeer := suite.createConnectedPeer()
	farPeer := suite.createConnectedPeer()
	suite.distanceCalculator.ExpectedCalls = nil
	query := data.Query{Id: data.QueryId, TimeFrom: 1, TimeTo: 2}
	suite.distanceCalculator.On("Distances", nearPeer, query).Return([]uint64{1, 1})
	suite.distanceCalculator.On("Distances", farPeer, query).Return([]uint64{2, 2})
	result := suite.sut.ForwardTargetsFor(query, suite.origin)
	suite.Assert().Equal([]peer.Queryable{nearPeer}, result)
}
