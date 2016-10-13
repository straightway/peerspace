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

	"github.com/apex/log"

	"github.com/straightway/straightway/general/slice"
)

type ActionHandler struct {
	baseHandler log.Handler
}

type subField struct {
	message string
	items   []log.Fields
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
	return &ActionHandler{baseHandler: baseHandler}
}

func (this *ActionHandler) HandleLog(entry *log.Entry) error {
	entryType := entry.Fields["EntryType"]
	if entryType != "NodeAction" {
		return this.baseHandler.HandleLog(entry)
	}

	formattedEntry := log.NewEntry(entry.Logger)
	formattedEntry.Message = formattedMessage(entry)
	formattedEntry.Fields = additionalValues(entry)
	this.baseHandler.HandleLog(formattedEntry)
	for _, subField := range subFields(entry) {
		subFieldHeading := log.NewEntry(entry.Logger)
		subFieldHeading.Message = subField.message
		this.baseHandler.HandleLog(subFieldHeading)
		for _, subItem := range subField.items {
			itemEntry := log.NewEntry(entry.Logger)
			itemEntry.Message = " "
			itemEntry.Fields = subItem
			this.HandleLog(itemEntry)
		}
	}
	return nil
}

// Private

func subFields(entry *log.Entry) []subField {
	result := []subField{}
	for key, value := range entry.Fields {
		if isSubField(value) {
			result = append(result, subField{key, value.([]log.Fields)})
		}
	}

	return result
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
