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

package strategy

import (
	"hash"
	"hash/crc64"

	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/general/mocked"
)

type hash64Mock struct {
	mocked.Base
	hasher     hash.Hash64
	mockedSums map[uint64]uint64
}

func newHash64Mock() *hash64Mock {
	result := &hash64Mock{
		hasher:     crc64.New(crc64.MakeTable(crc64.ECMA)),
		mockedSums: make(map[uint64]uint64)}
	result.On("Write", mock.Anything).Return()
	result.On("Reset").Return()
	result.On("Sum64").Return()
	return result
}

func (m *hash64Mock) SetupHashSum(data []byte, sum uint64) {
	key := crc64.Checksum(data, crc64.MakeTable(crc64.ECMA))
	m.mockedSums[key] = sum
}

func (m *hash64Mock) Write(p []byte) (n int, err error) {
	m.Called(p)
	return m.hasher.Write(p)
}

func (m *hash64Mock) Sum(b []byte) []byte {
	panic("Not implemented")
}

func (m *hash64Mock) Reset() {
	m.Called()
	m.hasher.Reset()
}

func (m *hash64Mock) Size() int {
	panic("Not implemented")
}

func (m *hash64Mock) BlockSize() int {
	panic("Not implemented")
}

func (m *hash64Mock) Sum64() uint64 {
	m.Called()
	sum, found := m.mockedSums[m.hasher.Sum64()]
	if !found {
		panic("Missing setup for hashed data")
	}
	return sum
}
