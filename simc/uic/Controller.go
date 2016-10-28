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
	"github.com/straightway/straightway/general/times"
	gsui "github.com/straightway/straightway/general/ui"
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/sim/measure"
	"github.com/straightway/straightway/simc/ui"
)

type Controller struct {
	simulationController      sim.Controller
	ui                        ui.SimulationUi
	timeProvider              times.Provider
	toolkitAdapter            gsui.ToolkitAdapter
	measurementProvider       measure.Provider
	measurementDisplayCounter int
	MeasurementUpdateRatio    int
}

func NewController(
	timeProvider times.Provider,
	simulationController sim.Controller,
	measurementProvider measure.Provider,
	tookitAdapter gsui.ToolkitAdapter) *Controller {

	result := &Controller{
		simulationController: simulationController,
		timeProvider:         timeProvider,
		measurementProvider:  measurementProvider,
		toolkitAdapter:       tookitAdapter}
	simulationController.RegisterForExecEvent(result.onExecEvent)
	return result
}

func (this *Controller) SetUi(ui ui.SimulationUi) {
	this.ui = ui
	this.simulationController.Stop()
	this.simulationController.Reset()
	ui.SetStartEnabled(true)
	ui.SetPauseEnabled(false)
	ui.SetResetEnabled(false)
	ui.SetSimulationTime(this.timeProvider.Time())
}

func (this *Controller) Start() {
	this.ui.SetStartEnabled(false)
	this.ui.SetPauseEnabled(true)
	this.ui.SetResetEnabled(false)
	this.simulationController.Resume()
	this.simulationController.Run()
}

func (this *Controller) Reset() {
	this.ui.SetStartEnabled(true)
	this.ui.SetPauseEnabled(false)
	this.ui.SetResetEnabled(false)
	this.simulationController.Reset()
	this.ui.SetSimulationTime(this.timeProvider.Time())
}

func (this *Controller) Pause() {
	this.ui.SetStartEnabled(true)
	this.ui.SetPauseEnabled(false)
	this.ui.SetResetEnabled(true)
	this.simulationController.Stop()
}

func (this *Controller) Quit() {
	this.simulationController.Stop()
	this.toolkitAdapter.Quit()
}

// Private

func (this *Controller) onExecEvent() {
	if this.measurementDisplayCounter < this.MeasurementUpdateRatio {
		this.measurementDisplayCounter++
		return
	}
	this.measurementDisplayCounter = 0

	this.ui.SetSimulationTime(this.timeProvider.Time())
	measurements := this.measurementProvider.Measurements()
	this.ui.SetQueryDurationMeasurementValue(measurements["QueryDuration"])
	this.ui.SetQuerySuccessMeasurementValue(measurements["QuerySuccess"])
}
