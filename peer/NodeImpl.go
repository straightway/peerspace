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

package peer

import (
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
)

type NodeImpl struct {
	Identifier           string
	StateStorage         StateStorage
	DataStorage          DataStorage
	AnnouncementStrategy AnnouncementStrategy
	DataStrategy         DataStrategy
	QueryStrategy        QueryStrategy
	ConnectionStrategy   ConnectionStrategy
	Timer                Timer
	Configuration        *Configuration

	connectingPeers []Connector
	connectedPeers  []Connector
	pendingQueries  []*pendingQuery
	isStarted       bool
}

type pendingQuery struct {
	query          Query
	expirationTime time.Time
	receivers      []Pusher
}

func (this *pendingQuery) AddReceiver(receiver Pusher, parent *NodeImpl) {
	this.receivers = append(this.receivers, receiver)
	this.refreshTimeout(parent)
}

func (this *NodeImpl) Id() string {
	return this.Identifier
}

func (this *NodeImpl) Equal(other general.Equaler) bool {
	connector, ok := other.(Connector)
	return ok && connector.Id() == this.Id()
}

func (this *NodeImpl) Startup() {
	this.pendingQueries = make([]*pendingQuery, 0)
	this.assertConsistency()
	this.DataStorage.Startup()
	this.isStarted = true
	this.requestPeerConnections()
}

func (this *NodeImpl) ShutDown() {
	defer func() { this.isStarted = false }()
	for _, peer := range append(this.connectingPeers, this.connectedPeers...) {
		peer.CloseConnectionWith(this)
	}
}

func (this *NodeImpl) RequestConnectionWith(peer Connector) {
	if general.Contains(this.connectedPeers, peer) {
		return
	}

	if this.isConnectionAcceptedWith(peer) {
		this.confirmConnectionWith(peer)
		this.acceptConnectionWith(peer)
	} else {
		this.refuseConnectionWith(peer)
	}
}

func (this *NodeImpl) CloseConnectionWith(peer Connector) {
	this.connectingPeers = removePeer(this.connectingPeers, peer)
	this.connectedPeers = removePeer(this.connectedPeers, peer)
}

func (this *NodeImpl) AnnouncePeers(peers []Connector) {
	for _, peer := range peers {
		if this.StateStorage.IsKnownPeer(peer) {
			continue
		}
		this.StateStorage.AddKnownPeer(peer)
		this.tryConnectWith(peer)
	}
}

func (this *NodeImpl) RequestPeers(receiver Connector) {
	peersToAnnounce := this.AnnouncementStrategy.AnnouncedPeers()
	receiver.AnnouncePeers(peersToAnnounce)
}

func (this *NodeImpl) IsConnectionPendingWith(peer Connector) bool {
	return general.Contains(this.connectingPeers, peer)
}

func (this *NodeImpl) IsConnectedWith(peer Connector) bool {
	return general.Contains(this.connectedPeers, peer)
}

func (this *NodeImpl) ConnectedPeers() []Connector {
	return append([]Connector(nil), this.connectedPeers...)
}

func (this *NodeImpl) ConnectingPeers() []Connector {
	return append([]Connector(nil), this.connectingPeers...)
}

func (this *NodeImpl) Push(data *data.Chunk, origin Connector) {
	if data == nil {
		return
	}

	if !this.DataStrategy.IsChunkAccepted(data, origin) {
		return
	}

	for _, p := range this.dataForwardPeers(origin, data.Key) {
		p.Push(data, this)
	}

	this.DataStorage.ConsiderStorage(data)
	this.removeObsoleteQueries(data.Key)
}

func (this *NodeImpl) Query(query Query, receiver Pusher) {
	if !this.QueryStrategy.IsQueryAccepted(query, receiver) {
		return
	}

	queryResults := this.DataStorage.Query(query)
	if len(queryResults) == 0 || query.IsTimed() {
		this.registerPendingQuery(query, receiver)
		this.forwardQuery(query, receiver)
	}

	for _, queryResult := range queryResults {
		receiver.Push(queryResult, this)
	}
}

func (this *NodeImpl) IsStarted() bool {
	return this.isStarted
}

// Private

func (this *NodeImpl) tryConnectWith(peer Connector) {
	if this.ConnectionStrategy.IsConnectionAcceptedWith(peer) {
		peer.RequestConnectionWith(this)
		this.connectingPeers = append(this.connectingPeers, peer)
	}
}

