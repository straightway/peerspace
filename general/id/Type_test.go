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

package id

import (
	"strings"
	"testing"

	"github.com/stretchr/testify/suite"
)

// Test suite

type Type_Test struct {
	suite.Suite
}

func TestType(t *testing.T) {
	suite.Run(t, new(Type_Test))
}

// Test cases

func (suite *Type_Test) Test_String_OfEmptyId_YieldsEmptyString() {
	sut := Type{}
	s := sut.String()
	suite.Assert().Empty(s)
}

func (suite *Type_Test) Test_String_OfNonEmptyId_YieldsNonEmptyString() {
	sut := Type{'a'}
	s := sut.String()
	suite.Assert().Equal("a", s)
}

func (suite *Type_Test) Test_String_OfNonEmptyIdStartingWith0_YieldsEmptyString() {
	sut := Type{0, 'a'}
	s := sut.String()
	suite.Assert().Empty(s)
}

func (suite *Type_Test) Test_FromString_OfEmptyString_YieldsEmptyId() {
	sut := FromString("")
	suite.Assert().Equal(Type{}, sut)
}

func (suite *Type_Test) Test_FromString_OfNonEmptyString_YieldsNonEmptyId() {
	sut := FromString("a")
	suite.Assert().Equal(Type{'a'}, sut)
}

func (suite *Type_Test) Test_FromString_OfTooLongString_Panics() {
	tooLongString := strings.Repeat("a", Size+1)
	suite.Assert().Panics(func() { FromString(tooLongString) })
}

func (suite *Type_Test) Test_Empty_YieldsEmptyId() {
	emptyId := Type{}
	suite.Assert().Equal(emptyId, Empty())
}
