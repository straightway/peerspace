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
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/strategy"
	"github.com/stretchr/testify/suite"
)

// Test suite

type DataStrategy_TestBase struct {
	suite.Suite
	sut                    *strategy.Data
	configuration          *peer.Configuration
	connectionInfoProvider *mocked.ConnectionInfoProvider
}

func (suite *DataStrategy_TestBase) SetupTest() {
	suite.configuration = peer.DefaultConfiguration()
	suite.connectionInfoProvider = mocked.NewConnectionInfoProvider()
	suite.sut = &strategy.Data{
		Configuration:          suite.configuration,
		ConnectionInfoProvider: suite.connectionInfoProvider}
}

func (suite *DataStrategy_TestBase) TearDownTest() {
	suite.configuration = nil
	suite.sut = nil
}
