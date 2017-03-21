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

// Test suite

type Query_TestBase struct {
	Forward_TestBase
	sut *Query
}

func (suite *Query_TestBase) SetupTest() {
	suite.Forward_TestBase.SetupTest()
	suite.sut = &Query{
		ConnectionInfoProvider: suite.connectionInfoProvider,
		PeerDistanceCalculator: suite.distanceCalculator,
		Configuration:          suite.configuration}
}

func (suite *Query_TestBase) TearDownTest() {
	suite.sut = nil
	suite.Forward_TestBase.TearDownTest()
}
