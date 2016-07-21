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

	"github.com/straightway/straightway/general"
	"github.com/stretchr/testify/suite"
)

type RangeInt64_Test struct {
	suite.Suite
}

func TestRange(t *testing.T) {
	suite.Run(t, new(RangeInt64_Test))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_NotIntersecting_SmallerFirst() {
	a := general.RangeInt64{5, 10}
	b := general.RangeInt64{10, 20}
	suite.Assert().False(a.IntersectsWith(b))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_NotIntersecting_LargerFirst() {
	a := general.RangeInt64{5, 10}
	b := general.RangeInt64{10, 20}
	suite.Assert().False(b.IntersectsWith(a))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_SameRange() {
	a := general.RangeInt64{5, 10}
	suite.Assert().True(a.IntersectsWith(a))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_IncludingRange_SmallerFirst() {
	a := general.RangeInt64{5, 10}
	b := general.RangeInt64{6, 9}
	suite.Assert().True(a.IntersectsWith(b))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_IncludingRange_LargerFirst() {
	a := general.RangeInt64{5, 10}
	b := general.RangeInt64{6, 9}
	suite.Assert().True(b.IntersectsWith(a))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_OverlappingRange_SmallerFirst() {
	a := general.RangeInt64{5, 10}
	b := general.RangeInt64{3, 7}
	suite.Assert().True(a.IntersectsWith(b))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_OverlappingRange_LargerFirst() {
	a := general.RangeInt64{5, 10}
	b := general.RangeInt64{3, 7}
	suite.Assert().True(b.IntersectsWith(a))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_InvalidRange_InvalidFirst() {
	a := general.RangeInt64{10, 5}
	b := general.RangeInt64{0, 20}
	suite.Assert().False(a.IntersectsWith(b))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_InvalidRange_InvalidLast() {
	a := general.RangeInt64{10, 5}
	b := general.RangeInt64{0, 20}
	suite.Assert().False(b.IntersectsWith(a))
}

func (suite *RangeInt64_Test) Test_IntersectsWith_InvalidRange_Both() {
	a := general.RangeInt64{10, 5}
	b := general.RangeInt64{7, 3}
	suite.Assert().False(b.IntersectsWith(a))
}

func (suite *RangeInt64_Test) Test_String() {
	suite.Assert().Equal("[3,5[", general.RangeInt64{3, 5}.String())
}
