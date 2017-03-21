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

	"github.com/apex/log"
	"github.com/apex/log/handlers/discard"

	"github.com/andlabs/ui"

	"github.com/straightway/straightway/general/gui"
	"github.com/straightway/straightway/general/times"
	simlog "github.com/straightway/straightway/simc/log"
	"github.com/straightway/straightway/simc/measure"
	"github.com/straightway/straightway/simc/profiler"
	sui "github.com/straightway/straightway/simc/ui"
)

type MainWindow struct {
	*ui.Window
	controller            sui.Controller
	startButton           *ui.Button
	resetButton           *ui.Button
	pauseButton           *ui.Button
	displayNetworkButton  *ui.Checkbox
	simulationTimeDisplay *gui.VCenteredLabel
	measurementTable      *gui.TextTable
	networkModel          sui.NetworkModel
	networkDisplay        *ui.Area
	noNetworkPlaceholder  *ui.Label
	timeProvider          times.Provider
}

func NewMainWindow(
	controller sui.Controller,
	networkModel sui.NetworkModel,
	timeProvider times.Provider) *MainWindow {
	mainWindow := &MainWindow{
		controller:   controller,
		networkModel: networkModel,
		timeProvider: timeProvider}
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

func (this *MainWindow) SetQuerySuccessMeasurementValue(newValue string) {
	this.measurementTable.SetText(1, 2, newValue)
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

func (this *MainWindow) onDisplayNetworkToggled(checkbox *ui.Checkbox) {
	if checkbox.Checked() {
		this.networkDisplay.Show()
		this.noNetworkPlaceholder.Hide()
		ui.QueueMain(this.redrawNetworkDisplay)
	} else {
		this.networkDisplay.Hide()
		this.noNetworkPlaceholder.Show()
	}
}

func (this *MainWindow) onLoggingToggled(checkbox *ui.Checkbox) {
	simlog.SetEnabled(checkbox.Checked())
	if checkbox.Checked() {
		actionLogger := simlog.NewActionHandler(simlog.DefaultBasicHandler)
		simTimeLogHandler := simlog.NewSimulationTimeHandler(actionLogger, this.timeProvider)
		log.SetHandler(simTimeLogHandler)
	} else {
		log.SetHandler(discard.New())
	}
}

func (this *MainWindow) onSamplingToggled(checkbox *ui.Checkbox) {
	measure.SetGatedSamplingEnabled(checkbox.Checked())
}

func (this *MainWindow) onProfilingToggled(checkbox *ui.Checkbox) {
	if checkbox.Checked() {
		profiler.Start()
	} else {
		profiler.Stop()
	}

}

// Private

func (this *MainWindow) init() {
	this.Window = ui.NewWindow("Straightway Simulation", 800, 600, false)

	mainLayout := ui.NewVerticalBox()

	commandBar := ui.NewHorizontalBox()
	commandBar.SetPadded(true)
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

	this.displayNetworkButton = ui.NewCheckbox("Show Network")
	this.displayNetworkButton.OnToggled(this.onDisplayNetworkToggled)
	commandBar.Append(this.displayNetworkButton, false)

	logCheckbox := ui.NewCheckbox("Log")
	logCheckbox.OnToggled(this.onLoggingToggled)
	this.onLoggingToggled(logCheckbox)
	commandBar.Append(logCheckbox, false)

	profileCheckbox := ui.NewCheckbox("Profile")
	profileCheckbox.OnToggled(this.onProfilingToggled)
	this.onProfilingToggled(profileCheckbox)
	commandBar.Append(profileCheckbox, false)

	sampleCheckbox := ui.NewCheckbox("Sample")
	sampleCheckbox.SetChecked(true)
	sampleCheckbox.OnToggled(this.onSamplingToggled)
	this.onSamplingToggled(sampleCheckbox)
	commandBar.Append(sampleCheckbox, false)

	stretcher := ui.NewVerticalBox()
	commandBar.Append(stretcher, true)

	this.simulationTimeDisplay = gui.NewVCenteredLabel("")
	commandBar.Append(this.simulationTimeDisplay, false)

	mainArea := ui.NewHorizontalBox()
	mainArea.SetPadded(true)
	mainLayout.Append(mainArea, true)

	handler := &NetworkModelAreaHandler{
		Model:     this.networkModel,
		NodeSize:  10,
		NodeColor: ui.Brush{R: 0.0, B: 0.0, G: 1.0, A: 0.5}}
	this.networkDisplay = ui.NewArea(handler)
	mainArea.Append(this.networkDisplay, true)
	this.noNetworkPlaceholder = ui.NewLabel("Network display disabled")
	mainArea.Append(this.noNetworkPlaceholder, true)
	this.networkDisplay.Hide()

	this.measurementTable = gui.NewTextTable(2, 3)
	this.measurementTable.SetText(0, 0, "MEASUREMENT")
	this.measurementTable.SetText(1, 0, "VALUE")
	this.measurementTable.SetText(0, 1, "Query duration")
	this.measurementTable.SetText(0, 2, "Query success")
	mainArea.Append(this.measurementTable, false)

	this.SetChild(mainLayout)
	this.OnClosing(func(*ui.Window) bool {
		this.networkModel = nil
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

func (this *MainWindow) redrawNetworkDisplay() {
	if this.networkModel == nil || this.displayNetworkButton.Checked() == false {
		return
	}

	ui.QueueMain(this.redrawNetworkDisplay)
	this.networkDisplay.QueueRedrawAll()
}
