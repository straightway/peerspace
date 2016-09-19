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
	"github.com/straightway/straightway/simc"
	measurec "github.com/straightway/straightway/simc/measure"
	"github.com/straightway/straightway/simc/uic"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type SimulationControllerAdapterTest struct {
	suite.Suite
	sut                  *uic.SimulationControllerAdapter
	simulationController *mocked.SimulationSteppableController
	simulationScheduler  *mocked.SimulationScheduler
	uiToolkitAdapter     *mocked.UiToolkitAdapter
	environmentFactory   func() *simc.Environment
}

func TestSimulationControllerAdapter(t *testing.T) {
	suite.Run(t, new(SimulationControllerAdapterTest))
}

func (suite *SimulationControllerAdapterTest) SetupTest() {
	suite.uiToolkitAdapter = mocked.NewUiToolkitAdapter()
	suite.simulationController = mocked.NewSimulationSteppableController()
	suite.simulationScheduler = mocked.NewSimulationScheduler()
	suite.simulationScheduler.CurrentTime = time.Unix(123456, 0).In(time.UTC)
	suite.sut = &uic.SimulationControllerAdapter{
		SimulationController: suite.simulationController,
		ToolkitAdapter:       suite.uiToolkitAdapter,
		TimeProvider:         suite.simulationScheduler,
		EnvironmentFactory:   func() *simc.Environment { return suite.environmentFactory() }}
	suite.environmentFactory = func() *simc.Environment {
		return simc.NewSimulationEnvironment(suite.simulationScheduler, 1)
	}
}

func (suite *SimulationControllerAdapterTest) TearDownTest() {
	suite.sut = nil
	suite.simulationController = nil
	suite.uiToolkitAdapter = nil
}

// Tests

func (suite *SimulationControllerAdapterTest) Test_Run_EnqueuesActionForNextStep() {
	suite.sut.Run()
	suite.uiToolkitAdapter.AssertCalledOnce(suite.T(), "Enqueue", mock.Anything)
}

func (suite *SimulationControllerAdapterTest) Test_EnqueuedAction_ExecutesNextSimulationEvent() {
	suite.sut.Run()
	suite.uiToolkitAdapter.LastAction()
	suite.simulationController.AssertCalledOnce(suite.T(), "ExecNext")
}

func (suite *SimulationControllerAdapterTest) Test_EnqueuedAction_EnqueuesNextActionIfNextSimulationEventExists() {
	suite.sut.Run()
	suite.uiToolkitAdapter.Calls = nil
	suite.uiToolkitAdapter.LastAction()
	suite.uiToolkitAdapter.AssertCalledOnce(suite.T(), "Enqueue", mock.Anything)
}

func (suite *SimulationControllerAdapterTest) Test_EnqueuedAction_EnqueuesNoActionIfNoSimulationEventExists() {
	suite.sut.Run()
	suite.simulationController.OnNew("ExecNext", mock.Anything).Return(false)
	suite.uiToolkitAdapter.Calls = nil
	suite.uiToolkitAdapter.LastAction()
	suite.uiToolkitAdapter.AssertNotCalled(suite.T(), "Enqueue", mock.Anything)
}

func (suite *SimulationControllerAdapterTest) Test_Stop_StopsController() {
	suite.sut.Stop()
	suite.simulationController.AssertCalledOnce(suite.T(), "Stop")
}

func (suite *SimulationControllerAdapterTest) Test_Resume_ResumesController() {
	suite.sut.Resume()
	suite.simulationController.AssertCalledOnce(suite.T(), "Resume")
}

func (suite *SimulationControllerAdapterTest) TestResetResetsController() {
	suite.sut.Reset()
	suite.simulationController.AssertCalledOnce(suite.T(), "Reset")
}

func (suite *SimulationControllerAdapterTest) Test_RegisterForExecEvent_IsForwardedToController() {
	callbackExecutions := 0
	suite.sut.RegisterForExecEvent(func() { callbackExecutions++ })
	suite.simulationController.AssertCalledOnce(suite.T(), "RegisterForExecEvent")
	suite.Assert().Equal(1, len(suite.simulationController.ExecEventHandlers))
	suite.simulationController.ExecEventHandlers[0]()
	suite.Assert().Equal(1, callbackExecutions)
}

func (suite *SimulationControllerAdapterTest) Test_FirstRunCall_CreatesEnvironment() {
	wasCalled := false
	suite.environmentFactory = func() *simc.Environment { wasCalled = true; return nil }
	suite.sut.Run()
	suite.Assert().True(wasCalled)
}

func (suite *SimulationControllerAdapterTest) Test_SecondRunCall_DoesNotCreateEnvironment() {
	suite.sut.Run()
	wasCalled := false
	suite.environmentFactory = func() *simc.Environment { wasCalled = true; return nil }
	suite.sut.Run()
	suite.Assert().False(wasCalled)
}

func (suite *SimulationControllerAdapterTest) Test_Run_Reset_Run_CreatesEnvironment() {
	suite.sut.Run()
	suite.sut.Reset()
	wasCalled := false
	suite.environmentFactory = func() *simc.Environment { wasCalled = true; return nil }
	suite.sut.Run()
	suite.Assert().True(wasCalled)
}

func (suite *SimulationControllerAdapterTest) Test_Measurements_ReturnsEmptyMapForNotStartedSimulation() {
	result := suite.sut.Measurements()
	suite.Assert().Empty(result)
}

func (suite *SimulationControllerAdapterTest) Test_Measurements_ReturnsMeasurementsStartedSimulation() {
	suite.sut.Run()
	measurements := suite.sut.Measurements()
	suite.Assert().Equal(2, len(measurements))
	suite.Assert().Equal((&measurec.Discrete{}).String(), measurements["QueryDuration"])
	suite.Assert().Equal((&measurec.Discrete{}).String(), measurements["QuerySuccess"])
}
