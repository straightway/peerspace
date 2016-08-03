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
	"github.com/straightway/straightway/general/times"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/storage"
	"github.com/stretchr/testify/suite"
)

// Test suite

type DataStorage_TestBase struct {
	suite.Suite
	sut   *storage.DataImpl
	raw   *mocked.RawStorageWithLifeCycle
	timer *mocked.Timer
}

func (suite *DataStorage_TestBase) SetupTest() {
	suite.timer = &mocked.Timer{}
	suite.raw = mocked.NewRawStorageWithLifeCycle(suite.timer)
	suite.sut = &storage.DataImpl{
		RawStorage:        suite.raw,
		PriorityGenerator: mocked.NewPriorityGenerator(0.0, times.Max())}
}

func (suite *DataStorage_TestBase) TearDownTest() {
	suite.sut = nil
	suite.raw = nil
}
