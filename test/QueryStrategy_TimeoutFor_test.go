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
	"github.com/straightway/straightway/general"
	"github.com/stretchr/testify/suite"
)

type QueryStrategy_TimeoutFor_Test struct {
	QueryStrategy_TestBase
}

func TestQueryStrategyTimeoutFor(t *testing.T) {
	suite.Run(t, new(QueryStrategy_TimeoutFor_Test))
}

func (suite *QueryStrategy_TimeoutFor_Test) TestTimeoutForTimedQueriesIsTakenFromConfiguration() {
	timeout := general.ParseDuration("10ns")
	suite.configuration.TimedQueryTimeout = timeout
	result := suite.sut.TimeoutFor(data.Query{Id: queryId, TimeFrom: 1})
	suite.Assert().Equal(timeout, result)
}

func (suite *QueryStrategy_TimeoutFor_Test) TestTimeoutForUntimedQueriesIsTakenFromConfiguration() {
	timeout := general.ParseDuration("10ms")
	suite.configuration.UntimedQueryTimeout = timeout
	result := suite.sut.TimeoutFor(data.Query{Id: queryId})
	suite.Assert().Equal(timeout, result)
}
