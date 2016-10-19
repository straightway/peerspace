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

package general

import (
	"testing"

	"github.com/stretchr/testify/suite"
)

// Test suite

type AreEqual_Test struct {
	suite.Suite
}

func TestGeneralAreEqual(t *testing.T) {
	suite.Run(t, new(AreEqual_Test))
}

func (suite *AreEqual_Test) SetupTest() {
}

func (suite *AreEqual_Test) TearDownTest() {
}

// Tests

func (suite *AreEqual_Test) Test_AreEqual_EqualEqualers() {
	eq1 := NewEqualerMock(true)
	eq2 := NewEqualerMock(true)
	result := AreEqual(eq1, eq2)
	suite.Assert().True(result)
}

func (suite *AreEqual_Test) Test_AreEqual_NotEqualEqualers() {
	eq1 := NewEqualerMock(false)
	eq2 := NewEqualerMock(false)
	result := AreEqual(eq1, eq2)
	suite.Assert().False(result)
}

func (suite *AreEqual_Test) Test_AreEqual_ObjectsOfDifferentType_FirstNoEqualer() {
	eq := NewEqualerMock(false)
	result := AreEqual(3, eq)
	suite.Assert().False(result)
}

func (suite *AreEqual_Test) Test_AreEqual_ObjectsOfDifferentType_LastNoEqualer() {
	eq := NewEqualerMock(false)
	result := AreEqual(eq, 3)
	suite.Assert().False(result)
}

func (suite *AreEqual_Test) Test_AreEqual_EqualObjects_BothNoEqualer() {
	result := AreEqual(3, 3)
	suite.Assert().True(result)
}

func (suite *AreEqual_Test) Test_AreEqual_NotEqualObjects_BothNoEqualer() {
	result := AreEqual(3, 5)
	suite.Assert().False(result)
}
