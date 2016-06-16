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
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Node_General_Test struct {
	suite.Suite
	*NodeContext
}

func TestNodeGeneral(t *testing.T) {
	suite.Run(t, new(Node_General_Test))
}

func (suite *Node_General_Test) SetupTest() {
	suite.NodeContext = NewNodeContext()
	suite.SetUp()
}

func (suite *Node_General_Test) TearDownTest() {
	suite.ShutDownNode()
	suite.NodeContext = nil
}

func (suite *Node_General_Test) Test_Id_IsSameAsIdentifier() {
	suite.node.Identifier = "id"
	assert.Equal(suite.T(), suite.node.Identifier, suite.node.Id())
}

func (suite *Node_General_Test) Test_Equal_WithSameID() {
	peerNode := mocked.CreatePeerConnector()
	peerNode.Identifier = "1"
	suite.node.Identifier = "1"
	assert.True(suite.T(), suite.node.Equal(peerNode))
}

func (suite *Node_General_Test) Test_Equal_NotWithDifferentID() {
	peerNode := mocked.CreatePeerConnector()
	peerNode.Identifier = "1"
	suite.node.Identifier = "2"
	assert.False(suite.T(), suite.node.Equal(peerNode))
}
