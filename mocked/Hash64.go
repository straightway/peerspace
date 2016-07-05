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

package mocked

import (
	"hash"
	"hash/crc64"

	"github.com/stretchr/testify/mock"
)

type Hash64 struct {
	Base
	hasher     hash.Hash64
	mockedSums map[uint64]uint64
}

func NewHash64() *Hash64 {
	result := &Hash64{
		hasher:     crc64.New(crc64.MakeTable(crc64.ECMA)),
		mockedSums: make(map[uint64]uint64)}
	result.On("Write", mock.Anything).Return()
	result.On("Reset").Return()
	result.On("Sum64").Return()
	return result
}

func (m *Hash64) SetupHashSum(data []byte, sum uint64) {
	key := crc64.Checksum(data, crc64.MakeTable(crc64.ECMA))
	m.mockedSums[key] = sum
}

func (m *Hash64) Write(p []byte) (n int, err error) {
	m.Called(p)
	return m.hasher.Write(p)
}

func (m *Hash64) Sum(b []byte) []byte {
	panic("Not implemented")
}

func (m *Hash64) Reset() {
	m.Called()
	m.hasher.Reset()
}

func (m *Hash64) Size() int {
	panic("Not implemented")
}

func (m *Hash64) BlockSize() int {
	panic("Not implemented")
}

func (m *Hash64) Sum64() uint64 {
	m.Called()
	sum, found := m.mockedSums[m.hasher.Sum64()]
	if !found {
		panic("Missing setup for hashed data")
	}
	return sum
}
