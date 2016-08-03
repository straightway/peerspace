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

	"github.com/straightway/straightway/simc"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/suite"
)

// Test suite

type SimulationStateStorage_Test struct {
	suite.Suite
	connectors []peer.Connector
	sut        *simc.StateStorage
}

func TestSimulationStateStorage(t *testing.T) {
	suite.Run(t, new(SimulationStateStorage_Test))
}

func (suite *SimulationStateStorage_Test) SetupTest() {
	suite.connectors = []peer.Connector{
		mocked.CreatePeerConnector(),
		mocked.CreatePeerConnector()}
	suite.sut = &simc.StateStorage{
		Connectors: suite.connectors}
}

func (suite *SimulationStateStorage_Test) TearDownTest() {
	suite.connectors = nil
	suite.sut = nil
}

// Tests

func (suite *SimulationStateStorage_Test) Test_GetAllKnownPeers() {
	suite.Assert().Equal(suite.connectors, suite.sut.GetAllKnownPeers())
}

func (suite *SimulationStateStorage_Test) Test_IsKnownPeer_ContainedPeer() {
	suite.Assert().True(suite.sut.IsKnownPeer(suite.connectors[0]))
}

func (suite *SimulationStateStorage_Test) Test_IsKnownPeer_NotContainedPeer() {
	notContainedPeer := mocked.CreatePeerConnector()
	suite.Assert().False(suite.sut.IsKnownPeer(notContainedPeer))
}

func (suite *SimulationStateStorage_Test) Test_AddKnownPeer_NewPeer() {
	notContainedPeer := mocked.CreatePeerConnector()
	suite.sut.AddKnownPeer(notContainedPeer)
	suite.Assert().True(suite.sut.IsKnownPeer(notContainedPeer))
}

func (suite *SimulationStateStorage_Test) Test_AddKnownPeer_AlreadyContainedPeer() {
	suite.sut.AddKnownPeer(suite.connectors[0])
	suite.Assert().Equal(suite.connectors, suite.sut.GetAllKnownPeers())
}