func (this *pendingQuery) refreshTimeout(parent *NodeImpl) {
	queryTimeout := parent.QueryStrategy.TimeoutFor(this.query)
	this.expirationTime = parent.Timer.Time().Add(queryTimeout)
}

func (this *NodeImpl) isConnectionAcceptedWith(peer Connector) bool {
	return this.IsConnectionPendingWith(peer) ||
		this.ConnectionStrategy.IsConnectionAcceptedWith(peer)
}

func (this *NodeImpl) isConnectionPendingWith(peer Connector) bool {
	return general.Contains(this.connectingPeers, peer)
}

func (this *NodeImpl) refuseConnectionWith(peer Connector) {
	peer.CloseConnectionWith(this)
}

func (this *NodeImpl) acceptConnectionWith(peer Connector) {
	this.connectedPeers = append(this.connectedPeers, peer)
	this.connectingPeers = removePeer(this.connectingPeers, peer)
}

func removePeer(peers []Connector, peerToRemove Connector) []Connector {
	return general.RemoveItemsIf(peers, func(p interface{}) bool {
		return peerToRemove.Equal(p.(Connector))
	}).([]Connector)
}

func (this *NodeImpl) confirmConnectionWith(peer Connector) {
	if this.isConnectionPendingWith(peer) == false {
		peer.RequestConnectionWith(this)
	}

	peer.RequestPeers(this)
}

func (this *NodeImpl) dataForwardPeers(origin Connector, key data.Key) []Connector {
	forwardPeers := this.DataStrategy.ForwardTargetsFor(key, origin)
	for _, query := range this.pendingQueriesForKey(key) {
		return general.SetUnion(forwardPeers, query.receivers).([]Connector)
	}

	return forwardPeers
}

func (this *NodeImpl) pendingQueriesForKey(key data.Key) []*pendingQuery {
	result := make([]*pendingQuery, 0)
	for _, q := range this.pendingQueries {
		if q.query.Matches(key) {
			result = append(result, q)
		}
	}

	return result
}

func (this *NodeImpl) forwardQuery(query Query, receiver Pusher) {
	fwdPeers := this.QueryStrategy.ForwardTargetsFor(query, receiver)
	for _, p := range fwdPeers {
		p.Query(query, this)
	}
}

func (this *NodeImpl) registerPendingQuery(query Query, receiver Pusher) {
	for _, pending := range this.pendingQueries {
		if pending.query == query {
			pending.AddReceiver(receiver, this)
			return
		}
	}

	queriesForKey := &pendingQuery{query: query, receivers: make([]Pusher, 0)}
	queriesForKey.AddReceiver(receiver, this)
	this.pendingQueries = append(this.pendingQueries, queriesForKey)
}

func (this *NodeImpl) removeObsoleteQueries(fulfilledQueryKey data.Key) {
	this.removeExactlyMatchedPendingQueries(fulfilledQueryKey)
	this.removeTimedOutQueries()
}

func (this *NodeImpl) removeExactlyMatchedPendingQueries(fulfilledQueryKey data.Key) {
	this.pendingQueries = general.RemoveItemsIf(this.pendingQueries, func(item interface{}) bool {
		pending := item.(*pendingQuery)
		return pending.query.MatchesOnly(fulfilledQueryKey)
	}).([]*pendingQuery)
}

func (this *NodeImpl) removeTimedOutQueries() {
	currentTime := this.Timer.Time()
	newPendingQueries := make([]*pendingQuery, 0)
	for _, query := range this.pendingQueries {
		if currentTime.Before(query.expirationTime) {
			newPendingQueries = append(newPendingQueries, query)
		}
	}
	this.pendingQueries = newPendingQueries
}

func (this *NodeImpl) assertConsistency() {
	if this.StateStorage == nil {
		panic("No StateStorage")
	}
	if this.DataStorage == nil {
		panic("No DataStorage")
	}
	if this.DataStrategy == nil {
		panic("No DataStrategy")
	}
	if this.ConnectionStrategy == nil {
		panic("No ConnectionStrategy")
	}
	if this.Timer == nil {
		panic("No Timer")
	}
	if this.Configuration == nil {
		panic("No Configuration")
	}
}

func (this *NodeImpl) requestPeerConnections() {
	allPeers := this.StateStorage.GetAllKnownPeers()
	for _, peer := range this.ConnectionStrategy.PeersToConnect(allPeers) {
		this.tryConnectWith(peer)
	}
}
