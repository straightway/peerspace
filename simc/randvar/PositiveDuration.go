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
	"time"

	"github.com/straightway/straightway/sim/randvar"
)

type PositiveDuration struct {
	baseRandVar randvar.Duration
}

func NewPositiveDuration(baseRandVar randvar.Duration) *PositiveDuration {
	return &PositiveDuration{baseRandVar: baseRandVar}
}

func (this *PositiveDuration) NextSample() time.Duration {
	sample := time.Duration(-1)
	for sample < time.Duration(0) {
		sample = this.baseRandVar.NextSample()
	}

	return sample
}
