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
	"testing"
	"time"

	"github.com/apex/log"

	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/mocked"
	simlog "github.com/straightway/straightway/simc/log"
	"github.com/stretchr/testify/suite"
)

// Test suite

type SimulationLogActionHandlerTest struct {
	suite.Suite
	sut         *simlog.ActionHandler
	baseHandler *mocked.LogHandler
}

type logEntry struct {
	message string
	fields  log.Fields
}

var defaultTimeStamp = time.Unix(12345, 0).In(time.UTC)
var otherTimeStamp = time.Unix(23456, 0).In(time.UTC)

func TestSimulationLogActionHandler(t *testing.T) {
	suite.Run(t, new(SimulationLogActionHandlerTest))
}

func (suite *SimulationLogActionHandlerTest) SetupTest() {
	suite.baseHandler = mocked.NewLogHandler()
	suite.sut = simlog.NewActionHandler(suite.baseHandler)
	log.SetHandler(suite.sut)
	log.SetLevel(log.DebugLevel)
}

func (suite *SimulationLogActionHandlerTest) TearDownTest() {
	log.SetHandler(nil)
	suite.baseHandler = nil
	suite.sut = nil
}

// Tests

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeOtherThanNodeActionIsForwarded() {
	fields := log.Fields{"EntryType": "AnyEntryType"}
	suite.setupBaseHandlerForLog("Info Log", fields)
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeActionIsFormatted() {
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Origin":      1,
		"Destination": 2,
		"Function":    "Func",
		"Parameter":   "Param"}
	suite.setupBaseHandlerForLog(
		"Info Log 1 Func Param -> 2",
		log.Fields{})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeActionIsFormatted2() {
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Origin":      2,
		"Destination": 3,
		"Function":    "Func1",
		"Parameter":   "Param1"}
	suite.setupBaseHandlerForLog(
		"Info Log2 2 Func1 Param1 -> 3",
		log.Fields{})
	entry := log.WithFields(fields)
	entry.Info("Info Log2")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeActionIsFormattedWithoutParameterIfMissing() {
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Origin":      1,
		"Destination": 2,
		"Function":    "Func"}
	suite.setupBaseHandlerForLog(
		"Info Log 1 Func -> 2",
		log.Fields{})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeActionIsFormattedWithoutOrigin() {
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Destination": 2,
		"Function":    "Func",
		"Parameter":   "Param"}
	suite.setupBaseHandlerForLog(
		"Info Log Func Param -> 2",
		log.Fields{})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeActionIsFormattedWithoutDestination() {
	fields := log.Fields{
		"EntryType": "NodeAction",
		"Origin":    1,
		"Function":  "Func",
		"Parameter": "Param"}
	suite.setupBaseHandlerForLog(
		"Info Log 1 Func Param",
		log.Fields{})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeActionIsFormattedWithoutFunction() {
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Origin":      1,
		"Destination": 2,
		"Parameter":   "Param"}
	suite.setupBaseHandlerForLog(
		"Info Log 1 Param -> 2",
		log.Fields{})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeActionIsFormattedWithoutMessage() {
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Origin":      1,
		"Destination": 2,
		"Function":    "Func",
		"Parameter":   "Param"}
	suite.setupBaseHandlerForLog(
		"1 Func Param -> 2",
		log.Fields{})
	entry := log.WithFields(fields)
	entry.Info("")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeListAdditionalFields() {
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Origin":      1,
		"Destination": 2,
		"Function":    "Func",
		"Parameter":   "Param",
		"Additional1": "AdditionalValue1",
		"Additional2": "AdditionalValue2"}
	suite.setupBaseHandlerForLog(
		"Info Log 1 Func Param -> 2",
		log.Fields{
			"Additional1": "AdditionalValue1",
			"Additional2": "AdditionalValue2"})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeSubFields() {
	subFields := []log.Fields{
		log.Fields{"EntryType": "NodeAction", "Function": "1"},
		log.Fields{"EntryType": "NodeAction", "Function": "2"}}
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Origin":      1,
		"Destination": 2,
		"Function":    "Func",
		"Parameter":   "Param",
		"subFields":   subFields}

	suite.setupBaseHandlerForLogs(
		logEntry{"Info Log 1 Func Param -> 2", log.Fields{}},
		logEntry{"subFields", log.Fields{}},
		logEntry{"  1", log.Fields{}},
		logEntry{"  2", log.Fields{}})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertNumberOfCalls(suite.T(), "HandleLog", 4)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeEmptySubFields() {
	subFields := []log.Fields{}
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Origin":      1,
		"Destination": 2,
		"Function":    "Func",
		"Parameter":   "Param",
		"subFields":   subFields}

	suite.setupBaseHandlerForLogs(
		logEntry{"Info Log 1 Func Param -> 2", log.Fields{}},
		logEntry{"subFields: Empty", log.Fields{}})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertNumberOfCalls(suite.T(), "HandleLog", 2)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeDeferredSubFields_NotLoggedImmediately() {
	subFields := []log.Fields{log.Fields{"Deferred": true}}
	fields := log.Fields{
		"EntryType": "NodeAction",
		"Function":  "Func",
		"subFields": subFields}

	suite.setupBaseHandlerForLog("Info Log Func", log.Fields{})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertNumberOfCalls(suite.T(), "HandleLog", 1)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeDeferredSubFields_LoggedWhenOriginChanges() {
	subFields := []log.Fields{log.Fields{"Deferred": true}}
	fields := log.Fields{
		"EntryType": "NodeAction",
		"Origin":    1,
		"Function":  "Func",
		"subFields": subFields}

	entry := log.NewEntry(nil)
	entry.Fields = fields
	entry.Timestamp = defaultTimeStamp
	entry.Level = log.InfoLevel
	entry.Message = "Info Log"
	suite.sut.HandleLog(entry)

	fields = log.Fields{
		"EntryType": "NodeAction",
		"Origin":    2,
		"Function":  "Func"}
	suite.setupBaseHandlerForLogs(
		logEntry{"subFields: Empty", log.Fields{}},
		logEntry{"Info Log 2 Func", log.Fields{}})
	entry = log.NewEntry(nil)
	entry.Fields = fields
	entry.Timestamp = defaultTimeStamp
	entry.Level = log.InfoLevel
	entry.Message = "Info Log"
	suite.sut.HandleLog(entry)
	suite.baseHandler.AssertNumberOfCalls(suite.T(), "HandleLog", 3)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeDeferredSubFields_LoggedWhenTimeChanges() {
	subFields := []log.Fields{log.Fields{"Deferred": true}}
	fields := log.Fields{
		"EntryType": "NodeAction",
		"Origin":    1,
		"Function":  "Func",
		"subFields": subFields}

	entry := log.NewEntry(nil)
	entry.Fields = fields
	entry.Timestamp = defaultTimeStamp
	entry.Level = log.InfoLevel
	entry.Message = "Info Log"
	suite.sut.HandleLog(entry)

	suite.setupBaseHandlerForLogs(
		logEntry{"subFields: Empty", log.Fields{}},
		logEntry{"Info Log 1 Func", log.Fields{}})

	entry = log.NewEntry(nil)
	entry.Fields = fields
	entry.Timestamp = otherTimeStamp
	entry.Level = log.InfoLevel
	entry.Message = "Info Log"
	suite.sut.HandleLog(entry)
	suite.baseHandler.AssertNumberOfCalls(suite.T(), "HandleLog", 3)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeDeferredSubFields_LoggedWithOldTimeWhenTimeChanges() {
	subFields := []log.Fields{log.Fields{"Deferred": true}}
	fields := log.Fields{
		"EntryType": "NodeAction",
		"Origin":    1,
		"Function":  "Func",
		"subFields": subFields}

	entry := log.NewEntry(nil)
	entry.Fields = fields
	entry.Timestamp = defaultTimeStamp
	entry.Level = log.InfoLevel
	entry.Message = "Info Log"
	suite.sut.HandleLog(entry)

	suite.baseHandler.OnNew("HandleLog", mock.Anything).Run(func(args mock.Arguments) {
		entryArg := args.Get(0).(*log.Entry)
		suite.Assert().Equal(defaultTimeStamp, entryArg.Timestamp)
		suite.baseHandler.OnNew("HandleLog", mock.Anything).Run(func(args mock.Arguments) {
			entryArg := args.Get(0).(*log.Entry)
			suite.Assert().Equal(otherTimeStamp, entryArg.Timestamp)
		})
	})

	entry = log.NewEntry(nil)
	entry.Fields = fields
	entry.Timestamp = otherTimeStamp
	entry.Level = log.InfoLevel
	entry.Message = "Info Log"
	suite.sut.HandleLog(entry)
	suite.baseHandler.AssertNumberOfCalls(suite.T(), "HandleLog", 3)
}

func (suite *SimulationLogActionHandlerTest) Test_EntryTypeNodeActionIsFormattedWithDuration() {
	fields := log.Fields{
		"EntryType":   "NodeAction",
		"Duration":    5 * time.Second,
		"Origin":      1,
		"Destination": 2,
		"Function":    "Func",
		"Parameter":   "Param"}
	suite.setupBaseHandlerForLog(
		"Info Log +5s 1 Func Param -> 2",
		log.Fields{})
	entry := log.WithFields(fields)
	entry.Info("Info Log")
	suite.baseHandler.AssertCalledOnce(suite.T(), "HandleLog", mock.Anything)
}

// Private

func (suite *SimulationLogActionHandlerTest) setupBaseHandlerForLog(
	message string,
	fields log.Fields) {

	suite.setupBaseHandlerForLogs(logEntry{message, fields})
}

func (suite *SimulationLogActionHandlerTest) setupBaseHandlerForLogs(logEntries ...logEntry) {
	nextAssertIndex := 0
	suite.baseHandler.OnNew("HandleLog", mock.Anything).Run(func(args mock.Arguments) {
		suite.Assert().True(nextAssertIndex < len(logEntries))
		logEntry := logEntries[nextAssertIndex]
		nextAssertIndex++

		entryArg := args.Get(0).(*log.Entry)
		suite.Assert().Equal(logEntry.message, entryArg.Message)
		suite.Assert().Equal(len(logEntry.fields), len(entryArg.Fields))
		for key, value := range logEntry.fields {
			suite.Assert().Equal(value, entryArg.Fields[key])
		}
	})
}
