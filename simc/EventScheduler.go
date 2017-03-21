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
	"time"

	"github.com/straightway/straightway/general/sorted"
)

type EventScheduler struct {
	currentTime       time.Time
	events            *sorted.Queue
	isStopped         bool
	execEventHandlers []func()
}

func NewEventScheduler() *EventScheduler {
	return &EventScheduler{
		currentTime: time.Time{}.In(time.UTC),
		events:      sorted.NewQueue()}
}

type event struct {
	time   time.Time
	action func()
}

func (this *event) IsLessThan(other sorted.Item) bool {
	return this.time.Before(other.(*event).time)
}

func (this *EventScheduler) Time() time.Time {
	return this.currentTime
}

func (this *EventScheduler) Schedule(duration time.Duration, action func()) {
	if duration < 0 {
		panic("Cannot schedule negative duration")
	}

	this.ScheduleAbsolute(this.currentTime.Add(duration), action)
}

func (this *EventScheduler) ScheduleAbsolute(execTime time.Time, action func()) {
	if execTime.Before(this.currentTime) {
		panic("Cannot schedule past event")
	}

	this.events.Insert(&event{time: execTime.In(time.UTC), action: action})
}

func (this *EventScheduler) Run() {
	this.Resume()
	for this.ExecNext() {
	}
}

func (this *EventScheduler) ExecNext() bool {
	if this.isStopped {
		return false
	}

	evt := this.events.Pop()
	if evt == nil {
		return false
	}
	this.execute(evt.(*event))

	for _, h := range this.execEventHandlers {
		h()
	}

	return true
}

func (this *EventScheduler) Stop() {
	this.isStopped = true
}

func (this *EventScheduler) Resume() {
	this.isStopped = false
}

func (this *EventScheduler) Reset() {
	this.events = sorted.NewQueue()
	this.currentTime = time.Time{}.In(time.UTC)
	this.Resume()
}

func (this *EventScheduler) RegisterForExecEvent(callback func()) {
	this.execEventHandlers = append(this.execEventHandlers, callback)
}

// Private

func (this *EventScheduler) execute(event *event) {
	this.currentTime = event.time
	event.action()
}
