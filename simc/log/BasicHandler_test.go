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
	"bytes"
	"fmt"
	"strings"
	"testing"
	"time"

	"github.com/apex/log"

	"github.com/stretchr/testify/suite"
)

// Test suite

type BasicHandler_test struct {
	suite.Suite
	sut          *BasicHandler
	outputStream *bytes.Buffer
}

type logLine struct {
	levelMarker string
	timestamp   *time.Time
	message     string
	fields      string
}

var basicTimeStamp = time.Unix(12345, 0).In(time.UTC)

func (this *logLine) String() string {
	timestampString := ""
	if this.timestamp != nil {
		timestampString = this.timestamp.Format("2006-01-02 15:04:05.9999")
	}

	return fmt.Sprintf("%s %-24s %-25s%s", this.levelMarker, timestampString, this.message, this.fields)
}

func TestBasicHandler(t *testing.T) {
	suite.Run(t, new(BasicHandler_test))
}

func (suite *BasicHandler_test) SetupTest() {
	SetEnabled(true)
	suite.outputStream = new(bytes.Buffer)
	suite.sut = NewBasicHandler(suite.outputStream)
	log.SetHandler(suite.sut)
	log.SetLevel(log.DebugLevel)
}

func (suite *BasicHandler_test) TearDownTest() {
	log.SetHandler(nil)
	suite.outputStream = nil
	suite.sut = nil
}

// Tests

func (suite *BasicHandler_test) Test_HandleLog_DebugLevel() {
	suite.log(log.DebugLevel, basicTimeStamp, "MSG", log.Fields{})
	suite.assertLogOutput(logLine{"D", &basicTimeStamp, "MSG", ""})
}

func (suite *BasicHandler_test) Test_HandleLog_InfoLevel() {
	suite.log(log.InfoLevel, basicTimeStamp, "MSG", log.Fields{})
	suite.assertLogOutput(logLine{"I", &basicTimeStamp, "MSG", ""})
}

func (suite *BasicHandler_test) Test_HandleLog_WarningLevel() {
	suite.log(log.WarnLevel, basicTimeStamp, "MSG", log.Fields{})
	suite.assertLogOutput(logLine{"W", &basicTimeStamp, "MSG", ""})
}

func (suite *BasicHandler_test) Test_HandleLog_ErrorLevel() {
	suite.log(log.ErrorLevel, basicTimeStamp, "MSG", log.Fields{})
	suite.assertLogOutput(logLine{"E", &basicTimeStamp, "MSG", ""})
}

func (suite *BasicHandler_test) Test_HandleLog_FatalLevel() {
	suite.log(log.FatalLevel, basicTimeStamp, "MSG", log.Fields{})
	suite.assertLogOutput(logLine{"F", &basicTimeStamp, "MSG", ""})
}

func (suite *BasicHandler_test) Test_HandleLog_TimestampOmittedForSecondLog() {
	suite.log(log.InfoLevel, basicTimeStamp, "MSG1", log.Fields{})
	suite.log(log.InfoLevel, basicTimeStamp, "MSG2", log.Fields{})
	suite.assertLogOutput(
		logLine{"I", &basicTimeStamp, "MSG1", ""},
		logLine{"I", nil, "MSG2", ""})
}

func (suite *BasicHandler_test) Test_HandleLog_LogsSingleField() {
	suite.log(log.InfoLevel, basicTimeStamp, "MSG", log.Fields{"Field": "FieldValue"})
	suite.assertLogOutput(logLine{"I", &basicTimeStamp, "MSG", " [Field: FieldValue]"})
}

func (suite *BasicHandler_test) Test_HandleLog_LogsMultipleFields() {
	suite.log(
		log.InfoLevel,
		basicTimeStamp,
		"MSG",
		log.Fields{"Field1": "Value1", "Field2": "Value2"})
	suite.assertLogOutput(
		logLine{
			"I",
			&basicTimeStamp,
			"MSG",
			" [Field1: Value1; Field2: Value2]"})
}

func (suite *BasicHandler_test) Test_HandleLog_LogsMultipleFieldsSorted() {
	suite.log(
		log.InfoLevel,
		basicTimeStamp,
		"MSG",
		log.Fields{"Field2": "Value2", "Field1": "Value1"})
	suite.assertLogOutput(
		logLine{
			"I",
			&basicTimeStamp,
			"MSG",
			" [Field1: Value1; Field2: Value2]"})
}

func (suite *BasicHandler_test) Test_HandleLog_DoesNothingIfLoggingIsDisabled() {
	SetEnabled(false)
	suite.log(log.DebugLevel, basicTimeStamp, "MSG", log.Fields{})
	suite.assertLogOutput()
}

// Private

func (suite *BasicHandler_test) log(
	level log.Level,
	timestamp time.Time,
	message string,
	fields log.Fields) {

	entry := log.NewEntry(nil)
	entry.Message = message
	entry.Level = level
	entry.Timestamp = timestamp
	entry.Fields = fields
	suite.sut.HandleLog(entry)
}

func (suite *BasicHandler_test) assertLogOutput(logLines ...logLine) {
	result := strings.Split(suite.outputStream.String(), "\n")
	suite.Assert().Equal(len(logLines), len(result)-1)
	for i, logLine := range logLines {
		suite.Assert().Equal(logLine.String(), result[i])
	}
}
