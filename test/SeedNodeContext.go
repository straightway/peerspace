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
	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/peerc"
)

type SeedNodeContext struct {
	NodeContextBase
	node *peerc.SeedNode
}

// Construction

func NewSeedNodeContext() *SeedNodeContext {
	newNodeContext := &SeedNodeContext{}
	newNodeContext.timer = &mocked.Timer{}
	newNodeContext.configuration = &app.Configuration{}
	newNodeContext.createSut = newNodeContext.createSeedNode
	newNodeContext.NodeContextBase.setupPeers = func() {}

	return newNodeContext
}

// Private

func (this *SeedNodeContext) createSeedNode() peer.Node {
	this.node = &peerc.SeedNode{}
	this.node.StateStorage = this.stateStorage
	this.node.Timer = this.timer
	this.node.Configuration = this.configuration

	for _, p := range this.connectedPeers {
		p.On("RequestConnectionWith", this.node).Return()
	}

	return this.node
}
