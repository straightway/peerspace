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

package sim

import (
	"math/rand"
)

type IdGenerator struct {
	RandSource rand.Source
}

var characters = []byte{
	'0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
	'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
	'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V'}

func (this *IdGenerator) NextId() string {
	dice := rand.New(this.RandSource)
	result := [12]byte{}
	nextChars := dice.Int63()
	for i := range result {
		result[i] = characters[byte(nextChars&int64(31))]
		nextChars = nextChars >> 5
	}

	return string(result[0:8])
}
