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

package ranges

import "fmt"

type Int64 [2]int64

func (this Int64) IntersectsWith(other Int64) bool {
	return this.isValid() && other.isValid() && !this.notIntersectingWith(other)
}

func (this Int64) String() string {
	return fmt.Sprintf("[%v,%v[", this[0], this[1])
}

// Private

func (this Int64) isValid() bool {
	return this[0] <= this[1]
}

func (this Int64) notIntersectingWith(other Int64) bool {
	return this[1] <= other[0] || other[1] <= this[0]
}
