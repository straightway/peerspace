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

package simulation

import (
	"sort"
	"time"
)

type EventScheduler struct {
	currentTime time.Time
	events      []event
}

type event struct {
	time   time.Time
	action func()
}

type eventByTime []event

func (a eventByTime) Len() int           { return len(a) }
func (a eventByTime) Swap(i, j int)      { a[i], a[j] = a[j], a[i] }
func (a eventByTime) Less(i, j int) bool { return a[i].time.Before(a[j].time) }

func (this *EventScheduler) Time() time.Time {
	return this.currentTime
}

func (this *EventScheduler) Schedule(duration time.Duration, action func()) {
	this.events = append(this.events, event{time: this.currentTime.Add(duration), action: action})
	sort.Sort(eventByTime(this.events))
}

func (this *EventScheduler) Run() {
	for this.hasEvent() {
		event := this.popEvent()
		this.execute(event)
	}
}

func (this *EventScheduler) Stop() {
	this.events = nil
}

// Private

func (this *EventScheduler) hasEvent() bool { return 0 < len(this.events) }

func (this *EventScheduler) popEvent() event {
	event := this.events[0]
	this.events = this.events[1:]
	return event
}

func (this *EventScheduler) execute(event event) {
	this.currentTime = event.time
	event.action()
}
