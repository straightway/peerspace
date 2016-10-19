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

package randvar

import (
	"math/rand"
	"testing"
	"time"

	"github.com/straightway/straightway/sim/randvar"
	"github.com/stretchr/testify/suite"
)

type PositiveDuration_Test struct {
	suite.Suite
	randSource rand.Source
}

func TestPositiveDuration(t *testing.T) {
	suite.Run(t, new(PositiveDuration_Test))
}

// Tests

func (suite *PositiveDuration_Test) TestNextNotNegativeSampleIsReturned() {
	baseRandvar := randvar.NewDurationMock(0)
	sut := NewPositiveDuration(baseRandvar)
	suite.Assert().Equal(time.Duration(0), sut.NextSample())
}

func (suite *PositiveDuration_Test) TestNegativeBaseSamplesAreIgnored() {
	baseRandvar := randvar.NewDurationMock(-2, -3, 5)
	sut := NewPositiveDuration(baseRandvar)
	suite.Assert().Equal(time.Duration(5), sut.NextSample())
}
