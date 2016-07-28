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
)

type NormalFloat64 struct {
	baseRand *rand.Rand
	mean     float64
	stdDev   float64
}

func NewNormalFloat64(source rand.Source, mean, stdDev float64) *NormalFloat64 {
	return &NormalFloat64{
		baseRand: rand.New(source),
		mean:     mean,
		stdDev:   stdDev}
}

func (this *NormalFloat64) NextSample() float64 {
	sample := this.baseRand.NormFloat64()
	sample *= this.stdDev
	sample += this.mean
	return sample
}
