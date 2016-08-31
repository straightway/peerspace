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

package simc

import (
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/sim"
)

type NetworkPeerConnector struct {
	Wrapped        peer.Connector
	EventScheduler sim.EventScheduler
	RawStorage     data.RawStorage
	Latency        time.Duration
	Bandwidth      float64 // in bytes/s
}

func (this *NetworkPeerConnector) Id() string {
	return this.Wrapped.Id()
}

func (this *NetworkPeerConnector) Equal(other general.Equaler) bool {
	return this.Wrapped.Equal(other)
}

func (this *NetworkPeerConnector) Push(data *data.Chunk, origin id.Holder) {
	sendDuration := this.sendDurationForSize(this.RawStorage.SizeOf(data))
	this.EventScheduler.Schedule(
		sendDuration,
		func() {
			this.Wrapped.Push(data, this.tryWrap(origin).(id.Holder))
		})
}

func (this *NetworkPeerConnector) Query(query data.Query, receiver peer.PusherWithId) {
	this.EventScheduler.Schedule(
		this.Latency,
		func() {
			this.Wrapped.Query(query, this.tryWrap(receiver).(peer.PusherWithId))
		})
}

func (this *NetworkPeerConnector) RequestConnectionWith(otherPeer peer.Connector) {
	this.EventScheduler.Schedule(
		this.Latency,
		func() {
			this.Wrapped.RequestConnectionWith(this.tryWrap(otherPeer).(peer.Connector))
		})
}

func (this *NetworkPeerConnector) CloseConnectionWith(otherPeer peer.Connector) {
	this.EventScheduler.Schedule(
		this.Latency,
		func() {
			this.Wrapped.CloseConnectionWith(this.tryWrap(otherPeer).(peer.Connector))
		})
}

func (this *NetworkPeerConnector) RequestPeers(receiver peer.Connector) {
	this.EventScheduler.Schedule(
		this.Latency,
		func() {
			this.Wrapped.RequestPeers(this.tryWrap(receiver).(peer.Connector))
		})
}

func (this *NetworkPeerConnector) AnnouncePeers(peers []peer.Connector) {
	peerSizes := uint64(0)
	wrappedPeers := make([]peer.Connector, len(peers))
	for i, p := range peers {
		peerSizes += uint64(len(p.Id()))
		wrappedPeers[i] = this.tryWrap(p).(peer.Connector)
	}

	sendDuration := this.sendDurationForSize(peerSizes)
	this.EventScheduler.Schedule(sendDuration, func() { this.Wrapped.AnnouncePeers(wrappedPeers) })
}

// Private

func (this *NetworkPeerConnector) tryWrap(obj interface{}) interface{} {
	wrapped, isPeerConnector := obj.(peer.Connector)
	if isPeerConnector {
		_, isAlreadyWrapped := wrapped.(*NetworkPeerConnector)
		if isAlreadyWrapped == false {
			return &NetworkPeerConnector{
				Wrapped:        wrapped,
				EventScheduler: this.EventScheduler,
				RawStorage:     this.RawStorage,
				Latency:        this.Latency,
				Bandwidth:      this.Bandwidth}
		}
	}

	return obj
}

func (this *NetworkPeerConnector) sendDurationForSize(size uint64) time.Duration {
	transmissionTime := time.Duration(float64(size)/this.Bandwidth) * time.Second
	return this.Latency + transmissionTime
}
