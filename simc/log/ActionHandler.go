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
	"fmt"
	"strings"
	"time"

	"github.com/apex/log"

	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/general/times"
)

type ActionHandler struct {
	baseHandler       log.Handler
	deferredSubFields map[string]subField
	lastLogTime       time.Time
}

type subField struct {
	message string
	origin  interface{}
	items   []log.Fields
	logTime time.Time
}

func (this *subField) log(baseHandler log.Handler, logger *log.Logger) {
	suffix := ""
	if len(this.items) == 0 {
		suffix = ": Empty"
	}

	logHandler := NewActionHandler(baseHandler)
	this.logHeading(suffix, logHandler, logger)
	this.logEntries(logHandler, logger)
}

func (this *subField) logHeading(suffix string, logHandler log.Handler, logger *log.Logger) {
	heading := log.NewEntry(logger)
	heading.Message = fmt.Sprintf("%v%v", this.message, suffix)
	heading.Timestamp = this.logTime
	logHandler.HandleLog(heading)
}

func (this *subField) logEntries(logHandler log.Handler, logger *log.Logger) {
	for _, subItem := range this.items {
		itemEntry := log.NewEntry(logger)
		itemEntry.Message = " "
		itemEntry.Timestamp = this.logTime
		itemEntry.Fields = subItem
		logHandler.HandleLog(itemEntry)
	}
}

var formattedFields []string = []string{
	"#+%v",
	"Duration",
	"Origin",
	"Function",
	"Parameter",
	"#-> %v",
	"Destination"}

func NewActionHandler(baseHandler log.Handler) *ActionHandler {
	return &ActionHandler{
		baseHandler:       baseHandler,
		deferredSubFields: make(map[string]subField),
		lastLogTime:       times.Max()}
}

func (this *ActionHandler) HandleLog(entry *log.Entry) error {
	if IsEnabled() == false {
		return nil
	}

	entryTime := entry.Timestamp.In(time.UTC)
	if entryTime.Equal(this.lastLogTime) == false {
		this.lastLogTime = entryTime
		this.logDeferredSubItems(entry.Logger)
	} else {
		this.logDeferredFieldsWithDifferentOrigin(entry)
	}

	entryType := entry.Fields["EntryType"]
	if entryType != "NodeAction" {
		return this.baseHandler.HandleLog(entry)
	}

	this.logPlain(entry)
	this.logSubItems(entry)

	return nil
}

// Private

func (this *ActionHandler) logPlain(entry *log.Entry) {
	formattedEntry := log.NewEntry(entry.Logger)
	formattedEntry.Message = formattedMessage(entry)
	formattedEntry.Fields = additionalValues(entry)
	formattedEntry.Timestamp = entry.Timestamp
	this.baseHandler.HandleLog(formattedEntry)
}

func (this *ActionHandler) logSubItems(entry *log.Entry) {
	for _, subField := range this.subFields(entry) {
		subField.log(this.baseHandler, entry.Logger)
	}
}

func (this *ActionHandler) subFields(entry *log.Entry) []subField {
	result := []subField{}
	origin, _ := entry.Fields["Origin"]

	for key, value := range entry.Fields {
		if isSubField(value) {
			fields := value.([]log.Fields)
			if isDeferred(fields) {
				this.deferredSubFields[key] = subField{key, origin, fields[1:], entry.Timestamp}
				continue
			}

			result = append(result, subField{key, origin, fields, entry.Timestamp})
		}
	}

	return result
}

func (this *ActionHandler) logDeferredFieldsWithDifferentOrigin(entry *log.Entry) {
	origin, _ := entry.Fields["Origin"]
	var keysToDelete []string
	var fieldsToLog []subField
	for key, value := range this.deferredSubFields {
		if value.origin != origin {
			keysToDelete = append(keysToDelete, key)
			fieldsToLog = append(fieldsToLog, value)
		}
	}

	for _, key := range keysToDelete {
		delete(this.deferredSubFields, key)
	}

	for _, field := range fieldsToLog {
		field.log(this.baseHandler, entry.Logger)
	}
}

func (this *ActionHandler) logDeferredSubItems(logger *log.Logger) {
	for _, value := range this.deferredSubFields {
		value.log(this.baseHandler, logger)
	}

	this.deferredSubFields = map[string]subField{}
}

func isDeferred(fields []log.Fields) bool {
	if len(fields) == 0 {
		return false
	}

	deferredValue, isDeferredFound := fields[0]["Deferred"]
	if isDeferredFound == false {
		return false
	}

	idDeferredEnabled, isDeferredFound := deferredValue.(bool)
	return isDeferredFound && idDeferredEnabled
}

func formattedMessage(entry *log.Entry) string {
	entries := make([]string, 0, 5)
	if 0 < len(entry.Message) {
		entries = append(entries, entry.Message)
	}

	entries = listFieldValueIfExists(
		entries,
		entry,
		formattedFields...)

	return slice.ToString(entries, " ")
}

func listFieldValueIfExists(appendTo []string, entry *log.Entry, fieldNameOrFormats ...string) []string {
	nextFormat := "%v"
	for _, fieldNameOrFormat := range fieldNameOrFormats {
		if strings.HasPrefix(fieldNameOrFormat, "#") {
			nextFormat = fieldNameOrFormat[1:]
			continue
		}

		if value, ok := entry.Fields[fieldNameOrFormat]; ok {
			appendTo = append(appendTo, fmt.Sprintf(nextFormat, value))
		}

		nextFormat = "%v"
	}

	return appendTo
}

func additionalValues(entry *log.Entry) log.Fields {
	notFormattedFields := log.Fields{}
	for key, value := range entry.Fields {
		if isSubField(value) || key == "EntryType" || slice.Contains(formattedFields, key) {
			continue
		}

		notFormattedFields[key] = value
	}

	return notFormattedFields
}

func isSubField(fieldValue interface{}) bool {
	_, isValueFieldSlice := fieldValue.([]log.Fields)
	return isValueFieldSlice
}
