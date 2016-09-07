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

	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simc/uic"
	"github.com/stretchr/testify/suite"
)

type SimulationUiController_Test struct {
	suite.Suite
	sut                  *uic.Controller
	simulationController *mocked.SimulationController
	ui                   *mocked.SimulationUi
}

func TestSimulationUiController(t *testing.T) {
	suite.Run(t, new(SimulationUiController_Test))
}

func (suite *SimulationUiController_Test) SetupTest() {
	suite.simulationController = mocked.NewSimulationController()
	suite.ui = mocked.NewSimulationUi()
	suite.sut = &uic.Controller{
		SimulationController: suite.simulationController}
	suite.sut.SetUi(suite.ui)
	suite.ui.Calls = nil
	suite.simulationController.Calls = nil
}

func (suite *SimulationUiController_Test) TearDownTest() {
	suite.sut = nil
	suite.simulationController = nil
	suite.ui = nil
}

// Tests

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
