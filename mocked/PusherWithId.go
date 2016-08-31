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
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/id"
	"github.com/stretchr/testify/mock"
)

type PusherWithId struct {
	Base
}

func NewPusherWithId(id string) *PusherWithId {
	result := &PusherWithId{}
	result.On("Id").Return(id)
	result.On("Equal", mock.Anything).Return(false)
	result.On("Push", mock.Anything, mock.Anything).Return()
	return result
}

func (m *PusherWithId) Id() string {
	return m.Called().Get(0).(string)
}

func (m *PusherWithId) Equal(other general.Equaler) bool {
	return m.Called(other).Get(0).(bool)
}

func (m *PusherWithId) Push(data *data.Chunk, origin id.Holder) {
	m.Called(data, origin)
}
