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
	"math"
	"math/rand"
	"testing"
	"time"

	"github.com/straightway/straightway/general/duration"
	"github.com/stretchr/testify/suite"
)

type NormalDuration_Test struct {
	suite.Suite
	randSource rand.Source
}

func TestNormalDuration(t *testing.T) {
	suite.Run(t, new(NormalDuration_Test))
}

func (suite *NormalDuration_Test) SetupTest() {
	suite.randSource = rand.NewSource(12345)
}

// Tests

func (suite *NormalDuration_Test) TestMeanValueIsAsSpecified() {
	meanDuration := duration.Parse("1m")
	stdDeviation := duration.Parse("1ms")
	sut := NewNormalDuration(suite.randSource, meanDuration, stdDeviation)
	actMean, actStdDeviation := determineMeanAndStdDeviationDuration(sut, 2000, meanDuration)

	suite.assertDeviationBelow(actMean, meanDuration, 0.01)
	suite.assertDeviationBelow(stdDeviation, actStdDeviation, 0.01)
}

// Private

func determineMeanAndStdDeviationDuration(randVar *NormalDuration, numSamples int, definedMean time.Duration) (mean, stdDev time.Duration) {
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

func (suite *NormalDuration_Test) assertDeviationBelow(expected, actual time.Duration, maxDeviation float64) {
	suite.Assert().NotEqual(expected, actual)
	deviation := math.Abs(1.0 - float64(actual)/float64(expected))
	suite.Assert().True(deviation < maxDeviation)
}
