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

package iter

import (
	"testing"

	"github.com/stretchr/testify/suite"

	"github.com/straightway/straightway/general/loop"
)

// Test suite

type Ator_Test struct {
	suite.Suite
}

func TestIterator(t *testing.T) {
	suite.Run(t, new(Ator_Test))
}

// Tests

func (suite *Ator_Test) Test_Do_WithEmptyIterator_DoesNothing() {
	sut := Ator(func() (interface{}, bool) { return nil, false })
	sut.Do(func(interface{}) loop.Control { panic("No iteration expected") })
}

func (suite *Ator_Test) Test_Do_WithNonEmptyIterator_YieldsValues() {
	values := []int{2, 3, 5}
	sut := OnSlice(values)
	result := make([]int, 0)

	sut.Do(func(item interface{}) loop.Control {
		result = append(result, item.(int))
		return loop.Continue
	})

	suite.Assert().Equal(values, result)
}

func (suite *Ator_Test) Test_Do_BreakOnfirstItem_YieldsOnlyOneItem() {
	values := []int{2, 3, 5}
	sut := OnSlice(values)
	result := make([]int, 0)

	sut.Do(func(item interface{}) loop.Control {
		result = append(result, item.(int))
		return loop.Break
	})

	suite.Assert().Equal(values[0:1], result)
}
