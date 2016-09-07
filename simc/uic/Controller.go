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
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/simc/ui"
)

type Controller struct {
	SimulationController sim.Controller
	ui                   ui.SimulationUi
}

func (this *Controller) SetUi(ui ui.SimulationUi) {
	this.ui = ui
	this.SimulationController.Stop()
	this.SimulationController.Reset()
	ui.SetStartEnabled(true)
	ui.SetPauseEnabled(false)
	ui.SetStopEnabled(false)
}

func (this *Controller) Start() {
	this.ui.SetStartEnabled(false)
	this.ui.SetPauseEnabled(true)
	this.ui.SetStopEnabled(true)
	this.SimulationController.Run()
}

func (this *Controller) Stop() {
	this.ui.SetStartEnabled(true)
	this.ui.SetPauseEnabled(false)
	this.ui.SetStopEnabled(false)
	this.SimulationController.Stop()
	this.SimulationController.Reset()
}

func (this *Controller) Pause() {
	this.ui.SetStartEnabled(true)
	this.ui.SetPauseEnabled(false)
	this.ui.SetStopEnabled(true)
	this.SimulationController.Stop()
	this.SimulationController.Resume()
}
