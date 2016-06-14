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

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/mocked"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Node_Query_Test struct {
	suite.Suite
	*NodeContext
}

func TestNodeQuery(t *testing.T) {
	suite.Run(t, new(Node_Query_Test))
}

func (suite *Node_Query_Test) SetupTest() {
	suite.NodeContext = NewNodeContext()
	suite.SetUp()
}

func (suite *Node_Query_Test) TearDownTest() {
	suite.ShutDownNode()
	suite.NodeContext = nil
}

func (suite *Node_Query_Test) Test_Query_LocallyStoredItemIsPushedToQueryNode() {
	queryPeer := mocked.CreatePeerConnector()
	queryKey := data.Key("1234")
	suite.dataStorage = mocked.NewDataStorage(queryKey, dataChunk)
	suite.createSut()
	suite.node.Query(queryKey, queryPeer)
	queryPeer.AssertNumberOfCalls(suite.T(), "Push", 1)
	queryPeer.AssertCalled(suite.T(), "Push", dataChunk)
}

func (suite *Node_Query_Test) Test_Query_NotLocallyStoredItemIsNotPushed() {
	queryPeer := mocked.CreatePeerConnector()
	queryKey := data.Key("1234")
	suite.node.Query(queryKey, queryPeer)
	queryPeer.AssertNumberOfCalls(suite.T(), "Push", 0)
}
