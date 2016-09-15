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
	"time"

	"github.com/andlabs/ui"
	"github.com/straightway/straightway/general/gui"
	sui "github.com/straightway/straightway/simc/ui"
)

type MainWindow struct {
	*ui.Window
	controller            sui.Controller
	startButton           *ui.Button
	resetButton           *ui.Button
	pauseButton           *ui.Button
	simulationTimeDisplay *gui.VCenteredLabel
	measurementTable      *gui.TextTable
}

func NewMainWindow(controller sui.Controller) *MainWindow {
	mainWindow := &MainWindow{controller: controller}
	mainWindow.init()
	return mainWindow
}

func (this *MainWindow) SetStartEnabled(enabled bool) {
	setEnabled(this.startButton, enabled)
}

func (this *MainWindow) SetResetEnabled(enabled bool) {
	setEnabled(this.resetButton, enabled)
}

func (this *MainWindow) SetPauseEnabled(enabled bool) {
	setEnabled(this.pauseButton, enabled)
}

func (this *MainWindow) SetSimulationTime(time time.Time) {
	this.simulationTimeDisplay.SetText(time.Format("2006-01-02 15:04:05.000"))
}

func (this *MainWindow) SetQueryDurationMeasurementValue(newValue string) {
	this.measurementTable.SetText(1, 1, newValue)
}

// Event handlers

func (this *MainWindow) onStartClicked(*ui.Button) {
	this.controller.Start()
}

func (this *MainWindow) onResetClicked(*ui.Button) {
	this.controller.Reset()
}

func (this *MainWindow) onPauseClicked(*ui.Button) {
	this.controller.Pause()
}

// Private

func (this *MainWindow) init() {
	this.Window = ui.NewWindow("Straightway Simulation", 200, 100, false)

	mainLayout := ui.NewVerticalBox()

	commandBar := ui.NewHorizontalBox()
	mainLayout.Append(commandBar, false)

	this.resetButton = ui.NewButton("|<")
	this.resetButton.OnClicked(this.onResetClicked)
	commandBar.Append(this.resetButton, false)

	this.pauseButton = ui.NewButton("||")
	this.pauseButton.OnClicked(this.onPauseClicked)
	commandBar.Append(this.pauseButton, false)

	this.startButton = ui.NewButton(">")
	this.startButton.OnClicked(this.onStartClicked)
	commandBar.Append(this.startButton, false)

	stretcher := ui.NewVerticalBox()
	commandBar.Append(stretcher, true)

	this.simulationTimeDisplay = gui.NewVCenteredLabel("")
	commandBar.Append(this.simulationTimeDisplay, false)

	mainArea := ui.NewHorizontalBox()
	mainArea.SetPadded(true)
	mainLayout.Append(mainArea, true)

	networkDisplay := NewNetworkArea()
	mainArea.Append(networkDisplay, true)

	this.measurementTable = gui.NewTextTable(2, 2)
	this.measurementTable.SetText(0, 0, "MEASUREMENT")
	this.measurementTable.SetText(1, 0, "VALUE")
	this.measurementTable.SetText(0, 1, "Query duration")
	mainArea.Append(this.measurementTable, false)

	this.SetChild(mainLayout)
	this.OnClosing(func(*ui.Window) bool {
		this.controller.Quit()
		return true
	})

	this.controller.SetUi(this)
}

func setEnabled(control ui.Control, enabled bool) {
	if enabled {
		control.Enable()
	} else {
		control.Disable()
	}
}
