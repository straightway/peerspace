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
	"math"
	"testing"

	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simc/ui"
	"github.com/straightway/straightway/simc/uic"
	"github.com/stretchr/testify/suite"
)

// Test suite

type SimulationUiNetworkLayouterTest struct {
	suite.Suite
	sut          *uic.NetworkLayouter
	nodeSelector *mocked.SimulationRandVarIntner
	network      *mocked.SimulationUiNetworkModel
	nodes        []*mocked.SimulationUiNodeModel
}

// 1.0 / (nodeSquareDistance + 1.0)
// For distancee = 1, also squareDistance = 1
const antiGravityCoefficientForDistance1 = 1.0 / 2.0

func TestSimulationUiNetworkLayouter(t *testing.T) {
	suite.Run(t, new(SimulationUiNetworkLayouterTest))
}

func (suite *SimulationUiNetworkLayouterTest) SetupTest() {
	suite.nodeSelector = mocked.NewSimulationRandVarIntner(0)
	suite.sut = &uic.NetworkLayouter{
		NodeSelector:     suite.nodeSelector,
		ConnectionForce:  2.0,
		AnitGravityForce: 0.5,
		MinX:             -100.0,
		MinY:             -100.0,
		MaxX:             100.0,
		MaxY:             100.0,
		TimeFactor:       1.0}
}

func (suite *SimulationUiNetworkLayouterTest) TearDownTest() {
	suite.sut = nil
	suite.nodeSelector = nil
	suite.network = nil
	suite.nodes = nil
}

