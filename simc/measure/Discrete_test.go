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
	"math"
	"testing"

	"github.com/stretchr/testify/suite"
)

type Discrete_Test struct {
	suite.Suite
	sut *Discrete
}

func TestDiscrete(t *testing.T) {
	suite.Run(t, new(Discrete_Test))
}

func (suite *Discrete_Test) SetupTest() {
	suite.sut = &Discrete{}
}

func (suite *Discrete_Test) TearDownTest() {
	suite.sut = nil
}

// Tests

func (suite *Discrete_Test) Test_NoSamples_YieldsMeanNaN() {
	suite.Assert().True(math.IsNaN(suite.sut.Mean()))
}

func (suite *Discrete_Test) Test_SingleSample_YieldsSampleAsMean() {
	sample := float64(83.0)
	suite.sut.AddSample(sample)
	suite.Assert().Equal(sample, suite.sut.Mean())
}

func (suite *Discrete_Test) Test_MultipleSamples_YieldsArithmeticMean() {
	suite.sut.AddSample(float64(11.0))
	suite.sut.AddSample(float64(5.0))
	suite.Assert().Equal(float64(8.0), suite.sut.Mean())
}

func (suite *Discrete_Test) Test_NoSamples_YieldsVarianceNaN() {
	suite.Assert().True(math.IsNaN(suite.sut.Variance()))
}

func (suite *Discrete_Test) Test_OneSample_YieldsVarianceNaN() {
	suite.sut.AddSample(float64(83.0))
	suite.Assert().True(math.IsNaN(suite.sut.Variance()))
}

func (suite *Discrete_Test) Test_OneSample_YieldsProperVariance() {
	samples := []float64{11.0, 5.0, 17.0, 13.0}
	for _, sample := range samples {
		suite.sut.AddSample(sample)
	}

	suite.Assert().Equal(variance(samples), suite.sut.Variance())
}

func (suite *Discrete_Test) Test_NumberOfSamples() {
	suite.sut.AddSample(float64(11.0))
	suite.sut.AddSample(float64(5.0))
	suite.Assert().Equal(uint(2), suite.sut.NumberOfSamples())
}

func (suite *Discrete_Test) Test_String() {
	suite.sut.AddSample(math.Sqrt(0.5))
	suite.sut.AddSample(-math.Sqrt(0.5))
	suite.Assert().Equal("  0.00+/-  1.00 (2 samples)", suite.sut.String())
}

// Private

func variance(samples []float64) float64 {
	sum := float64(0.0)
	sumSquares := float64(0.0)
	for _, sample := range samples {
		sum += sample
		sumSquares += sample * sample
	}

	n := float64(len(samples))
	return (sumSquares - sum*sum/n) / (n - 1)
}
