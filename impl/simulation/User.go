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
	"time"

	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/simulation"
	"github.com/straightway/straightway/simulation/randvar"
)

type User struct {
	Node              peer.Node
	Scheduler         *EventScheduler
	StartupDuration   randvar.Duration
	OnlineDuration    randvar.Duration
	OnlineActivity    simulation.UserActivity
	attractiveQueries []peer.Query
	nextOfflineTime   time.Time
}

func (this *User) Id() string {
	return "UserOf_" + this.Node.Id()
}

func (this *User) Activate() {
	this.schedule(this.StartupDuration, this.doStartup)
}

func (this *User) AttractTo(query peer.Query) {
	this.attractiveQueries = append(this.attractiveQueries, query)
}

func (this *User) PopAttractiveQuery() (query peer.Query, isFound bool) {
	isFound = 0 < len(this.attractiveQueries)
	if isFound {
		query = this.attractiveQueries[0]
		this.attractiveQueries = this.attractiveQueries[1:]
	}

	return
}

// Private

func (this *User) doStartup() {
	this.Node.Startup()
	this.nextOfflineTime = this.schedule(this.OnlineDuration, this.doShutDown)
	this.OnlineActivity.ScheduleUntil(this.nextOfflineTime)
}

func (this *User) doShutDown() {
	this.Node.ShutDown()
	this.Activate()
}

func (this *User) schedule(duration randvar.Duration, action func()) time.Time {
	actionDuration := duration.NextSample()
	this.Scheduler.Schedule(actionDuration, action)
	return this.Scheduler.Time().Add(actionDuration)
}
