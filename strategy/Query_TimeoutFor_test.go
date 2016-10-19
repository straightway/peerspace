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
	"github.com/straightway/straightway/general/duration"
	"github.com/stretchr/testify/suite"
)

type Query_TimeoutFor_Test struct {
	Query_TestBase
}

func TestQueryStrategyTimeoutFor(t *testing.T) {
	suite.Run(t, new(Query_TimeoutFor_Test))
}

func (suite *Query_TimeoutFor_Test) TestTimeoutForTimedQueriesIsTakenFromConfiguration() {
	timeout := duration.Parse("10ns")
	suite.configuration.TimedQueryTimeout = timeout
	result := suite.sut.TimeoutFor(data.Query{Id: data.QueryId, TimeFrom: 1})
	suite.Assert().Equal(timeout, result)
}

func (suite *Query_TimeoutFor_Test) TestTimeoutForUntimedQueriesIsTakenFromConfiguration() {
	timeout := duration.Parse("10ms")
	suite.configuration.UntimedQueryTimeout = timeout
	result := suite.sut.TimeoutFor(data.Query{Id: data.QueryId})
	suite.Assert().Equal(timeout, result)
}
