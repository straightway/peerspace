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
	"github.com/straightway/straightway/simc/uic"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type SimulationControllerAdapterTest struct {
	suite.Suite
	sut                  *uic.SimulationControllerAdapter
	simulationController *mocked.SimulationSteppableController
	uiToolkitAdapter     *mocked.UiToolkitAdapter
	timeProvider         *mocked.Timer
}

func TestSimulationControllerAdapter(t *testing.T) {
	suite.Run(t, new(SimulationControllerAdapterTest))
}

func (suite *SimulationControllerAdapterTest) SetupTest() {
	suite.uiToolkitAdapter = mocked.NewUiToolkitAdapter()
	suite.simulationController = mocked.NewSimulationSteppableController()
	suite.timeProvider = &mocked.Timer{CurrentTime: time.Unix(123456, 0).In(time.UTC)}
	suite.sut = &uic.SimulationControllerAdapter{
		SimulationController: suite.simulationController,
		ToolkitAdapter:       suite.uiToolkitAdapter,
		TimeProvider:         suite.timeProvider}
}

func (suite *SimulationControllerAdapterTest) TearDownTest() {
	suite.sut = nil
	suite.simulationController = nil
	suite.uiToolkitAdapter = nil
}

// Tests

func (suite *SimulationControllerAdapterTest) TestRunEnqueuesActionForNextStep() {
	suite.sut.Run()
	suite.uiToolkitAdapter.AssertCalledOnce(suite.T(), "Enqueue", mock.Anything)
}

func (suite *SimulationControllerAdapterTest) TestEnqueuedActionExecutesNextSimulationEvent() {
	suite.sut.Run()
	suite.uiToolkitAdapter.LastAction()
	suite.simulationController.AssertCalledOnce(suite.T(), "ExecNext")
}

func (suite *SimulationControllerAdapterTest) TestEnqueuedActionNotifiesTimeUpdater() {
	suite.sut.Run()
	suite.uiToolkitAdapter.LastAction()
	// TODO	suite.timeUpdater.AssertCalledOnce(suite.T(), "SetCurrentTime", suite.timeProvider.CurrentTime)
}

func (suite *SimulationControllerAdapterTest) TestEnqueuedActionEnqueuesNextActionIfNextSimulationEventExists() {
	suite.sut.Run()
	suite.uiToolkitAdapter.Calls = nil
	suite.uiToolkitAdapter.LastAction()
	suite.uiToolkitAdapter.AssertCalledOnce(suite.T(), "Enqueue", mock.Anything)
}

func (suite *SimulationControllerAdapterTest) TestEnqueuedActionEnqueuesNoActionIfNoSimulationEventExists() {
	suite.sut.Run()
	suite.simulationController.OnNew("ExecNext", mock.Anything).Return(false)
	suite.uiToolkitAdapter.Calls = nil
	suite.uiToolkitAdapter.LastAction()
	suite.uiToolkitAdapter.AssertNotCalled(suite.T(), "Enqueue", mock.Anything)
}

func (suite *SimulationControllerAdapterTest) TestStopStopsController() {
	suite.sut.Stop()
	suite.simulationController.AssertCalledOnce(suite.T(), "Stop")
}

func (suite *SimulationControllerAdapterTest) TestResumeResumesController() {
	suite.sut.Resume()
	suite.simulationController.AssertCalledOnce(suite.T(), "Resume")
}

func (suite *SimulationControllerAdapterTest) TestResetResetsController() {
	suite.sut.Reset()
	suite.simulationController.AssertCalledOnce(suite.T(), "Reset")
}

func (suite *SimulationControllerAdapterTest) TestRegisterForExecEventIsForwardedToController() {
	callbackExecutions := 0
	suite.sut.RegisterForExecEvent(func() { callbackExecutions++ })
	suite.simulationController.AssertCalledOnce(suite.T(), "RegisterForExecEvent")
	suite.Assert().Equal(1, len(suite.simulationController.ExecEventHandlers))
	suite.simulationController.ExecEventHandlers[0]()
	suite.Assert().Equal(1, callbackExecutions)
}
