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

package simulation

import (
	"bytes"
	"encoding/binary"

	"github.com/straightway/straightway/data"
)

type RawStorage struct{}

func (this *RawStorage) CreateChunk(key data.Key, virtualSize uint32) *data.Chunk {
	buf := new(bytes.Buffer)
	binary.Write(buf, binary.LittleEndian, virtualSize)
	return &data.Chunk{Key: key, Data: buf.Bytes()}
}

func (this *RawStorage) SizeOf(chunk *data.Chunk) uint32 {
	buf := bytes.NewReader(chunk.Data)
	var virtualSize uint32 = 0
	err := binary.Read(buf, binary.LittleEndian, &virtualSize)
	if err != nil {
		panic(err)
	}
	return virtualSize
}
