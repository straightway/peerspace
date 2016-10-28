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

package sorted

import (
	"sort"
	"testing"

	"github.com/stretchr/testify/suite"
)

type Queue_Test struct {
	suite.Suite
}

func TestQueue(t *testing.T) {
	suite.Run(t, new(Queue_Test))
}

// Tests

func (suite *Queue_Test) Test_Pop_EmptyTree() {
	suite.checkInOutOrder()
}

func (suite *Queue_Test) Test_Pop_Insert_OneElement() {
	suite.checkInOutOrder(0)
}

func (suite *Queue_Test) Test_Pop_Insert_TwoElements() {
	suite.checkInOutOrder(1, 0)
	suite.checkInOutOrder(0, 1)
}

func (suite *Queue_Test) Test_Pop_Insert_ThreeElements() {
	suite.checkInOutOrder(2, 1, 0)
	suite.checkInOutOrder(1, 2, 0)
	suite.checkInOutOrder(0, 1, 2)
	suite.checkInOutOrder(0, 2, 1)
	suite.checkInOutOrder(1, 0, 2)
	suite.checkInOutOrder(2, 0, 1)
}

func (suite *Queue_Test) Test_Pop_Insert_DuplicateElements() {
	suite.checkInOutOrder(0, 0)
	suite.checkInOutOrder(2, 2, 1, 2, 1, 0, 0)
}

// Private

func (suite *Queue_Test) checkInOutOrder(itemOrderNumbers ...int) {
	sut := NewQueue()
	for _, orderNumber := range itemOrderNumbers {
		sut.Insert(NewSortableMock(orderNumber))
	}

	sortedOrderNumbers := append([]int(nil), itemOrderNumbers...)
	sort.Ints(sortedOrderNumbers)

	for _, orderNumber := range sortedOrderNumbers {
		item := sut.Pop()
		suite.Assert().Equal(orderNumber, item.(*ItemMock).orderNumber)
	}

	suite.Assert().Nil(sut.Pop())
}
