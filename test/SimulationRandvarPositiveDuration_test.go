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
	"math/rand"
	"testing"
	"time"

	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simc/randvar"
	"github.com/stretchr/testify/suite"
)

type SimulationRandvarPositiveDuration_Test struct {
	suite.Suite
	randSource rand.Source
}

func TestSimulationRandvarPositiveDuration(t *testing.T) {
	suite.Run(t, new(SimulationRandvarPositiveDuration_Test))
}

// Tests

func (suite *SimulationRandvarPositiveDuration_Test) TestNextNotNegativeSampleIsReturned() {
	baseRandvar := mocked.NewDurationRandVar(0)
	sut := randvar.NewPositiveDuration(baseRandvar)
	suite.Assert().Equal(time.Duration(0), sut.NextSample())
}

func (suite *SimulationRandvarPositiveDuration_Test) TestNegativeBaseSamplesAreIgnored() {
	baseRandvar := mocked.NewDurationRandVar(-2, -3, 5)
	sut := randvar.NewPositiveDuration(baseRandvar)
	suite.Assert().Equal(time.Duration(5), sut.NextSample())
}
