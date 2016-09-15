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
	"log"
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/sim/measure"
	"github.com/straightway/straightway/sim/randvar"
)

type User struct {
	NodeInstance         peer.Node
	SchedulerInstance    sim.EventScheduler
	StartupDuration      randvar.Duration
	OnlineDuration       randvar.Duration
	OnlineActivity       sim.UserActivity
	QuerySampleCollector measure.SampleCollector
	attractiveQueries    []data.Query
	pendingQueries       []queryRecord
	nextOfflineTime      time.Time
}

type queryRecord struct {
	query     data.Query
	startTime time.Time
}

func (this *User) Id() string {
	return "UserOf_" + this.NodeInstance.Id()
}

func (this *User) Equal(other general.Equaler) bool {
	otherUser, isOtherUser := other.(*User)
	return isOtherUser && otherUser.Id() == this.Id()
}

func (this *User) Push(data *data.Chunk, origin id.Holder) {
	currTime := this.Scheduler().Time()
	for i, qr := range this.pendingQueries {
		if qr.query.Matches(data.Key) {
			queryDuration := currTime.Sub(qr.startTime)
			this.QuerySampleCollector.AddSample(queryDuration.Seconds())
			this.pendingQueries = append(this.pendingQueries[:i], this.pendingQueries[i+1:]...)
			break
		}
	}
}

func (this *User) Activate() {
	this.schedule(this.StartupDuration, this.doStartup)
}

func (this *User) AttractTo(query data.Query) {
	this.attractiveQueries = append(this.attractiveQueries, query)
}

func (this *User) PopAttractiveQuery() (query data.Query, isFound bool) {
	isFound = 0 < len(this.attractiveQueries)
	if isFound {
		query = this.attractiveQueries[0]
		this.attractiveQueries = this.attractiveQueries[1:]
		qr := queryRecord{query: query, startTime: this.Scheduler().Time()}
		this.pendingQueries = append(this.pendingQueries, qr)
	}

	return
}

func (this *User) Node() peer.Node {
	return this.NodeInstance
}

func (this *User) Scheduler() sim.EventScheduler {
	return this.SchedulerInstance
}

// Private

func (this *User) doStartup() {
	log.Printf(
		"%v: %v is started",
		this.SchedulerInstance.Time(),
		this.Id())
	this.NodeInstance.Startup()
	this.nextOfflineTime = this.schedule(this.OnlineDuration, this.doShutDown)
	this.OnlineActivity.ScheduleUntil(this.nextOfflineTime)
}

func (this *User) doShutDown() {
	log.Printf(
		"%v: %v is shut down",
		this.SchedulerInstance.Time(),
		this.Id())
	this.NodeInstance.ShutDown()
	this.Activate()
}

func (this *User) schedule(duration randvar.Duration, action func()) time.Time {
	actionDuration := duration.NextSample()
	this.SchedulerInstance.Schedule(actionDuration, action)
	return this.SchedulerInstance.Time().Add(actionDuration)
}
