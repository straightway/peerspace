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
	"github.com/straightway/straightway/strategy"
)

// Test suite

type DataStrategy_TestBase struct {
	ForwardStrategy_TestBase
	sut *strategy.Data
}

func (suite *DataStrategy_TestBase) SetupTest() {
	suite.ForwardStrategy_TestBase.SetupTest()
	suite.sut = &strategy.Data{
		Configuration:          suite.configuration,
		ConnectionInfoProvider: suite.connectionInfoProvider}
}

func (suite *DataStrategy_TestBase) TearDownTest() {
	suite.ForwardStrategy_TestBase.TearDownTest()
	suite.sut = nil
}
