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

type TextTable struct {
	ui.Control
	cols   int
	rows   int
	labels []*ui.Label
}

func NewTextTable(cols, rows int) *TextTable {
	mainBox := ui.NewHorizontalBox()
	mainBox.SetPadded(true)
	labels := make([]*ui.Label, rows*cols)
	for c := 0; c < cols; c++ {
		col := ui.NewVerticalBox()
		col.SetPadded(true)
		mainBox.Append(col, false)
		for r := 0; r < rows; r++ {
			label := ui.NewLabel("")
			col.Append(label, false)
			labels[r*cols+c] = label
		}
		col.Append(ui.NewHorizontalBox(), true)
	}
	mainBox.Append(ui.NewVerticalBox(), true)

	return &TextTable{
		Control: mainBox,
		cols:    cols,
		rows:    rows,
		labels:  labels}
}

func (this *TextTable) SetText(col, row int, text string) {
	this.labels[row*this.cols+col].SetText(text)
}
