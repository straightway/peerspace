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

package simc

import (
	"math/rand"

	"github.com/straightway/straightway/general/id"
)

type IdGenerator struct {
	RandSource rand.Source
}

var characters = []byte{
	'0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
	'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
	'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V'}

func (this *IdGenerator) NextId() id.Type {
	dice := rand.New(this.RandSource)
	result := id.Type{}
	nextChars := dice.Int63()
	for i := 0; i < 12; i++ {
		result[i] = characters[byte(nextChars&int64(31))]
		nextChars = nextChars >> 5
	}

	return result
}
