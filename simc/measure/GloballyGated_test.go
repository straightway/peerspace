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

package measure

import (
	"testing"

	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"

	"github.com/straightway/straightway/sim/measure"
)

type GloballyGated_Test struct {
	suite.Suite
	sut     *GloballyGated
	wrapped *measure.SampleCollectorMock
}

func TestGloballyGated(t *testing.T) {
	suite.Run(t, new(GloballyGated_Test))
}

func (suite *GloballyGated_Test) SetupTest() {
	suite.wrapped = measure.NewSampleCollectorMock()
	suite.sut = &GloballyGated{suite.wrapped}
}

func (suite *GloballyGated_Test) TearDownTest() {
	suite.sut = nil
	suite.wrapped = nil
}

// Tests

func (suite *GloballyGated_Test) Test_SamplingEnabled_AddSampleIsForwarded() {
	SetGatedSamplingEnabled(true)
	suite.sut.AddSample(3.14)
	suite.wrapped.AssertCalledOnce(suite.T(), "AddSample", 3.14)
}

func (suite *GloballyGated_Test) Test_SamplingDisabled_AddSampleIsForwarded() {
	SetGatedSamplingEnabled(false)
	suite.sut.AddSample(3.14)
	suite.wrapped.AssertNotCalled(suite.T(), "AddSample", mock.Anything)
}

func (suite *GloballyGated_Test) Test_String_YieldsNAForNonStringer() {
	suite.Assert().Equal("n.a.", suite.sut.String())
}

func (suite *GloballyGated_Test) Test_String_IsTakenFromWrappedStringer() {
	wrappedStringer := &Discrete{}
	suite.sut.Wrapped = wrappedStringer
	suite.Assert().Equal(wrappedStringer.String(), suite.sut.String())
}
