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
	"github.com/straightway/straightway/general/ui"
	"github.com/straightway/straightway/sim"
)

type SimulationControllerAdapter struct {
	SimulationController sim.SteppableController
	ToolkitAdapter       ui.ToolkitAdapter
	TimeProvider         times.Provider
	EnvironmentFactory   func() interface{}
	environment          interface{}
}

func (this *SimulationControllerAdapter) Run() {
	if this.environment == nil {
		this.environment = this.EnvironmentFactory()
	}
	this.ToolkitAdapter.Enqueue(this.execNextStep)
}

func (this *SimulationControllerAdapter) Stop() {
	this.SimulationController.Stop()
}

func (this *SimulationControllerAdapter) Resume() {
	this.SimulationController.Resume()
}

func (this *SimulationControllerAdapter) Reset() {
	this.environment = nil
	this.SimulationController.Reset()
}

func (this SimulationControllerAdapter) RegisterForExecEvent(callback func()) {
	this.SimulationController.RegisterForExecEvent(callback)
}

// Private

func (this *SimulationControllerAdapter) execNextStep() {
	if this.SimulationController.ExecNext() {
		this.ToolkitAdapter.Enqueue(this.execNextStep)
	}
}
