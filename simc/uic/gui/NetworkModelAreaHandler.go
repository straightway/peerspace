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

package gui

import (
	"math"

	"github.com/andlabs/ui"
	"github.com/straightway/straightway/general/slice"
	simui "github.com/straightway/straightway/simc/ui"
)

type NetworkModelAreaHandler struct {
	Model                  simui.NetworkModel
	NodeSize               float64
	NodeColor              ui.Brush
	BoundsReceiver         BoundsReceiver
	handledConnectionNodes []connection
}

type connection struct {
	from simui.NodeModel
	to   simui.NodeModel
}

var black *ui.Brush = &ui.Brush{R: 0.0, B: 0.0, G: 0.0, A: 1.0}

func (this *NetworkModelAreaHandler) Draw(a *ui.Area, dp *ui.AreaDrawParams) {
	this.BoundsReceiver.SetBounds(
		this.NodeSize,
		this.NodeSize,
		dp.AreaWidth-this.NodeSize,
		dp.AreaHeight-this.NodeSize)

	for _, node := range this.Model.Nodes() {
		this.drawNode(node, dp)
	}
	this.handledConnectionNodes = nil
}

func (this *NetworkModelAreaHandler) drawNode(
	node simui.NodeModel,
	dp *ui.AreaDrawParams) {

	dp.Context.Save()
	p := ui.NewPath(ui.Winding)
	x, y := node.Position()
	p.NewFigureWithArc(x, y, this.NodeSize, 0.0, 2.0*math.Pi, false)
	p.End()
	dp.Context.Fill(p, &this.NodeColor)
	dp.Context.Restore()

	for _, connectedNode := range node.Connections() {
		this.drawConnection(node, connectedNode, dp)
	}
}

func (this *NetworkModelAreaHandler) drawConnection(
	from simui.NodeModel,
	to simui.NodeModel,
	dp *ui.AreaDrawParams) {

	if slice.Contains(this.handledConnectionNodes, connection{from, to}) {
		return
	}

	dp.Context.Save()
	p := ui.NewPath(ui.Winding)
	xFrom, yFrom := from.Position()
	xTo, yTo := to.Position()
	p.NewFigure(xFrom, yFrom)
	p.LineTo(xTo, yTo)
	p.End()
	stroke := &ui.StrokeParams{Thickness: 1.0}
	toConnections := to.Connections()
	if slice.Contains(toConnections, from) {
		this.handledConnectionNodes = append(this.handledConnectionNodes, connection{to, from})
	} else {
		stroke.Dashes = []float64{5.0}
	}
	dp.Context.Stroke(p, black, stroke)
	dp.Context.Restore()

	this.handledConnectionNodes = append(this.handledConnectionNodes, connection{from, to})
}

func (this *NetworkModelAreaHandler) MouseEvent(a *ui.Area, me *ui.AreaMouseEvent)            {}
func (this *NetworkModelAreaHandler) MouseCrossed(a *ui.Area, left bool)                      {}
func (this *NetworkModelAreaHandler) DragBroken(a *ui.Area)                                   {}
func (this *NetworkModelAreaHandler) KeyEvent(a *ui.Area, ke *ui.AreaKeyEvent) (handled bool) { return }
