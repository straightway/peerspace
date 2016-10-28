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

package activity

import (
	"io/ioutil"
	"log"
	"os"
	"time"

	"github.com/stretchr/testify/suite"

	"github.com/straightway/straightway/general/duration"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/sim/measure"
	"github.com/straightway/straightway/sim/randvar"
	"github.com/straightway/straightway/simc"
	"github.com/straightway/straightway/simc/test"
)

type TestBase struct {
	suite.Suite
	scheduler   *simc.EventScheduler
	user        *simc.User
	node        *peer.NodeMock
	offlineTime time.Time
}

func (suite *TestBase) SetupTest() {
	log.SetOutput(ioutil.Discard)
	suite.scheduler = simc.NewEventScheduler()
	suite.node = peer.NewNodeMock("1")
	suite.user = &simc.User{
		SchedulerInstance:            suite.scheduler,
		NodeInstance:                 suite.node,
		QueryWaitingTimeout:          duration.Parse("1h"),
		QueryDurationSampleCollector: measure.NewSampleCollectorMock(),
		QuerySuccessSampleCollector:  measure.NewSampleCollectorMock(),
		QuerySelectionSelector:       randvar.NewIntnerMock(0)}
	suite.scheduler.Schedule(duration.Parse("1000h"), func() {
		suite.scheduler.Stop()
	})
	now := suite.scheduler.Time()
	suite.offlineTime = now.Add(test.OnlineDuration)
}

func (suite *TestBase) TearDownTest() {
	suite.scheduler = nil
	suite.node = nil
	suite.user = nil
	log.SetOutput(os.Stderr)
}
