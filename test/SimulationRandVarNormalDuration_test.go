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
	"math"
	"math/rand"
	"testing"
	"time"

	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/simulation/randvar"
	"github.com/stretchr/testify/suite"
)

type SimulationRandvarNormalDuration_Test struct {
	suite.Suite
	randSource rand.Source
}

func TestSimulationRandvarNormalDuration(t *testing.T) {
	suite.Run(t, new(SimulationRandvarNormalDuration_Test))
}

func (suite *SimulationRandvarNormalDuration_Test) SetupTest() {
	suite.randSource = rand.NewSource(12345)
}

// Tests

func (suite *SimulationRandvarNormalDuration_Test) TestMeanValueIsAsSpecified() {
	meanDuration := general.ParseDuration("1m")
	stdDeviation := general.ParseDuration("1ms")
	sut := randvar.NewNormalDuration(suite.randSource, meanDuration, stdDeviation)
	actMean, actStdDeviation := determineMeanAndStdDeviationDuration(sut, 2000, meanDuration)

	suite.assertDeviationBelow(actMean, meanDuration, 0.01)
	suite.assertDeviationBelow(stdDeviation, actStdDeviation, 0.01)
}

// Private

func determineMeanAndStdDeviationDuration(randVar randvar.Duration, numSamples int, definedMean time.Duration) (mean, stdDev time.Duration) {
	sumDurations := time.Duration(0)
	sumVariations := int64(0)
	for i := 0; i < numSamples; i++ {
		sample := randVar.NextSample()
		sumDurations += sample
		currVariation := sample - definedMean
		sumVariations += int64(currVariation * currVariation)
	}

	variance := sumVariations / int64(numSamples)
	stdDev = time.Duration(math.Sqrt(float64(variance)))
	mean = time.Duration(int64(sumDurations) / int64(numSamples))

	return
}

func (suite *SimulationRandvarNormalDuration_Test) assertDeviationBelow(expected, actual time.Duration, maxDeviation float64) {
	suite.Assert().NotEqual(expected, actual)
	deviation := math.Abs(1.0 - float64(actual)/float64(expected))
	suite.Assert().True(deviation < maxDeviation)
}
