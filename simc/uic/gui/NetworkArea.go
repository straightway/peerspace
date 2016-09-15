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
	"github.com/andlabs/ui"
)

type NetworkArea struct {
	ui.Control
	areaHandler ui.AreaHandler
}

type networkAreaHandler struct {
	Color ui.Brush
	Text  string
}

func (this *networkAreaHandler) Draw(a *ui.Area, dp *ui.AreaDrawParams) {
	dp.Context.Save()
	p := ui.NewPath(ui.Winding)
	p.AddRectangle(0.0, 0.0, dp.AreaWidth, dp.AreaHeight)
	p.End()
	dp.Context.Fill(p, &this.Color)
	dp.Context.Restore()
	fontDesc := ui.FontDescriptor{
		Family:  "sans",
		Size:    12.0,
		Weight:  ui.TextWeightNormal,
		Italic:  ui.TextItalicNormal,
		Stretch: ui.TextStretchNormal}
	font := ui.LoadClosestFont(&fontDesc)
	txt := ui.NewTextLayout(this.Text, font, 20.0)
	dp.Context.Text(10.0, 10.0, txt)
	txt.Free()
}

func (this *networkAreaHandler) MouseEvent(a *ui.Area, me *ui.AreaMouseEvent)            {}
func (this *networkAreaHandler) MouseCrossed(a *ui.Area, left bool)                      {}
func (this *networkAreaHandler) DragBroken(a *ui.Area)                                   {}
func (this *networkAreaHandler) KeyEvent(a *ui.Area, ke *ui.AreaKeyEvent) (handled bool) { return }

func NewNetworkArea() *NetworkArea {
	handler := &networkAreaHandler{
		Text:  "Network",
		Color: ui.Brush{R: 0.0, G: 1.0, B: 0.0, A: 0.5}}
	area := ui.NewArea(handler)
	return &NetworkArea{
		Control:     area,
		areaHandler: handler}
}
