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

package env

import (
	"testing"

	"github.com/apex/log"
	"github.com/apex/log/handlers/discard"

	"github.com/stretchr/testify/suite"

	"github.com/straightway/straightway/general/duration"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/simc"
)

type Environment_Test struct {
	suite.Suite
	sut       *Environment
	scheduler *simc.EventScheduler
}

func TestEnvironment(t *testing.T) {
	suite.Run(t, new(Environment_Test))
}

func (suite *Environment_Test) SetupTest() {
	log.SetHandler(discard.Default)
	suite.scheduler = simc.NewEventScheduler()
	suite.sut = New(suite.scheduler, 5)
}

func (suite *Environment_Test) TearDownTest() {
	suite.scheduler = nil
	suite.sut = nil
	log.SetHandler(nil)
}

// Tests

func (suite *Environment_Test) Test_SchedulingEvents() {
	suite.scheduler.Schedule(duration.Parse("10h"), func() { suite.scheduler.Stop() })
	suite.scheduler.Run()
}

func (suite *Environment_Test) Test_Nodes() {
	suite.Assert().Equal(6, len(suite.sut.Nodes())) // +1 seed node
}

func (suite *Environment_Test) Test_NodeForId_ExistingNode() {
	for _, node := range suite.sut.Nodes() {
		suite.Assert().Equal(node, suite.sut.NodeModelForId(node.Id()))
	}
}

func (suite *Environment_Test) Test_NodeForId_NotExistingNode() {
	suite.Assert().Panics(func() { suite.sut.NodeModelForId(id.FromString("NotExistingId")) })
}

func (suite *Environment_Test) Test_QueryDurationMeasure() {
	suite.Assert().NotNil(suite.sut.QueryDurationMeasure())
}

func (suite *Environment_Test) Test_QuerySuccessMeasure() {
	suite.Assert().NotNil(suite.sut.QuerySuccessMeasure())
}
