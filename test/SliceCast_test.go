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
	//"reflect"
	"testing"

	"github.com/straightway/straightway/general/slice"
	"github.com/stretchr/testify/suite"
)

type SliceCast_Test struct {
	suite.Suite
}

// Test types

type BaseInterface interface {
	BaseMethod()
}

type OtherBaseInterface interface {
	OtherBaseMethod()
}

type Interface interface {
	BaseInterface
	OtherBaseInterface
	Method()
}

type CousinInterface interface {
	OtherBaseInterface
	BaseMethod()
	Method()
}

type Struct struct{}

func (*Struct) BaseMethod()      {}
func (*Struct) OtherBaseMethod() {}
func (*Struct) Method()          {}

// Tests

func TestSliceCast(t *testing.T) {
	suite.Run(t, new(SliceCast_Test))
}

func (suite *SliceCast_Test) Test_Cast_NilIsEmpty() {
	var sut []Struct
	var result []Interface
	result = slice.Cast(sut, result).([]Interface)
	suite.Assert().Empty(result)
}

func (suite *SliceCast_Test) Test_Cast_UpCast() {
	sut := []*Struct{&Struct{}, &Struct{}}
	expected := []Interface{sut[0], sut[1]}
	var result []Interface
	result = slice.Cast(sut, result).([]Interface)
	suite.Assert().Equal(expected, result)
}

func (suite *SliceCast_Test) Test_Cast_DownCast() {
	expected := []*Struct{&Struct{}, &Struct{}}
	sut := []Interface{expected[0], expected[1]}
	var result []*Struct
	result = slice.Cast(sut, result).([]*Struct)
	suite.Assert().Equal(expected, result)
}

func (suite *SliceCast_Test) Test_Cast_BaseInterface() {
	sut := []BaseInterface{&Struct{}, &Struct{}}
	expected := []Interface{sut[0].(Interface), sut[1].(Interface)}
	var result []Interface
	result = slice.Cast(sut, result).([]Interface)
	suite.Assert().Equal(expected, result)
}

func (suite *SliceCast_Test) Test_Cast_CousinInterface() {
	sut := []BaseInterface{&Struct{}, &Struct{}}
	expected := []CousinInterface{sut[0].(CousinInterface), sut[1].(CousinInterface)}
	var result []CousinInterface
	result = slice.Cast(sut, result).([]CousinInterface)
	suite.Assert().Equal(expected, result)
}

func (suite *SliceCast_Test) Test_Cast_ImproperTypePanics() {
	sut := []int{1}
	var result []Interface
	suite.Panics(func() { slice.Cast(sut, result) })
}
