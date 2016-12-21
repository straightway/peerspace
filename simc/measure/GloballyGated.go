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

	"github.com/straightway/straightway/sim/measure"
)

type GloballyGated struct {
	Wrapped measure.SampleCollector
}

var isSamplingEnabled = true

func SetGatedSamplingEnabled(isEnabled bool) {
	isSamplingEnabled = isEnabled
}

func (this *GloballyGated) AddSample(sample float64) {
	if isSamplingEnabled {
		this.Wrapped.AddSample(sample)
	}
}

func (this *GloballyGated) String() string {
	stringer, isStringer := this.Wrapped.(fmt.Stringer)
	if isStringer {
		return stringer.String()
	} else {
		return "n.a."
	}
}
