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

package simulation

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/simulation/randvar"
	"github.com/straightway/straightway/storage"
	"github.com/straightway/straightway/strategy"
)

const (
	gb = 1024 * 1024 * 1024
)

type Environment struct {
	Scheduler   EventScheduler
	users       []*User
	nextNodeId  uint
	randSource  rand.Source
	initialUser *User
}

func NewSimulationEnvironment(numberOfUsers int) *Environment {
	result := &Environment{
		randSource: rand.NewSource(12345)}
	result.createSeedNode()
	for i := 0; i < numberOfUsers; i++ {
		result.addNewUser()
	}
	return result
}

func (this *Environment) Users() []*User {
	return this.users
}

func (this *Environment) addNewUser() *User {
	newUser := this.createUser()
	this.users = append(this.users, newUser)
	return newUser
}

func (this *Environment) createSeedNode() {
	this.initialUser = &User{
		Scheduler:       &this.Scheduler,
		Node:            this.createNode(),
		StartupDuration: randvar.NewNormalDuration(this.randSource, time.Duration(0), time.Duration(0)),
		OnlineDuration:  randvar.NewNormalDuration(this.randSource, time.Duration(-1), time.Duration(0)),
		OnlineActivity:  mocked.NewSimulationUserActivity()}
	this.initialUser.Activate()
}

func (this *Environment) createUser() *User {
	newUser := &User{
		Scheduler:       &this.Scheduler,
		Node:            this.createNode(),
		StartupDuration: randvar.NewNormalDuration(this.randSource, general.ParseDuration("8h"), general.ParseDuration("2h")),
		OnlineDuration:  randvar.NewNormalDuration(this.randSource, general.ParseDuration("2h"), general.ParseDuration("2h")),
		OnlineActivity:  mocked.NewSimulationUserActivity()}
	newUser.Activate()
	return newUser
}

func (this *Environment) createNode() peer.Node {
	this.nextNodeId++
	configuration := peer.DefaultConfiguration()
	peerDistanceRelated := &strategy.PeerDistanceRelated{}
	stateStorage := this.createStateStorage()
	newNode := &peer.NodeImpl{
		Identifier:           fmt.Sprintf("%v", this.nextNodeId),
		StateStorage:         stateStorage,
		DataStorage:          this.createDataStorage(peerDistanceRelated),
		AnnouncementStrategy: this.createAnnouncementStrategy(configuration, stateStorage),
		DataStrategy:         this.createDataStrategy(configuration, peerDistanceRelated),
		Timer:                &this.Scheduler,
		Configuration:        configuration}
	newNode.ConnectionStrategy = this.createConnecionStrategy(configuration, newNode)
	return newNode
}

func (this *Environment) createStateStorage() peer.StateStorage {
	stateStorage := &StateStorage{}
	if 0 < len(this.users) {
		stateStorage.AddKnownPeer(this.initialUser.Node)
	}

	return stateStorage
}

func (this *Environment) createDataStorage(priorityGenerator storage.PriorityGenerator) peer.DataStorage {
	rawStorage := &RawStorage{
		FreeStorageValue: 2 * gb,
		Timer:            &this.Scheduler}
	return &storage.DataImpl{
		PriorityGenerator: priorityGenerator,
		RawStorage:        rawStorage}
}

func (this *Environment) createDataStrategy(
	configuration *peer.Configuration,
	peerDistanceCalculator strategy.PeerDistanceCalculator) peer.DataStrategy {
	return &strategy.Data{
		Configuration:          configuration,
		PeerDistanceCalculator: peerDistanceCalculator}
}

func (this *Environment) createAnnouncementStrategy(
	configuration *peer.Configuration,
	stateStorage peer.StateStorage) peer.AnnouncementStrategy {
	return &strategy.Announcement{
		Configuration: configuration,
		RandomSource:  this.randSource,
		StateStorage:  stateStorage}
}

func (this *Environment) createConnecionStrategy(
	configuration *peer.Configuration,
	connectionInfoProvider strategy.ConnectionInfoProvider) peer.ConnectionStrategy {
	return &strategy.Connection{
		Configuration:          configuration,
		ConnectionInfoProvider: connectionInfoProvider,
		RandSource:             this.randSource}
}
