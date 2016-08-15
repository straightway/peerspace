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

	"github.com/straightway/straightway/sim/randvar"
)

type Query struct {
	Scheduler          *EventScheduler
	User               *User
	QueryPauseDuration randvar.Duration
}

func (this *Query) ScheduleUntil(maxTime time.Time) {
	currSimTime := this.Scheduler.Time()
	for {
		currSimTime = currSimTime.Add(this.QueryPauseDuration.NextSample())
		if maxTime.Before(currSimTime) {
			return
		}

		this.Scheduler.ScheduleAbsolute(currSimTime, this.doQuery)
	}
}

// Private

func (this *Query) doQuery() {
	query, isQueryFound := this.User.PopAttractiveQuery()
	if isQueryFound {
		this.User.Node.Query(query, this.User)
	}
}