// Tests

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_AffectsRandomNode() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 1.0, 1.0, 0.0, 2.0)
	suite.nodeSelector.SetValues(1)
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, 0.0)
	suite.Assert().False(suite.isNodePosition(1, 1.0, 1.0))
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_NoNodesNotChanged() {
	suite.createNetworkWithNodeCoordinates(0)
	suite.Assert().NotPanics(func() { suite.sut.ImproveLayoutOf(suite.network) })
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_SingleNodesNotChanged() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0)
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, 0.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_TwoUnconnectedNodesAtSamePosJumpsToRandomPosition() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 0.0, 0.0)
	suite.nodeSelector.SetValues(0, 12, 13)
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, -88.0, -87.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_TwoUnconnectedNodesDriftByAntiGravityInYDirection() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 0.0, 1.0)
	moveY := suite.antiGravityForceForDistance1()
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, moveY)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_TwoUnconnectedNodesDriftByAntiGravityInXDirection() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 1.0, 0.0)
	moveX := suite.antiGravityForceForDistance1()
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, moveX, 0.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_TwoUnconnectedNodesDriftByAntiGravityInBothDirections() {
	dxy := math.Sqrt(0.5)
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, dxy, dxy)
	moveXY := dxy * suite.antiGravityForceForDistance1()
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, moveXY, moveXY)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_ThreeUnconnectedNodesDriftByAntiGravityInXDirection() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 0.0, 1.0, 0.0, 1.0)
	moveY := 2 * suite.antiGravityForceForDistance1()
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, moveY)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_TwoConnectedNodesInYDirection() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 0.0, 1.0)
	suite.nodes[0].ConnectTo(suite.nodes[1])
	moveY := suite.antiGravityForceForDistance1() + suite.sut.ConnectionForce
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, moveY)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_TwoConnectedNodesXDirection() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 1.0, 0.0)
	suite.nodes[0].ConnectTo(suite.nodes[1])
	moveX := suite.antiGravityForceForDistance1() + suite.sut.ConnectionForce
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, moveX, 0.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_TwoConnectedNodesBothDirections() {
	dxy := math.Sqrt(0.5)
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, dxy, dxy)
	suite.nodes[0].ConnectTo(suite.nodes[1])
	moveXY := dxy*suite.antiGravityForceForDistance1() + dxy*suite.sut.ConnectionForce
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, moveXY, moveXY)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_ThreeConnectedNodesXDirection() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 0.0, 1.0, 0.0, 1.0)
	suite.nodes[0].ConnectTo(suite.nodes[1])
	suite.nodes[0].ConnectTo(suite.nodes[2])
	moveY := 2*suite.antiGravityForceForDistance1() + 2*suite.sut.ConnectionForce
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, moveY)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_RespectMinY() {
	suite.sut.MinY = 0.0
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 0.0, 1.0)
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, 0.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_RespectMaxY() {
	suite.sut.MaxY = -10.0
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 0.0, 1.0)
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, -10.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_RespectMinX() {
	suite.sut.MinX = 0.0
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 1.0, 0.0)
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, 0.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_RespectMaxX() {
	suite.sut.MaxX = -10.0
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 1.0, 0.0)
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, -10.0, 0.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_ImproveLayout_IsInfluencedByTimeFactor() {
	suite.sut.TimeFactor = 2.0
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 0.0, 1.0)
	moveY := suite.antiGravityForceForDistance1()
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 0.0, moveY*2.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_SetBounds_SetsMinMaxXY() {
	suite.sut.SetBounds(1.0, 2.0, 3.0, 4.0)
	suite.Assert().Equal(suite.sut.MinX, 1.0)
	suite.Assert().Equal(suite.sut.MinY, 2.0)
	suite.Assert().Equal(suite.sut.MaxX, 3.0)
	suite.Assert().Equal(suite.sut.MaxY, 4.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_SetBounds_InvalidBoundsDoNothing() {
	suite.sut.SetBounds(0.0, 0.0, 0.0, 0.0)
	suite.createNetworkWithNodeCoordinates(1.0, 2.0, 3.0, 4.0)
	suite.sut.ImproveLayoutOf(suite.network)
	suite.assertNodePosition(0, 1.0, 2.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_Centralize_MovesSingleNodeToCenter() {
	suite.createNetworkWithNodeCoordinates(1.0, 0.0)
	suite.sut.Centralize(suite.network)
	suite.assertNodePosition(0, 0.0, 0.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_Centralize_MovesSingleNodeToCenterIfNotInOrigin() {
	suite.sut.SetBounds(10.0, 10.0, 20.0, 20.0)
	suite.createNetworkWithNodeCoordinates(1.0, 0.0)
	suite.sut.Centralize(suite.network)
	suite.assertNodePosition(0, 15.0, 15.0)
}

func (suite *SimulationUiNetworkLayouterTest) Test_Centralize_MovesTwoNodesToCenterOfMass() {
	suite.createNetworkWithNodeCoordinates(0.0, 0.0, 2.0, 2.0)
	suite.sut.Centralize(suite.network)
	suite.assertNodePosition(0, -1.0, -1.0)
	suite.assertNodePosition(1, 1.0, 1.0)
}

// Private

func (suite *SimulationUiNetworkLayouterTest) createNetworkWithNodeCoordinates(nodePositions ...float64) {
	numberOfNodes := len(nodePositions) / 2
	nodes := make([]ui.NodeModel, numberOfNodes)
	suite.nodes = make([]*mocked.SimulationUiNodeModel, numberOfNodes)
	for i := range nodes {
		x, y := float64(i), float64(i)
		if len(nodePositions) == 1 {
			panic("Uneven number of node coordinates")
		}
		if 2 <= len(nodePositions) {
			x, y = nodePositions[0], nodePositions[1]
			nodePositions = nodePositions[2:]
		}

		suite.nodes[i] = mocked.NewSimulationUiNodeModel(x, y)
		nodes[i] = suite.nodes[i]
	}

	suite.network = mocked.NewSimulationUiNetworkModel(nodes...)
	suite.nodeSelector.SetValues(0)
}

func (suite *SimulationUiNetworkLayouterTest) assertNodePosition(index int, x, y float64) {
	nodeX, nodeY := suite.nodes[index].Position()
	suite.Assert().True(
		suite.isNodePosition(index, x, y),
		"Expected node position: %v,%v, actual: %v,%v",
		x, y,
		nodeX, nodeY)
}

func (suite *SimulationUiNetworkLayouterTest) isNodePosition(index int, x, y float64) bool {
	nodeX, nodeY := suite.nodes[index].Position()
	return nodeX == x && nodeY == y
}

func (this *SimulationUiNetworkLayouterTest) antiGravityForceForDistance1() float64 {
	return -this.sut.AnitGravityForce * antiGravityCoefficientForDistance1
}
