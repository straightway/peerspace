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

package data

import "github.com/straightway/straightway/general/id"

var (
	QueryId      id.Type = id.FromString("1234")
	OtherId      id.Type = id.FromString("abcd")
	UntimedKey   Key     = Key{Id: QueryId}
	UntimedChunk Chunk   = Chunk{Key: UntimedKey, Data: []byte{0x2, 0x3, 0x5, 0x7, 0xB}}
	TimedKey10   Key     = Key{Id: QueryId, TimeStamp: 10}
	TimedChunk10 Chunk   = Chunk{Key: TimedKey10, Data: []byte{0x2, 0x3, 0x5, 0x7, 0xB}}
	TimedKey20   Key     = Key{Id: QueryId, TimeStamp: 20}
	TimedChunk20 Chunk   = Chunk{Key: TimedKey20, Data: []byte{0x3, 0x5, 0x7, 0xB, 0xD}}
)
