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

// Test suite

type Iterator_Test struct {
	suite.Suite
}

func TestIterator(t *testing.T) {
	suite.Run(t, new(Iterator_Test))
}

// Tests

func (suite *Iterator_Test) Test_Loop_WithEmptyIterator_DoesNothing() {
	sut := general.Iterator(func() (interface{}, bool) { return nil, false })
	sut.Loop(func(interface{}) general.LoopControl { panic("No iteration expected"); return true })
}

func (suite *Iterator_Test) Test_Loop_WithNonEmptyIterator_YieldsValues() {
	values := []int{2, 3, 5}
	sut := general.Iterate(values)
	result := make([]int, 0)

	sut.Loop(func(item interface{}) general.LoopControl {
		result = append(result, item.(int))
		return general.Continue
	})

	suite.Assert().Equal(values, result)
}

func (suite *Iterator_Test) Test_Loop_BreakOnfirstItem_YieldsOnlyOneItem() {
	values := []int{2, 3, 5}
	sut := general.Iterate(values)
	result := make([]int, 0)

	sut.Loop(func(item interface{}) general.LoopControl {
		result = append(result, item.(int))
		return general.Break
	})

	suite.Assert().Equal(values[0:1], result)
}
