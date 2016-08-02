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

	"github.com/straightway/straightway/sim/randvar"
	"github.com/stretchr/testify/suite"
)

type SimulationRandvarNormal_Test struct {
	suite.Suite
	randSource rand.Source
}

func TestSimulationRandvarNormal(t *testing.T) {
	suite.Run(t, new(SimulationRandvarNormal_Test))
}

func (suite *SimulationRandvarNormal_Test) SetupTest() {
	suite.randSource = rand.NewSource(12345)
}

// Tests

func (suite *SimulationRandvarNormal_Test) TestMeanValueIsAsSpecified() {
	mean := 4.0
	stdDeviation := 2.0
	sut := randvar.NewNormalFloat64(suite.randSource, mean, stdDeviation)
	actMean, actStdDeviation := determineMeanAndStdDeviation(sut, 2000, mean)

	suite.assertDeviationBelow(actMean, mean, 0.01)
	suite.assertDeviationBelow(stdDeviation, actStdDeviation, 0.01)
}

// Private

func determineMeanAndStdDeviation(randVar *randvar.NormalFloat64, numSamples int, definedMean float64) (mean, stdDev float64) {
	sum := float64(0.0)
	sumVariations := float64(0.0)
	for i := 0; i < numSamples; i++ {
		sample := randVar.NextSample()
		sum += sample
		currVariation := sample - definedMean
		sumVariations += currVariation * currVariation
	}

	variance := sumVariations / float64(numSamples)
	stdDev = math.Sqrt(variance)
	mean = sum / float64(numSamples)

	return
}

func (suite *SimulationRandvarNormal_Test) assertDeviationBelow(expected, actual float64, maxDeviation float64) {
	suite.Assert().NotEqual(expected, actual)
	deviation := math.Abs(1.0 - actual/expected)
	suite.Assert().True(deviation < maxDeviation)
}
