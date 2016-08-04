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

package peerc

import (
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/peer"
)

type Node struct {
	Identifier           string
	StateStorage         peer.StateStorage
	DataStorage          peer.DataStorage
	AnnouncementStrategy peer.AnnouncementStrategy
	DataStrategy         peer.DataStrategy
	QueryStrategy        peer.QueryStrategy
	ConnectionStrategy   peer.ConnectionStrategy
	Timer                peer.Timer
	Configuration        *peer.Configuration

	connectingPeers []peer.Connector
	connectedPeers  []peer.Connector
	pendingQueries  []*pendingQuery
	isStarted       bool
}

type pendingQuery struct {
	query          data.Query
	expirationTime time.Time
	receivers      []peer.Pusher
}

func (this *pendingQuery) AddReceiver(receiver peer.Pusher, parent *Node) {
	this.receivers = append(this.receivers, receiver)
	this.refreshTimeout(parent)
}

func (this *Node) Id() string {
	return this.Identifier
}

func (this *Node) Equal(other general.Equaler) bool {
	connector, ok := other.(peer.Connector)
	return ok && connector.Id() == this.Id()
}

func (this *Node) Startup() {
	this.pendingQueries = make([]*pendingQuery, 0)
	this.assertConsistency()
	this.DataStorage.Startup()
	this.isStarted = true
	this.requestPeerConnections()
}

func (this *Node) ShutDown() {
	defer func() { this.isStarted = false }()
	for _, peer := range append(this.connectingPeers, this.connectedPeers...) {
		peer.CloseConnectionWith(this)
	}

	if this.DataStorage != nil {
		this.DataStorage.ShutDown()
	}
}

func (this *Node) RequestConnectionWith(peer peer.Connector) {
	if slice.Contains(this.connectedPeers, peer) {
		return
	}

	if this.isConnectionAcceptedWith(peer) {
		this.confirmConnectionWith(peer)
		this.acceptConnectionWith(peer)
	} else {
		this.refuseConnectionWith(peer)
	}
}

func (this *Node) CloseConnectionWith(peer peer.Connector) {
	this.connectingPeers = removePeer(this.connectingPeers, peer)
	this.connectedPeers = removePeer(this.connectedPeers, peer)
}

func (this *Node) AnnouncePeers(peers []peer.Connector) {
	for _, peer := range peers {
		if this.StateStorage.IsKnownPeer(peer) {
			continue
		}
		this.StateStorage.AddKnownPeer(peer)
		this.tryConnectWith(peer)
	}
}

func (this *Node) RequestPeers(receiver peer.Connector) {
	peersToAnnounce := this.AnnouncementStrategy.AnnouncedPeers()
	receiver.AnnouncePeers(peersToAnnounce)
}

func (this *Node) IsConnectionPendingWith(peer peer.Connector) bool {
	return slice.Contains(this.connectingPeers, peer)
}

func (this *Node) IsConnectedWith(peer peer.Connector) bool {
	return slice.Contains(this.connectedPeers, peer)
}

func (this *Node) ConnectedPeers() []peer.Connector {
	return append([]peer.Connector(nil), this.connectedPeers...)
}

func (this *Node) ConnectingPeers() []peer.Connector {
	return append([]peer.Connector(nil), this.connectingPeers...)
}

