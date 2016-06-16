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

package general

import (
	"fmt"
	"reflect"
)

func AssertFieldsNotNil(obj interface{}, fieldNames ...string) {
	objValue := reflect.ValueOf(obj)
	for _, fieldName := range fieldNames {
		field := objValue.FieldByName(fieldName)
		switch field.Kind() {
		case reflect.Invalid:
			panic(fmt.Sprintf("Invalid field: %v", fieldName))
		case reflect.Interface:
			fallthrough
		case reflect.Ptr:
			fallthrough
		case reflect.Chan:
			fallthrough
		case reflect.Func:
			fallthrough
		case reflect.Map:
			fallthrough
		case reflect.Slice:
			if field.IsNil() {
				panic(fmt.Sprintf("Field %v is nil", fieldName))
			}
		}
	}
}
