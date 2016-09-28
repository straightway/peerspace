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
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/simc/ui"
	"github.com/straightway/straightway/strategy"
)

type NodeModel struct {
	id             string
	nodeModels     NodeModelRepository
	connectionInfo strategy.ConnectionInfoProvider
	x, y           float64
}

func NewNodeModel(
	id string,
	nodeModels NodeModelRepository,
	connectionInfo strategy.ConnectionInfoProvider) *NodeModel {

	result := &NodeModel{
		id:             id,
		nodeModels:     nodeModels,
		connectionInfo: connectionInfo}
	return result
}

func (this *NodeModel) Equal(other general.Equaler) bool {
	otherNodeModel, isOtherNodeModel := other.(*NodeModel)
	return isOtherNodeModel && otherNodeModel.Id() == this.id
}

func (this *NodeModel) Id() string {
	return this.id
}

func (this *NodeModel) Position() (x, y float64) {
	return this.x, this.y
}

func (this *NodeModel) SetPosition(x, y float64) {
	this.x = x
	this.y = y
}

func (this *NodeModel) Connections() []ui.NodeModel {
	connectedPeers := this.connectionInfo.ConnectedPeers()
	connectingPeers := this.connectionInfo.ConnectingPeers()
	result := make([]ui.NodeModel, 0, len(connectedPeers)+len(connectingPeers))
	for _, peer := range connectedPeers {
		result = append(result, this.nodeModels.NodeModelForId(peer.Id()))
	}
	for _, peer := range connectingPeers {
		result = append(result, this.nodeModels.NodeModelForId(peer.Id()))
	}

	return result
}
