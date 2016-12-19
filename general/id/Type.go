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

package id

import "fmt"

const (
	Size = 16
)

type Type [Size]byte

func Empty() Type {
	return Type{}
}

func (this Type) String() string {
	result := ""
	for _, char := range this {
		if char == 0 {
			break
		}

		result += string(char)
	}

	return result
}

func FromString(s string) Type {
	if Size < len(s) {
		panic(fmt.Sprintf("Cannot convert '%v' to id.Type because it is too long", s))
	}

	result := Type{}
	copy(result[:], []byte(s))
	return result
}
