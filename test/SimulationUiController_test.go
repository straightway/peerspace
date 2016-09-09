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

package test

import (
	"testing"
	"time"

	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simc/ui"
	"github.com/straightway/straightway/simc/uic"
	"github.com/stretchr/testify/suite"
)

type SimulationUiController_Test struct {
	suite.Suite
	sut                  ui.Controller
	simulationController *mocked.SimulationSteppableController
	ui                   *mocked.SimulationUi
	timeProvider         *mocked.Timer
}

func TestSimulationUiController(t *testing.T) {
	suite.Run(t, new(SimulationUiController_Test))
}

func (suite *SimulationUiController_Test) SetupTest() {
	suite.timeProvider = &mocked.Timer{CurrentTime: time.Unix(123456, 0).In(time.UTC)}
	suite.simulationController = mocked.NewSimulationSteppableController()
	suite.ui = mocked.NewSimulationUi()
	suite.sut = uic.NewController(suite.timeProvider, suite.simulationController)
	suite.sut.SetUi(suite.ui)
	suite.ui.Calls = nil
	suite.simulationController.Calls = nil
}

func (suite *SimulationUiController_Test) TearDownTest() {
	suite.sut = nil
	suite.simulationController = nil
	suite.ui = nil
	suite.timeProvider = nil
}

// Tests

func (suite *SimulationUiController_Test) TestConstructorConnectsToSimControllersExecEvent() {
	suite.Assert().NotEmpty(suite.simulationController.ExecEventHandlers)
}

func (suite *SimulationUiController_Test) TestSimControllersExecEventTriggerSimulationTimeUpdate() {
	suite.simulationController.ExecEventHandlers[0]()
	suite.ui.AssertCalledOnce(suite.T(), "SetSimulationTime", suite.timeProvider.Time())
}

func (suite *SimulationUiController_Test) Test_Start_StartsSimulation() {
	suite.sut.Start()
	suite.simulationController.AssertCalledOnce(suite.T(), "Run")
}

func (suite *SimulationUiController_Test) Test_Start_SetsButtonStates() {
	suite.sut.Start()
	suite.ui.AssertCalledOnce(suite.T(), "SetStartEnabled", false)
	suite.ui.AssertCalledOnce(suite.T(), "SetPauseEnabled", true)
	suite.ui.AssertCalledOnce(suite.T(), "SetStopEnabled", true)
}

func (suite *SimulationUiController_Test) Test_Stop_StopsAndResetsSimulation() {
	suite.sut.Stop()
	suite.simulationController.AssertCalledOnce(suite.T(), "Stop")
	suite.simulationController.AssertCalledOnce(suite.T(), "Reset")
}

func (suite *SimulationUiController_Test) Test_Stop_SetsButtonStates() {
	suite.sut.Stop()
	suite.ui.AssertCalledOnce(suite.T(), "SetStartEnabled", true)
	suite.ui.AssertCalledOnce(suite.T(), "SetPauseEnabled", false)
	suite.ui.AssertCalledOnce(suite.T(), "SetStopEnabled", false)
}

func (suite *SimulationUiController_Test) Test_Pause_StopsAndResumesSimulation() {
	suite.sut.Pause()
	suite.simulationController.AssertCalledOnce(suite.T(), "Stop")
	suite.simulationController.AssertCalledOnce(suite.T(), "Resume")
}

func (suite *SimulationUiController_Test) Test_Pause_SetsButtonStates() {
	suite.sut.Pause()
	suite.ui.AssertCalledOnce(suite.T(), "SetStartEnabled", true)
	suite.ui.AssertCalledOnce(suite.T(), "SetPauseEnabled", false)
	suite.ui.AssertCalledOnce(suite.T(), "SetStopEnabled", true)
}

func (suite *SimulationUiController_Test) Test_SetUi_StopsSimulation() {
	suite.sut.SetUi(suite.ui)
	suite.simulationController.AssertCalledOnce(suite.T(), "Stop")
	suite.simulationController.AssertCalledOnce(suite.T(), "Reset")
}

func (suite *SimulationUiController_Test) Test_SetUi_SetsInitialButtonStates() {
	suite.sut.SetUi(suite.ui)
	suite.ui.AssertCalledOnce(suite.T(), "SetStartEnabled", true)
	suite.ui.AssertCalledOnce(suite.T(), "SetPauseEnabled", false)
	suite.ui.AssertCalledOnce(suite.T(), "SetStopEnabled", false)
}

// TODO

func (suite *SimulationUiController_Test) Test_SetUi_SetsInitialSimulationTime() {
	suite.timeProvider.CurrentTime = time.Unix(123456, 0).In(time.UTC)
	suite.sut.SetUi(suite.ui)
	suite.ui.AssertCalledOnce(suite.T(), "SetSimulationTime", suite.timeProvider.CurrentTime)
}

func (suite *SimulationUiController_Test) Test_ResgisteredEventHandler_SetSimulationTimeInUi() {
	suite.timeProvider.CurrentTime = time.Unix(123456, 0).In(time.UTC)
	suite.simulationController.ExecEventHandlers[0]()
	suite.ui.AssertCalledOnce(suite.T(), "SetSimulationTime", suite.timeProvider.CurrentTime)
}
