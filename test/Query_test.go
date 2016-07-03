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

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Query_Test struct {
	suite.Suite
}

var key data.Key = data.Key{Id: "123", TimeStamp: 456}

func TestQuery(t *testing.T) {
	suite.Run(t, new(Query_Test))
}

func (suite *Query_Test) Test_QueryExactlyKey_HasKeyIdInResult() {
	query := peer.QueryExactlyKey(key)
	assert.Equal(suite.T(), key.Id, query.Id)
}

func (suite *Query_Test) Test_QueryExactlyKey_HasKeyTimepointUpperBoundInResult() {
	query := peer.QueryExactlyKey(key)
	assert.Equal(suite.T(), key.TimeStamp, query.TimeTo)
}

func (suite *Query_Test) Test_QueryExactlyKey_HasKeyTimepointLowerBoundInResult() {
	query := peer.QueryExactlyKey(key)
	assert.Equal(suite.T(), key.TimeStamp, query.TimeFrom)
}

func (suite *Query_Test) Test_Matches_SameId_TimestampExactlyRange() {
	query := peer.QueryExactlyKey(key)
	assert.True(suite.T(), query.Matches(key))
}

func (suite *Query_Test) Test_MatchesNot_SameId_TimestampWithinRange() {
	query := peer.Query{Id: key.Id, TimeFrom: key.TimeStamp - 1, TimeTo: key.TimeStamp + 1}
	assert.True(suite.T(), query.Matches(key))
}

func (suite *Query_Test) Test_MatchesNot_SameId_TimestampAboveRange() {
	query := peer.Query{Id: key.Id, TimeFrom: key.TimeStamp + 1, TimeTo: key.TimeStamp + 1}
	assert.False(suite.T(), query.Matches(key))
}

func (suite *Query_Test) Test_MatchesNot_DifferentId_TimestampExactlyRange() {
	query := peer.Query{Id: "different from " + key.Id, TimeFrom: key.TimeStamp, TimeTo: key.TimeStamp}
	assert.False(suite.T(), query.Matches(key))
}

func (suite *Query_Test) Test_MatchesOnly_SameId_TimestampExactlyRange() {
	query := peer.QueryExactlyKey(key)
	assert.True(suite.T(), query.MatchesOnly(key))
}

func (suite *Query_Test) Test_MatchesOnlyNot_DifferentId_TimestampExactlyRange() {
	query := peer.QueryExactlyKey(key)
	query.Id = "different from " + key.Id
	assert.False(suite.T(), query.MatchesOnly(key))
}

func (suite *Query_Test) Test_MatchesOnlyNot_SameId_TimestampInRange1() {
	query := peer.QueryExactlyKey(key)
	query.TimeFrom = query.TimeFrom - 1
	assert.False(suite.T(), query.MatchesOnly(key))
}

func (suite *Query_Test) Test_MatchesOnlyNot_SameId_TimestampInRange2() {
	query := peer.QueryExactlyKey(key)
	query.TimeTo = query.TimeTo + 1
	assert.False(suite.T(), query.MatchesOnly(key))
}

func (suite *Query_Test) Test_IsTimed_IdOnlyQuery_False() {
	query := peer.Query{Id: "Id"}
	assert.False(suite.T(), query.IsTimed())
}

func (suite *Query_Test) Test_IsTimed_WithTimeFrom_True() {
	query := peer.Query{Id: "Id", TimeFrom: 1}
	assert.True(suite.T(), query.IsTimed())
}

func (suite *Query_Test) Test_IsTimed_WithTimeTo_True() {
	query := peer.Query{Id: "Id", TimeTo: 1}
	assert.True(suite.T(), query.IsTimed())
}
