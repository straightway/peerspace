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

package log

import (
	"github.com/apex/log"

	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/general/mocked"
)

type handlerMock struct {
	mocked.Base
	Error error
}

func newHandlerMock() *handlerMock {
	result := &handlerMock{}
	result.On("HandleLog", mock.Anything).Return()
	return result
}

func (m *handlerMock) HandleLog(entry *log.Entry) error {
	m.Called(entry)
	return m.Error
}
