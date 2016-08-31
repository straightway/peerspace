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
	"fmt"
	"math"
)

type Discrete struct {
	numSamples uint
	mean       float64
	m2         float64
}

func (this *Discrete) AddSample(sample float64) {
	this.numSamples++
	delta := sample - this.mean
	this.mean += delta / float64(this.numSamples)
	this.m2 += delta * (sample - this.mean)
}

func (this *Discrete) Mean() float64 {
	if this.numSamples < 1 {
		return math.NaN()
	}

	return this.mean
}

func (this *Discrete) Variance() float64 {
	if this.numSamples < 2 {
		return math.NaN()
	}

	return this.m2 / (float64(this.numSamples) - 1)
}

func (this *Discrete) NumberOfSamples() uint {
	return this.numSamples
}

func (this *Discrete) String() string {
	return fmt.Sprintf("%v+/-%v (%v samples)", this.Mean(), math.Sqrt(this.Variance()), this.NumberOfSamples())
}
