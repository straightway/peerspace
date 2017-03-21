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

package slice

import (
	"testing"

	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/loop"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/suite"
)

// Test suite

type SliceTools_Test struct {
	suite.Suite
}

type testStruct struct{}

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
	assert.False(suite.T(), Contains(nil, intEqualer(3)))
}

func (suite *SliceTools_Test) Test_Contains_ItemContainedInSlice() {
	assert.True(suite.T(), Contains([]intEqualer{intEqualer(3)}, intEqualer(3)))
}

func (suite *SliceTools_Test) Test_Contains_ItemNotContainedInSlice() {
	assert.False(suite.T(), Contains([]intEqualer{intEqualer(2)}, intEqualer(3)))
}

func (suite *SliceTools_Test) Test_Contains_NilContained() {
	assert.True(suite.T(), Contains([]general.Equaler{intEqualer(2), nil}, nil))
}

func (suite *SliceTools_Test) Test_Contains_NilNotContained() {
	assert.False(suite.T(), Contains([]intEqualer{intEqualer(2)}, nil))
}

func (suite *SliceTools_Test) Test_Contains_WithNotEqualerSlice() {
	assert.True(suite.T(), Contains([]int{2}, 2))
}

func (suite *SliceTools_Test) Test_Contains_WithNonSliceOrArray() {
	assert.Panics(suite.T(), func() { Contains(2, intEqualer(2)) })
}

func (suite *SliceTools_Test) Test_SetUnion_OfNilItemsIsNil() {
	assert.Nil(suite.T(), SetUnion(nil, nil))
}

func (suite *SliceTools_Test) Test_SetUnion_WithFirstItemNilIsFirstItemUnique() {
	input := []intEqualer{intEqualer(2), intEqualer(2)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(2)}, SetUnion(input, nil))
}

func (suite *SliceTools_Test) Test_SetUnion_WithSecondItemNilIsSecondItemUnique() {
	input := []intEqualer{intEqualer(2), intEqualer(2)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(2)}, SetUnion(nil, input))
}

func (suite *SliceTools_Test) Test_SetUnion_WithDisjointSlicesAreAppended() {
	a := []intEqualer{intEqualer(2)}
	b := []intEqualer{intEqualer(3)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(2), intEqualer(3)}, SetUnion(a, b))
}

func (suite *SliceTools_Test) Test_SetUnion_WithOverlappingSlices() {
	a := []intEqualer{intEqualer(1), intEqualer(2)}
	b := []intEqualer{intEqualer(1), intEqualer(3)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(1), intEqualer(2), intEqualer(3)}, SetUnion(a, b))
}

func (suite *SliceTools_Test) Test_SetUnion_WithOverlappingValuesInFirstArgument() {
	a := []intEqualer{intEqualer(1), intEqualer(1), intEqualer(2)}
	b := []intEqualer{intEqualer(3)}
	assert.Equal(suite.T(), []intEqualer{intEqualer(1), intEqualer(2), intEqualer(3)}, SetUnion(a, b))
}

func (suite *SliceTools_Test) Test_ForEachSliceItem_NilDoesNothing() {
	ForEachItem(nil, func(p interface{}) loop.Control {
		panic("Callback should not be called")
	})
}

func (suite *SliceTools_Test) Test_ForEachSliceItem_CallbackIsCalledForEachItem() {
	input := []int{2, 3, 5, 7}
	output := []int{}
	ForEachItem(input, func(p interface{}) loop.Control {
		output = append(output, p.(int))
		return loop.Continue
	})
	assert.Equal(suite.T(), input, output)
}

func (suite *SliceTools_Test) Test_ForEachSliceItem_CallbackReturnFalseBreaksLoop() {
	input := []int{2, 3, 5, 7}
	output := []int{}
	ForEachItem(input, func(p interface{}) loop.Control {
		output = append(output, p.(int))
		return loop.Break
	})
	assert.Equal(suite.T(), []int{2}, output)
}

func (suite *SliceTools_Test) Test_RemoveItemsIf_NilSliceBecomesEmpty() {
	result := RemoveItemsIf(nil, truePredicate)
	assert.Empty(suite.T(), result)
}