func (this *Node) Push(data *data.Chunk, origin id.Holder) {
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

func (this *Node) Query(query data.Query, receiver peer.Pusher) {
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

func (this *Node) IsStarted() bool {
	return this.isStarted
}

// Private

func (this *Node) tryConnectWith(peer peer.Connector) {
	if this.ConnectionStrategy.IsConnectionAcceptedWith(peer) {
		this.connectingPeers = append(this.connectingPeers, peer)
		peer.RequestConnectionWith(this)
	}
}

func (this *pendingQuery) refreshTimeout(parent *Node) {
	queryTimeout := parent.QueryStrategy.TimeoutFor(this.query)
	this.expirationTime = parent.Timer.Time().Add(queryTimeout)
}

func (this *Node) isConnectionAcceptedWith(peer peer.Connector) bool {
	return this.IsConnectionPendingWith(peer) ||
		this.ConnectionStrategy.IsConnectionAcceptedWith(peer)
}

func (this *Node) isConnectionPendingWith(peer peer.Connector) bool {
	return slice.Contains(this.connectingPeers, peer)
}

func (this *Node) refuseConnectionWith(peer peer.Connector) {
	peer.CloseConnectionWith(this)
}

func (this *Node) acceptConnectionWith(peer peer.Connector) {
	this.connectedPeers = append(this.connectedPeers, peer)
	this.connectingPeers = removePeer(this.connectingPeers, peer)
}

func removePeer(peers []peer.Connector, peerToRemove peer.Connector) []peer.Connector {
	return slice.RemoveItemsIf(peers, func(p interface{}) bool {
		return peerToRemove.Equal(p.(peer.Connector))
	}).([]peer.Connector)
}

func (this *Node) confirmConnectionWith(peer peer.Connector) {
	if this.isConnectionPendingWith(peer) == false {
		peer.RequestConnectionWith(this)
	}

	peer.RequestPeers(this)
}

func (this *Node) dataForwardPeers(origin id.Holder, key data.Key) []peer.Connector {
	forwardPeers := this.DataStrategy.ForwardTargetsFor(key, origin)
	for _, query := range this.pendingQueriesForKey(key) {
		return slice.SetUnion(forwardPeers, query.receivers).([]peer.Connector)
	}

	return forwardPeers
}

func (this *Node) pendingQueriesForKey(key data.Key) []*pendingQuery {
	result := make([]*pendingQuery, 0, 0)
	for _, q := range this.pendingQueries {
		if q.query.Matches(key) {
			result = append(result, q)
		}
	}

	return result
}

func (this *Node) forwardQuery(query data.Query, receiver peer.Pusher) {
	fwdPeers := this.QueryStrategy.ForwardTargetsFor(query, receiver)
	for _, p := range fwdPeers {
		p.Query(query, this)
	}
}

func (this *Node) registerPendingQuery(query data.Query, receiver peer.Pusher) {
	for _, pending := range this.pendingQueries {
		if pending.query == query {
			pending.AddReceiver(receiver, this)
			return
		}
	}

	queriesForKey := &pendingQuery{query: query, receivers: make([]peer.Pusher, 0)}
	queriesForKey.AddReceiver(receiver, this)
	this.pendingQueries = append(this.pendingQueries, queriesForKey)
}

func (this *Node) removeObsoleteQueries(fulfilledQueryKey data.Key) {
	this.removeExactlyMatchedPendingQueries(fulfilledQueryKey)
	this.removeTimedOutQueries()
}

func (this *Node) removeExactlyMatchedPendingQueries(fulfilledQueryKey data.Key) {
	this.pendingQueries = slice.RemoveItemsIf(this.pendingQueries, func(item interface{}) bool {
		pending := item.(*pendingQuery)
		return pending.query.MatchesOnly(fulfilledQueryKey)
	}).([]*pendingQuery)
}

func (this *Node) removeTimedOutQueries() {
	currentTime := this.Timer.Time()
	newPendingQueries := make([]*pendingQuery, 0)
	for _, query := range this.pendingQueries {
		if currentTime.Before(query.expirationTime) {
			newPendingQueries = append(newPendingQueries, query)
		}
	}
	this.pendingQueries = newPendingQueries
}

func (this *Node) assertConsistency() {
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

func (this *Node) requestPeerConnections() {
	allPeers := this.StateStorage.GetAllKnownPeers()
	for _, peer := range this.ConnectionStrategy.PeersToConnect(allPeers) {
		this.tryConnectWith(peer)
	}
}
