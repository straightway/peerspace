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

package ranges

import (
	"testing"

	"github.com/stretchr/testify/suite"
)

type Int64_Test struct {
	suite.Suite
}

func TestRange(t *testing.T) {
	suite.Run(t, new(Int64_Test))
}

func (suite *Int64_Test) Test_IntersectsWith_NotIntersecting_SmallerFirst() {
	a := Int64{5, 10}
	b := Int64{10, 20}
	suite.Assert().False(a.IntersectsWith(b))
}

func (suite *Int64_Test) Test_IntersectsWith_NotIntersecting_LargerFirst() {
	a := Int64{5, 10}
	b := Int64{10, 20}
	suite.Assert().False(b.IntersectsWith(a))
}

func (suite *Int64_Test) Test_IntersectsWith_SameRange() {
	a := Int64{5, 10}
	suite.Assert().True(a.IntersectsWith(a))
}

func (suite *Int64_Test) Test_IntersectsWith_IncludingRange_SmallerFirst() {
	a := Int64{5, 10}
	b := Int64{6, 9}
	suite.Assert().True(a.IntersectsWith(b))
}

func (suite *Int64_Test) Test_IntersectsWith_IncludingRange_LargerFirst() {
	a := Int64{5, 10}
	b := Int64{6, 9}
	suite.Assert().True(b.IntersectsWith(a))
}

func (suite *Int64_Test) Test_IntersectsWith_OverlappingRange_SmallerFirst() {
	a := Int64{5, 10}
	b := Int64{3, 7}
	suite.Assert().True(a.IntersectsWith(b))
}

func (suite *Int64_Test) Test_IntersectsWith_OverlappingRange_LargerFirst() {
	a := Int64{5, 10}
	b := Int64{3, 7}
	suite.Assert().True(b.IntersectsWith(a))
}

func (suite *Int64_Test) Test_IntersectsWith_InvalidRange_InvalidFirst() {
	a := Int64{10, 5}
	b := Int64{0, 20}
	suite.Assert().False(a.IntersectsWith(b))
}

func (suite *Int64_Test) Test_IntersectsWith_InvalidRange_InvalidLast() {
	a := Int64{10, 5}
	b := Int64{0, 20}
	suite.Assert().False(b.IntersectsWith(a))
}

func (suite *Int64_Test) Test_IntersectsWith_InvalidRange_Both() {
	a := Int64{10, 5}
	b := Int64{7, 3}
	suite.Assert().False(b.IntersectsWith(a))
}

func (suite *Int64_Test) Test_String() {
	suite.Assert().Equal("[3,5[", Int64{3, 5}.String())
}
