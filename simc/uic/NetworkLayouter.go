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

package uic

import (
	"math"

	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/sim/randvar"
	"github.com/straightway/straightway/simc/ui"
)

type NetworkLayouter struct {
	NodeSelector           randvar.Intner
	ConnectionForce        float64
	AnitGravityForce       float64
	MinX, MinY, MaxX, MaxY float64
	TimeFactor             float64
}

func (this *NetworkLayouter) SetBounds(minX, minY, maxX, maxY float64) {
	this.MinX = minX
	this.MinY = minY
	this.MaxX = maxX
	this.MaxY = maxY
}

func (this *NetworkLayouter) ImproveLayoutOf(network ui.NetworkModel) {
	if this.areBoundsInvalid() {
		return
	}

	nodes := network.Nodes()
	if len(nodes) == 0 {
		return
	}

	indexOfNodeToChange := this.NodeSelector.Intn(len(nodes))
	changedNode := nodes[indexOfNodeToChange]
	dx, dy := 0.0, 0.0
	for _, otherNode := range nodes {
		if changedNode == otherNode {
			continue
		}

		agX, agY := this.forces(changedNode, otherNode)
		dx += agX
		dy += agY
	}

	x, y := changedNode.Position()
	x, y = this.clip(x+dx*this.TimeFactor, y+dy*this.TimeFactor)

	changedNode.SetPosition(x, y)
}

func (this *NetworkLayouter) Centralize(network ui.NetworkModel) {
	nodes := network.Nodes()
	numNodes := float64(len(nodes))
	centerOfMassX, centerOfMassY := 0.0, 0.0
	for _, node := range nodes {
		x, y := node.Position()
		centerOfMassX += x
		centerOfMassY += y
	}

	centerOfMassX /= numNodes
	centerOfMassY /= numNodes

	centerX := (this.MaxX + this.MinX) / 2.0
	centerY := (this.MaxY + this.MinY) / 2.0

	for _, node := range nodes {
		x, y := node.Position()
		x -= centerOfMassX - centerX
		y -= centerOfMassY - centerY
		node.SetPosition(x, y)
	}
}

// Private

func (this *NetworkLayouter) areBoundsInvalid() bool {
	return this.MaxX <= this.MinX || this.MaxY <= this.MinY
}

func (this *NetworkLayouter) forces(a, b ui.NodeModel) (x, y float64) {
	ax, ay := a.Position()
	bx, by := b.Position()
	dx, dy := bx-ax, by-ay
	if dx == 0.0 && dy == 0.0 {
		dx = 1.0
	}

	x, y = this.antiGravityForce(dx, dy)

	if slice.Contains(a.Connections(), b) {
		cx, cy := this.connectionForce(dx, dy)
		x += cx
		y += cy
	}

	return
}

func (this *NetworkLayouter) antiGravityForce(dx, dy float64) (x, y float64) {
	d := math.Sqrt(dx*dx + dy*dy)
	antiGravityForce := -this.AnitGravityForce / ((1 + d*d) * d)
	x = dx * antiGravityForce
	y = dy * antiGravityForce
	return
}

func (this *NetworkLayouter) connectionForce(dx, dy float64) (x, y float64) {
	x = dx * this.ConnectionForce
	y = dy * this.ConnectionForce
	return
}

func (this *NetworkLayouter) clip(ix, iy float64) (x, y float64) {
	x, y = ix, iy
	if x < this.MinX {
		x = this.MinX
	} else if this.MaxX < x {
		x = this.MaxX
	}
	if y < this.MinY {
		y = this.MinY
	} else if this.MaxY < y {
		y = this.MaxY
	}
	return
}
