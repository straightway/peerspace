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
package straightway

import (
	"testing"

	"github.com/straightway/straightway/core"
	"github.com/straightway/straightway/mock"
	tmock "github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type PeerNodeSendingTest struct {
	suite.Suite
	sut          *core.PeerNode
	peers        []*mock.MockedPeer
	stateStorage *mock.MockedStateStorage
}

func TestPeerNodeSending(t *testing.T) {
	suite.Run(t, new(PeerNodeSendingTest))
}

func (suite *PeerNodeSendingTest) SetupTest() {
	suite.createSut()
	suite.peers = []*mock.MockedPeer{createMockedPeer()}
	peerInterfaces := make([]core.Peer, len(suite.peers))
	for i, peer := range suite.peers {
		peerInterfaces[i] = peer
	}

	suite.stateStorage.On("GetAllKnownPeers").Return(peerInterfaces)
}

func (suite *PeerNodeSendingTest) TearDownTest() {
	suite.peers = nil
}

// Send data

func (suite *PeerNodeSendingTest) Test_PushedData_IsForwarded() {
	data := core.Data{0x2, 0x3, 0x5, 0x7, 0x11}
	suite.sut.Push(data)
	targetPeer := suite.peers[0]
	targetPeer.AssertNumberOfCalls(suite.T(), "Push", 1)
	targetPeer.AssertCalled(suite.T(), "Push", data)
}

// Private

func (suite *PeerNodeSendingTest) createSut() {
	suite.sut = new(core.PeerNode)
	suite.stateStorage = new(mock.MockedStateStorage)
	suite.sut.SetStateStorage(suite.stateStorage)
}

func createMockedPeer() *mock.MockedPeer {
	mockedPeer := new(mock.MockedPeer)
	mockedPeer.On("Push", tmock.AnythingOfTypeArgument("core.Data"))
	return mockedPeer
}
