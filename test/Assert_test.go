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

type Assert_Test struct {
	suite.Suite
}

type TestType struct {
	privateInterface  interface{}
	PublicInterface   interface{}
	privateNotNilable int
	PublicNotNilable  int
	Pointer           *TestType
	Channel           chan string
	Function          func()
	Map               map[int]string
	Slice             []string
}

func TestAssert(t *testing.T) {
	suite.Run(t, new(Assert_Test))
}

func (suite *Assert_Test) Test_AssertNotNil_PtrObject_PublicNilInterfaceFieldPanics() {
	t := &TestType{}
	assert.Panics(suite.T(), func() { general.AssertFieldsNotNil(t, "PublicInterface") })
}

func (suite *Assert_Test) Test_AssertNotNil_PtrObject_PrivateNotNilInterfaceField() {
	t := &TestType{PublicInterface: "Not Nil"}
	assert.NotPanics(suite.T(), func() { general.AssertFieldsNotNil(t, "PublicInterface") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNilInterfaceFieldPanics() {
	t := TestType{}
	assert.Panics(suite.T(), func() { general.AssertFieldsNotNil(t, "PublicInterface") })
}

func (suite *Assert_Test) Test_AssertNotNil_PrivateNilInterfaceFieldPanics() {
	t := TestType{}
	assert.Panics(suite.T(), func() { general.AssertFieldsNotNil(t, "privateInterface") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNotNilInterfaceField() {
	t := TestType{PublicInterface: "Not Nil"}
	assert.NotPanics(suite.T(), func() { general.AssertFieldsNotNil(t, "PublicInterface") })
}

func (suite *Assert_Test) Test_AssertNotNil_PrivateNotNilInterfaceField() {
	t := TestType{privateInterface: "Not Nil"}
	assert.NotPanics(suite.T(), func() { general.AssertFieldsNotNil(t, "privateInterface") })
}

func (suite *Assert_Test) Test_AssertNotNil_PrivateZeroedNotNilableField() {
	t := TestType{}
	assert.NotPanics(suite.T(), func() { general.AssertFieldsNotNil(t, "privateNotNilable") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNilPointerFieldPanics() {
	t := TestType{}
	assert.Panics(suite.T(), func() { general.AssertFieldsNotNil(t, "Pointer") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNotNilPointerField() {
	t := TestType{}
	t.Pointer = &t
	assert.NotPanics(suite.T(), func() { general.AssertFieldsNotNil(t, "Pointer") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNilChannelFieldPanics() {
	t := TestType{}
	assert.Panics(suite.T(), func() { general.AssertFieldsNotNil(t, "Channel") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNotNilChannelField() {
	t := TestType{Channel: make(chan string)}
	assert.NotPanics(suite.T(), func() { general.AssertFieldsNotNil(t, "Channel") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNilFunctionFieldPanics() {
	t := TestType{}
	assert.Panics(suite.T(), func() { general.AssertFieldsNotNil(t, "Function") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNotNilFunctionField() {
	t := TestType{Function: func() {}}
	assert.NotPanics(suite.T(), func() { general.AssertFieldsNotNil(t, "Function") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNilMapFieldPanics() {
	t := TestType{}
	assert.Panics(suite.T(), func() { general.AssertFieldsNotNil(t, "Map") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNotNilMapField() {
	t := TestType{Map: make(map[int]string)}
	assert.NotPanics(suite.T(), func() { general.AssertFieldsNotNil(t, "Map") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNilSliceFieldPanics() {
	t := TestType{}
	assert.Panics(suite.T(), func() { general.AssertFieldsNotNil(t, "Slice") })
}

func (suite *Assert_Test) Test_AssertNotNil_PublicNotNilSliceField() {
	t := TestType{Slice: make([]string, 0)}
	assert.NotPanics(suite.T(), func() { general.AssertFieldsNotNil(t, "Slice") })
}

func (suite *Assert_Test) Test_AssertNotNil_NotExistingFieldPanics() {
	t := TestType{}
	assert.Panics(suite.T(), func() { general.AssertFieldsNotNil(t, "NotExistingField") })
}
