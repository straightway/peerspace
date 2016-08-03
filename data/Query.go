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

package data

type Query struct {
	Id       Id
	TimeFrom int64
	TimeTo   int64
}

func QueryExactlyKey(key Key) Query {
	return Query{Id: key.Id, TimeTo: key.TimeStamp, TimeFrom: key.TimeStamp}
}

func (this *Query) Matches(key Key) bool {
	return this.Id == key.Id && this.TimeFrom <= key.TimeStamp && key.TimeStamp <= this.TimeTo
}

func (this *Query) MatchesOnly(key Key) bool {
	return this.Id == key.Id && this.TimeFrom == key.TimeStamp && this.TimeTo == key.TimeStamp
}

func (this *Query) IsTimed() bool {
	return this.TimeFrom != 0 || this.TimeTo != 0
}
