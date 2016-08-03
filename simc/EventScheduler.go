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

package simc

import (
	"sort"
	"time"
)

type EventScheduler struct {
	currentTime time.Time
	events      []event
	isRunning   bool
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
	return this.currentTime.In(time.UTC)
}

func (this *EventScheduler) Schedule(duration time.Duration, action func()) {
	if 0 <= duration {
		this.ScheduleAbsolute(this.currentTime.Add(duration), action)
	}
}

func (this *EventScheduler) ScheduleNextDayTime(daysFromNow int, clockTime time.Duration, action func()) bool {
	year, month, day := this.currentTime.Date()
	today := time.Date(year, month, day, 0, 0, 0, 0, this.currentTime.Location())
	targetTime := today.AddDate(0, 0, daysFromNow)
	isTargetTimeInFuture := targetTime.Before(this.currentTime) == false
	if isTargetTimeInFuture {
		this.ScheduleAbsolute(targetTime.Add(clockTime), action)
	}
	return isTargetTimeInFuture
}

func (this *EventScheduler) ScheduleAbsolute(time time.Time, action func()) {
	this.events = append(this.events, event{time: time, action: action})
	sort.Sort(eventByTime(this.events))
}

func (this *EventScheduler) Run() {
	this.isRunning = true
	for this.isRunning && this.hasEvent() {
		event := this.popEvent()
		this.execute(event)
	}
	this.events = nil
}

func (this *EventScheduler) Stop() {
	this.isRunning = false
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
