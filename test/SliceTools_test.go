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
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/suite"
)

// Test suite

type SliceTools_Test struct {
	suite.Suite
}

type intEqualer int

func (this intEqualer) Equal(other general.Equaler) bool {
	switch other.(type) {
	case intEqualer:
		return this == other
	default:
		return false
	}
}

func TestSliceTools(t *testing.T) {
	suite.Run(t, new(SliceTools_Test))
}

func (suite *SliceTools_Test) Test_Contains_NilSlice_Nothing() {
	assert.False(suite.T(), general.Contains(nil, intEqualer(3)))
}

func (suite *SliceTools_Test) Test_Contains_ItemContainedInSlice() {
	assert.True(suite.T(), general.Contains([]intEqualer{intEqualer(3)}, intEqualer(3)))
}

func (suite *SliceTools_Test) Test_Contains_ItemNotContainedInSlice() {
	assert.False(suite.T(), general.Contains([]intEqualer{intEqualer(2)}, intEqualer(3)))
}

func (suite *SliceTools_Test) Test_Contains_NilContained() {
	assert.True(suite.T(), general.Contains([]general.Equaler{intEqualer(2), nil}, nil))
}

func (suite *SliceTools_Test) Test_Contains_NilNotContained() {
	assert.False(suite.T(), general.Contains([]intEqualer{intEqualer(2)}, nil))
}

func (suite *SliceTools_Test) Test_Contains_WithNotEqualerSlice() {
	assert.False(suite.T(), general.Contains([]int{2}, intEqualer(2)))
}

func (suite *SliceTools_Test) Test_Contains_WithNonSliceOrArray() {
	assert.Panics(suite.T(), func() { general.Contains(2, intEqualer(2)) })
}

func (suite *SliceTools_Test) Test_SetUnion_OfNilItemsIsNil() {
	assert.Nil(suite.T(), general.SetUnion(nil, nil))
}

func (suite *SliceTools_Test) Test_SetUnion_WithFirstItemNilIsFirstItemUnique() {
	input := []intEqualer{intEqualer(2), intEqualer(2)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(2)}, general.SetUnion(input, nil))
}

func (suite *SliceTools_Test) Test_SetUnion_WithSecondItemNilIsSecondItemUnique() {
	input := []intEqualer{intEqualer(2), intEqualer(2)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(2)}, general.SetUnion(nil, input))
}

func (suite *SliceTools_Test) Test_SetUnion_WithDisjointSlicesAreAppended() {
	a := []intEqualer{intEqualer(2)}
	b := []intEqualer{intEqualer(3)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(2), intEqualer(3)}, general.SetUnion(a, b))
}

func (suite *SliceTools_Test) Test_SetUnion_WithOverlappingSlices() {
	a := []intEqualer{intEqualer(1), intEqualer(2)}
	b := []intEqualer{intEqualer(1), intEqualer(3)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(1), intEqualer(2), intEqualer(3)}, general.SetUnion(a, b))
}

func (suite *SliceTools_Test) Test_SetUnion_WithOverlappingValuesInFirstArgument() {
	a := []intEqualer{intEqualer(1), intEqualer(1), intEqualer(2)}
	b := []intEqualer{intEqualer(3)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(1), intEqualer(2), intEqualer(3)}, general.SetUnion(a, b))
}

func (suite *SliceTools_Test) Test_ForEachSliceItem_NilDoesNothing() {
	general.ForEachSliceItem(nil, func(p interface{}) general.LoopControl {
		panic("Callback should not be called")
		return general.Continue
	})
}

func (suite *SliceTools_Test) Test_ForEachSliceItem_CallbackIsCalledForEachItem() {
	input := []int{2, 3, 5, 7}
	output := []int{}
	general.ForEachSliceItem(input, func(p interface{}) general.LoopControl {
		output = append(output, p.(int))
		return general.Continue
	})
	assert.Equal(suite.T(), input, output)
}

func (suite *SliceTools_Test) Test_ForEachSliceItem_CallbackReturnFalseBreaksLoop() {
	input := []int{2, 3, 5, 7}
	output := []int{}
	general.ForEachSliceItem(input, func(p interface{}) general.LoopControl {
		output = append(output, p.(int))
		return general.Break
	})
	assert.Equal(suite.T(), []int{2}, output)
}
