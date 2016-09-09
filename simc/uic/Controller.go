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
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/simc/ui"
)

type Controller struct {
	simulationController sim.Controller
	ui                   ui.SimulationUi
	timeProvider         times.Provider
}

func NewController(
	timeProvider times.Provider,
	simulationController sim.Controller) ui.Controller {
	result := &Controller{
		simulationController: simulationController,
		timeProvider:         timeProvider}
	simulationController.RegisterForExecEvent(result.onExecEvent)
	return result
}

func (this *Controller) onExecEvent() {
	this.ui.SetSimulationTime(this.timeProvider.Time())
}

func (this *Controller) SetUi(ui ui.SimulationUi) {
	this.ui = ui
	this.simulationController.Stop()
	this.simulationController.Reset()
	ui.SetStartEnabled(true)
	ui.SetPauseEnabled(false)
	ui.SetStopEnabled(false)
	ui.SetSimulationTime(this.timeProvider.Time())
}

func (this *Controller) Start() {
	this.ui.SetStartEnabled(false)
	this.ui.SetPauseEnabled(true)
	this.ui.SetStopEnabled(true)
	this.simulationController.Run()
}

func (this *Controller) Stop() {
	this.ui.SetStartEnabled(true)
	this.ui.SetPauseEnabled(false)
	this.ui.SetStopEnabled(false)
	this.simulationController.Stop()
	this.simulationController.Reset()
}

func (this *Controller) Pause() {
	this.ui.SetStartEnabled(true)
	this.ui.SetPauseEnabled(false)
	this.ui.SetStopEnabled(true)
	this.simulationController.Stop()
	this.simulationController.Resume()
}
