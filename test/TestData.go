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
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/duration"
)

var (
	queryId      string     = "1234"
	untimedKey   data.Key   = data.Key{Id: queryId}
	untimedChunk data.Chunk = data.Chunk{Key: untimedKey, Data: []byte{0x2, 0x3, 0x5, 0x7, 0xB}}
	timedKey10   data.Key   = data.Key{Id: queryId, TimeStamp: 10}
	timedChunk10 data.Chunk = data.Chunk{Key: timedKey10, Data: []byte{0x2, 0x3, 0x5, 0x7, 0xB}}
	timedKey20   data.Key   = data.Key{Id: queryId, TimeStamp: 20}
	timedChunk20 data.Chunk = data.Chunk{Key: timedKey20, Data: []byte{0x3, 0x5, 0x7, 0xB, 0xD}}

	onlineDuration = duration.Parse("2h")
)
