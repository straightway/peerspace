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

package sim_

import (
	"fmt"
	"hash/crc64"
	"math/rand"
	"time"

	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/sim"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/sim_/randvar"
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

func (this *Environment) Audience() []sim.DataConsumer {
	result := make([]sim.DataConsumer, len(this.users), len(this.users))
	for i, u := range this.users {
		result[i] = u
	}
	return result
}

func (this *Environment) addNewUser() *User {
	newUser := this.createUser()
	this.users = append(this.users, newUser)
	return newUser
}

func (this *Environment) createSeedNode() {
	node, _, _ := this.createNode()
	this.initialUser = &User{
		Scheduler:       &this.Scheduler,
		Node:            node,
		StartupDuration: randvar.NewNormalDuration(this.randSource, time.Duration(0), time.Duration(0)),
		OnlineDuration:  randvar.NewNormalDuration(this.randSource, time.Duration(-1), time.Duration(0)),
		OnlineActivity:  mocked.NewSimulationUserActivity()}
	this.initialUser.Activate()
}

func (this *Environment) createUser() *User {
	node, configuration, rawStorage := this.createNode()
	newUser := &User{
		Scheduler:       &this.Scheduler,
		Node:            node,
		StartupDuration: randvar.NewNormalDuration(this.randSource, general.ParseDuration("8h"), general.ParseDuration("2h")),
		OnlineDuration:  randvar.NewNormalDuration(this.randSource, general.ParseDuration("2h"), general.ParseDuration("2h"))}
	newUser.OnlineActivity = this.createActivity(newUser, configuration, rawStorage)
	newUser.Activate()
	return newUser
}

func (this *Environment) createNode() (peer.Node, *peer.Configuration, *RawStorage) {
	this.nextNodeId++
	nodeId := fmt.Sprintf("%v", this.nextNodeId)
	configuration := peer.DefaultConfiguration()
	peerDistanceRelated := &strategy.PeerDistanceRelated{
		LocalPeerId: nodeId,
		Timer:       &this.Scheduler,
		Hasher:      crc64.New(crc64.MakeTable(crc64.ECMA))}
	stateStorage := this.createStateStorage()
	dataStorage, rawStorage := this.createDataStorage(peerDistanceRelated)
	newNode := &peer.NodeImpl{
		Identifier:           nodeId,
		StateStorage:         stateStorage,
		DataStorage:          dataStorage,
		AnnouncementStrategy: this.createAnnouncementStrategy(configuration, stateStorage),
		Timer:                &this.Scheduler,
		Configuration:        configuration}
	newNode.DataStrategy = this.createDataStrategy(configuration, peerDistanceRelated, newNode)
	newNode.ConnectionStrategy = this.createConnecionStrategy(configuration, newNode)
	return newNode, configuration, rawStorage
}

func (this *Environment) createStateStorage() peer.StateStorage {
	stateStorage := &StateStorage{}
	if 0 < len(this.users) {
		stateStorage.AddKnownPeer(this.initialUser.Node)
	}

	return stateStorage
}

func (this *Environment) createDataStorage(
	priorityGenerator storage.PriorityGenerator) (dataStorage peer.DataStorage, rawStorage *RawStorage) {
	rawStorage = &RawStorage{
		FreeStorageValue: 2 * gb,
		Timer:            &this.Scheduler}
	dataStorage = &storage.DataImpl{
		PriorityGenerator: priorityGenerator,
		RawStorage:        rawStorage}
	return
}

func (this *Environment) createDataStrategy(
	configuration *peer.Configuration,
	peerDistanceCalculator strategy.PeerDistanceCalculator,
	connectionInfoProvider strategy.ConnectionInfoProvider) peer.DataStrategy {
	return &strategy.Data{
		Configuration:          configuration,
		PeerDistanceCalculator: peerDistanceCalculator,
		ConnectionInfoProvider: connectionInfoProvider}
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

func (this *Environment) createActivity(
	user *User,
	configuration *peer.Configuration,
	chunkCreator sim.ChunkCreator) sim.UserActivity {
	return &Upload{
		User:               user,
		Configuration:      configuration,
		Delay:              randvar.NewNormalDuration(this.randSource, general.ParseDuration("15m"), general.ParseDuration("30m")),
		DataSize:           randvar.NewNormalFloat64(this.randSource, 32000, 32000),
		IdGenerator:        &IdGenerator{RandSource: this.randSource},
		ChunkCreator:       chunkCreator,
		AudienceProvider:   this,
		AttractionRatio:    randvar.NewNormalFloat64(this.randSource, 0.3, 0.1),
		AudiencePermutator: rand.New(this.randSource)}
}
