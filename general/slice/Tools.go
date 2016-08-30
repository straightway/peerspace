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

package slice

import (
	"fmt"
	"reflect"

	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/loop"
)

func Contains(slice interface{}, itemToCompare general.Equaler) bool {
	itemFound := false
	ForEachItem(slice, func(c interface{}) loop.Control {
		itemFound = general.AreEqual(c, itemToCompare)
		return loop.BreakIf(itemFound)
	})

	return itemFound
}

func SetUnion(slices ...interface{}) interface{} {
	var result reflect.Value
	hasResult := false
	for _, slice := range slices {
		if slice == nil {
			continue
		}

		if !hasResult {
			result = makeEmptySliceOfSameTypeAs(slice)
			hasResult = true
		}

		result = addDisjoint(result, slice)
	}

	if hasResult {
		return result.Interface()
	} else {
		return nil
	}
}

func ForEachItem(slice interface{}, do func(item interface{}) loop.Control) {
	if slice == nil {
		return
	}

	itemsType := reflect.TypeOf(slice)
	switch itemsType.Kind() {
	case reflect.Slice:
		itemsSlice := reflect.ValueOf(slice)
		for i := 0; i < itemsSlice.Len(); i++ {
			if do(itemsSlice.Index(i).Interface()) == loop.Break {
				break
			}
		}
	default:
		panic(fmt.Sprintf("slice has invalid type %T", slice))
	}
}

func RemoveItemsIf(slice interface{}, predicate func(item interface{}) bool) interface{} {
	if predicate == nil {
		panic("No predicate")
	}
	sliceValue := reflect.ValueOf(slice)
	if slice == nil {
		return nil
	}

	result := makeEmptySliceOfSameTypeAs(slice)
	inputLen := sliceValue.Len()
	for i := 0; i < inputLen; i++ {
		item := sliceValue.Index(i)
		if !predicate(interfaceOrNil(item)) {
			result = reflect.Append(result, item)
		}
	}

	return result.Interface()
}

func Cast(from interface{}, to interface{}) interface{} {
	result := makeEmptySliceOfSameTypeAs(to)
	ForEachItem(from, func(item interface{}) loop.Control {
		result = reflect.Append(result, reflect.ValueOf(item))
		return loop.Continue
	})
	return interfaceOrNil(result)
}

// Private

func interfaceOrNil(value reflect.Value) interface{} {
	ivalue := value.Interface()
	if ivalue != nil && value.Kind() >= reflect.Array && value.Kind() != reflect.Struct && value.IsNil() {
		return nil
	}
	return ivalue
}

func makeEmptySliceOfSameTypeAs(a interface{}) reflect.Value {
	targetType := reflect.TypeOf(a)
	return reflect.MakeSlice(targetType, 0, 0)
}

func addDisjoint(dst reflect.Value, src interface{}) reflect.Value {
	ForEachItem(src, func(item interface{}) loop.Control {
		if !Contains(dst.Interface(), item.(general.Equaler)) {
			dst = reflect.Append(dst, reflect.ValueOf(item))
		}

		return loop.Continue
	})

	return dst
}
