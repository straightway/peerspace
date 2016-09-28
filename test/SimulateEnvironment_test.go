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

package test

import (
	"io/ioutil"
	"log"
	"os"
	"testing"

	"github.com/straightway/straightway/general/duration"
	"github.com/straightway/straightway/simc"
	"github.com/stretchr/testify/assert"
)

func TestSimulationEnvironment(t *testing.T) {
	log.SetOutput(ioutil.Discard)
	scheduler := &simc.EventScheduler{}
	simc.NewSimulationEnvironment(scheduler, 5)
	scheduler.Schedule(duration.Parse("240h"), func() { scheduler.Stop() })
	scheduler.Run()
	log.SetOutput(os.Stderr)
}

func TestNodes(t *testing.T) {
	log.SetOutput(ioutil.Discard)
	env := simc.NewSimulationEnvironment(&simc.EventScheduler{}, 5)
	assert.Equal(t, 6, len(env.Nodes())) // +1 seed node
	log.SetOutput(os.Stderr)
}

func TestNodeForId_ExistingNode(t *testing.T) {
	log.SetOutput(ioutil.Discard)
	env := simc.NewSimulationEnvironment(&simc.EventScheduler{}, 5)
	for _, node := range env.Nodes() {
		assert.Equal(t, node, env.NodeModelForId(node.Id()))
	}
	log.SetOutput(os.Stderr)
}

func TestNodeForId_NotExistingNode(t *testing.T) {
	log.SetOutput(ioutil.Discard)
	env := simc.NewSimulationEnvironment(&simc.EventScheduler{}, 5)
	assert.Panics(t, func() { env.NodeModelForId("NotExistingId") })
	log.SetOutput(os.Stderr)
}
