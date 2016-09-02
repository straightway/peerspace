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

package ui

import (
	"github.com/andlabs/ui"
)

type VCenteredLabel struct {
	ui.Control
	label *ui.Label
}

func NewVCenteredLabel(text string) *VCenteredLabel {
	layout := ui.NewVerticalBox()
	result := &VCenteredLabel{
		Control: layout,
		label:   ui.NewLabel(text)}
	layout.Append(ui.NewVerticalBox(), true)
	layout.Append(result.label, false)
	layout.Append(ui.NewVerticalBox(), true)
	return result
}

func (this *VCenteredLabel) Text() string {
	return this.label.Text()
}

func (this *VCenteredLabel) SetText(text string) {
	this.label.SetText(text)
}