func (suite *SliceTools_Test) Test_RemoveItemsIf_NilSliceResultIsCastableToSliceType() {
	var sut []int = nil
	result := RemoveItemsIf(sut, truePredicate)
	assert.NotPanics(suite.T(), func() { sut = result.([]int) })
}

func (suite *SliceTools_Test) Test_RemoveItemsIf_NilPredicatePanics() {
	var sut []int = nil
	var nilPredicate func(item interface{}) bool = nil
	assert.Panics(suite.T(), func() { RemoveItemsIf(sut, nilPredicate) })
}

func (suite *SliceTools_Test) Test_RemoveItemsIf_FalsePredicateYieldsUnchangedSlice() {
	var sut []int = []int{2, 3, 5}
	result := RemoveItemsIf(sut, falsePredicate)
	assert.Equal(suite.T(), []int{2, 3, 5}, result)
}

func (suite *SliceTools_Test) Test_RemoveItemsIf_TruePredicateYieldsEmptySlice() {
	var sut []int = []int{2, 3, 5}
	result := RemoveItemsIf(sut, truePredicate)
	assert.Empty(suite.T(), result)
}

func (suite *SliceTools_Test) Test_RemoveItemsIf_EvenPredicateYieldsOddSlice() {
	var sut []int = []int{2, 3, 5}
	result := RemoveItemsIf(sut, evenPredicate)
	assert.Equal(suite.T(), []int{3, 5}, result)
}

func (suite *SliceTools_Test) Test_RemoveItemsIf_NilPredicateYieldsNotNil() {
	notNilItem := 2
	var sut []*int = []*int{&notNilItem, nil}
	result := RemoveItemsIf(sut, nilPredicate)
	assert.Equal(suite.T(), []*int{&notNilItem}, result)
}

func (suite *SliceTools_Test) Test_RemoveItemsIf_WithPlainStructValues() {
	var sut []testStruct = []testStruct{testStruct{}, testStruct{}}
	result := RemoveItemsIf(sut, truePredicate)
	assert.Empty(suite.T(), result)
}

func (suite *SliceTools_Test) Test_IndexOf_ReturnsMinusOneForEmptySlice() {
	var sut = []int{}
	suite.Assert().Equal(-1, IndexOf(sut, 2))
}

func (suite *SliceTools_Test) Test_IndexOf_ReturnsZeroIfOnlyElementMatches() {
	var sut = []int{2}
	suite.Assert().Equal(0, IndexOf(sut, 2))
}

func (suite *SliceTools_Test) Test_IndexOf_ReturnsMinusOneIfOnlyElementMatchesNot() {
	var sut = []int{3}
	suite.Assert().Equal(-1, IndexOf(sut, 2))
}

func (suite *SliceTools_Test) Test_IndexOf_ReturnsOneIfSecondElementMatches() {
	var sut = []int{3, 2}
	suite.Assert().Equal(1, IndexOf(sut, 2))
}

func (suite *SliceTools_Test) Test_ToString_ReturnsEmptyStringForEmptySlice() {
	result := ToString([]int{}, "")
	suite.Assert().Empty(result)
}

func (suite *SliceTools_Test) Test_ToString_ReturnsSingleStringForSingleElementSlice() {
	result := ToString([]int{1}, "")
	suite.Assert().Equal("1", result)
}

func (suite *SliceTools_Test) Test_ToString_MultiElementSlice() {
	result := ToString([]int{1, 2, 3}, "")
	suite.Assert().Equal("123", result)
}

func (suite *SliceTools_Test) Test_ToString_MultiElementSliceWithSeparator() {
	result := ToString([]int{1, 2, 3}, ",")
	suite.Assert().Equal("1,2,3", result)
}

// Private

func truePredicate(item interface{}) bool {
	return true
}

func falsePredicate(item interface{}) bool {
	return false
}

func evenPredicate(item interface{}) bool {
	return item.(int)%2 == 0
}

func nilPredicate(item interface{}) bool {
	return item == nil
}
