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
	"testing"
	"time"

	"github.com/straightway/straightway/general/ui"
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/simc"
	"github.com/straightway/straightway/simc/env"
	measurec "github.com/straightway/straightway/simc/measure"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type SimulationControllerAdapter_Test struct {
	suite.Suite
	sut                  *SimulationControllerAdapter
	simulationController *sim.SteppableControllerMock
	simulationScheduler  *sim.EventSchedulerMock
	uiToolkitAdapter     *ui.ToolkitAdapterMock
	environmentFactory   func() *env.Environment
}

func TestSimulationControllerAdapter(t *testing.T) {
	suite.Run(t, new(SimulationControllerAdapter_Test))
}

func (suite *SimulationControllerAdapter_Test) SetupTest() {
	suite.uiToolkitAdapter = ui.NewToolkitAdapterMock()
	suite.simulationController = sim.NewSteppableControllerMock()
	suite.simulationScheduler = sim.NewEventSchedulerMock()
	suite.simulationScheduler.CurrentTime = time.Unix(123456, 0).In(time.UTC)
	suite.sut = &SimulationControllerAdapter{
		SimulationController: suite.simulationController,
		ToolkitAdapter:       suite.uiToolkitAdapter,
		TimeProvider:         suite.simulationScheduler,
		EnvironmentFactory:   func() *env.Environment { return suite.environmentFactory() }}
	suite.environmentFactory = func() *env.Environment {
		return env.New(suite.simulationScheduler, 1)
	}
}

func (suite *SimulationControllerAdapter_Test) TearDownTest() {
	suite.sut = nil
	suite.simulationController = nil
	suite.uiToolkitAdapter = nil
}

// Tests

func (suite *SimulationControllerAdapter_Test) Test_Run_EnqueuesActionForNextStep() {
	suite.sut.Run()
	suite.uiToolkitAdapter.AssertCalledOnce(suite.T(), "Enqueue", mock.Anything)
}

func (suite *SimulationControllerAdapter_Test) Test_EnqueuedAction_ExecutesNextSimulationEvent() {
	suite.sut.Run()
	suite.uiToolkitAdapter.LastAction()
	suite.simulationController.AssertCalledOnce(suite.T(), "ExecNext")
}

func (suite *SimulationControllerAdapter_Test) Test_EnqueuedAction_EnqueuesNextActionIfNextSimulationEventExists() {
	suite.sut.Run()
	suite.uiToolkitAdapter.Calls = nil
	suite.uiToolkitAdapter.LastAction()
	suite.uiToolkitAdapter.AssertCalledOnce(suite.T(), "Enqueue", mock.Anything)
}

func (suite *SimulationControllerAdapter_Test) Test_EnqueuedAction_EnqueuesNoActionIfNoSimulationEventExists() {
	suite.sut.Run()
	suite.simulationController.OnNew("ExecNext", mock.Anything).Return(false)
	suite.uiToolkitAdapter.Calls = nil
	suite.uiToolkitAdapter.LastAction()
	suite.uiToolkitAdapter.AssertNotCalled(suite.T(), "Enqueue", mock.Anything)
}

func (suite *SimulationControllerAdapter_Test) Test_Stop_StopsController() {
	suite.sut.Stop()
	suite.simulationController.AssertCalledOnce(suite.T(), "Stop")
}

func (suite *SimulationControllerAdapter_Test) Test_Resume_ResumesController() {
	suite.sut.Resume()
	suite.simulationController.AssertCalledOnce(suite.T(), "Resume")
}

func (suite *SimulationControllerAdapter_Test) TestResetResetsController() {
	suite.sut.Reset()
	suite.simulationController.AssertCalledOnce(suite.T(), "Reset")
}

func (suite *SimulationControllerAdapter_Test) Test_RegisterForExecEvent_IsForwardedToController() {
	callbackExecutions := 0
	suite.sut.RegisterForExecEvent(func() { callbackExecutions++ })
	suite.simulationController.AssertCalledOnce(suite.T(), "RegisterForExecEvent")
	suite.Assert().Equal(1, len(suite.simulationController.ExecEventHandlers))
	suite.simulationController.ExecEventHandlers[0]()
	suite.Assert().Equal(1, callbackExecutions)
}

func (suite *SimulationControllerAdapter_Test) Test_FirstRunCall_CreatesEnvironment() {
	wasCalled := false
	suite.environmentFactory = func() *env.Environment { wasCalled = true; return nil }
	suite.sut.Run()
	suite.Assert().True(wasCalled)
}

func (suite *SimulationControllerAdapter_Test) Test_SecondRunCall_DoesNotCreateEnvironment() {
	suite.sut.Run()
	wasCalled := false
	suite.environmentFactory = func() *env.Environment { wasCalled = true; return nil }
	suite.sut.Run()
	suite.Assert().False(wasCalled)
}

func (suite *SimulationControllerAdapter_Test) Test_Run_Reset_Run_CreatesEnvironment() {
	suite.sut.Run()
	suite.sut.Reset()
	wasCalled := false
	suite.environmentFactory = func() *env.Environment { wasCalled = true; return nil }
	suite.sut.Run()
	suite.Assert().True(wasCalled)
}

func (suite *SimulationControllerAdapter_Test) Test_Measurements_ReturnsEmptyMapForNotStartedSimulation() {
	result := suite.sut.Measurements()
	suite.Assert().Empty(result)
}

func (suite *SimulationControllerAdapter_Test) Test_Measurements_ReturnsMeasurementsStartedSimulation() {
	suite.sut.Run()
	measurements := suite.sut.Measurements()
	suite.Assert().Equal(2, len(measurements))
	suite.Assert().Equal((&measurec.Discrete{}).String(), measurements["QueryDuration"])
	suite.Assert().Equal((&measurec.Discrete{}).String(), measurements["QuerySuccess"])
}

func (suite *SimulationControllerAdapter_Test) Test_Nodes_ReturnsNodesFromEnvironment() {
	environment := env.New(simc.NewEventScheduler(), 5)
	suite.environmentFactory = func() *env.Environment { return environment }
	suite.sut.Run()
	suite.Assert().Equal(environment.Nodes(), suite.sut.Nodes())
}

func (suite *SimulationControllerAdapter_Test) Test_Nodes_ReturnsNoNodesWithoutEnvironemnt() {
	suite.environmentFactory = func() *env.Environment { return nil }
	suite.Assert().Empty(suite.sut.Nodes())
}
