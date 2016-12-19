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
	"reflect"
	"runtime"
	"strings"
	"time"

	"github.com/apex/log"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/sim"
	slog "github.com/straightway/straightway/simc/log"
)

type NetworkProperties struct {
	EventScheduler sim.EventScheduler
	SizeOfer       data.SizeOfer
	Latency        time.Duration
	Bandwidth      float64 // in bytes/s
	connectors     map[id.Type]*NetworkPeerConnector
}

type NetworkPeerConnector struct {
	wrapped    peer.Connector
	properties *NetworkProperties
	sendQueue  []sendItem
}

type sendItem struct {
	duration   time.Duration
	sendAction func()
	detail     log.Fields
}

func NewNetworkPeerConnector(
	wrapped peer.Connector,
	properties *NetworkProperties) *NetworkPeerConnector {

	if properties.connectors == nil {
		properties.connectors = make(map[id.Type]*NetworkPeerConnector)
	}

	connector, isFound := properties.connectors[wrapped.Id()]
	if isFound == false {
		connector = &NetworkPeerConnector{wrapped: wrapped, properties: properties}
		properties.connectors[wrapped.Id()] = connector
	}

	return connector
}

func (this *NetworkPeerConnector) Id() id.Type {
	return this.wrapped.Id()
}

func (this *NetworkPeerConnector) Equal(other general.Equaler) bool {
	return this.wrapped.Equal(other)
}

func (this *NetworkPeerConnector) Wrapped() peer.Connector {
	return this.wrapped
}

func (this *NetworkPeerConnector) Bandwidth() float64 {
	return this.properties.Bandwidth
}

func (this *NetworkPeerConnector) Latency() time.Duration {
	return this.properties.Latency
}

func (this *NetworkPeerConnector) Push(data *data.Chunk, origin id.Holder) {
	sendDuration := this.sendDurationForSize(this.properties.SizeOfer.SizeOf(data))
	this.scheduleFor(
		origin,
		sendDuration,
		func() {
			this.wrapped.Push(data, this.tryWrap(origin).(id.Holder))
		},
		data.Key)
}

func (this *NetworkPeerConnector) Query(query data.Query, receiver peer.PusherWithId) {
	this.scheduleFor(
		receiver,
		this.properties.Latency,
		func() {
			this.wrapped.Query(query, this.tryWrap(receiver).(peer.PusherWithId))
		},
		query)
}

func (this *NetworkPeerConnector) RequestConnectionWith(otherPeer peer.Connector) {
	this.scheduleFor(
		otherPeer,
		this.properties.Latency,
		func() {
			this.wrapped.RequestConnectionWith(this.tryWrap(otherPeer).(*NetworkPeerConnector))
		})
}

func (this *NetworkPeerConnector) CloseConnectionWith(otherPeer peer.Connector) {
	this.scheduleFor(
		otherPeer,
		this.properties.Latency,
		func() {
			this.wrapped.CloseConnectionWith(this.tryWrap(otherPeer).(*NetworkPeerConnector))
		})
}

func (this *NetworkPeerConnector) RequestPeers(receiver peer.Connector) {
	this.scheduleFor(
		receiver,
		this.properties.Latency,
		func() {
			this.wrapped.RequestPeers(this.tryWrap(receiver).(*NetworkPeerConnector))
		})
}

func (this *NetworkPeerConnector) AnnouncePeersFrom(from peer.Connector, peers []peer.Connector) {
	peerSizes := uint64(0)
	wrappedPeers := make([]peer.Connector, len(peers))
	for i, p := range peers {
		peerSizes += uint64(len(p.Id()))
		wrappedPeers[i] = this.tryWrap(p).(*NetworkPeerConnector)
	}

	sendDuration := this.sendDurationForSize(peerSizes)
	this.scheduleFor(
		from,
		sendDuration,
		func() {
			this.wrapped.AnnouncePeersFrom(this.tryWrap(from).(peer.Connector), wrappedPeers)
		},
		len(peers))
}

// Private

func (this *NetworkPeerConnector) tryWrap(obj interface{}) interface{} {
	wrapped, isPeerConnector := obj.(peer.Connector)
	if isPeerConnector {
		_, isAlreadyWrapped := wrapped.(*NetworkPeerConnector)
		if isAlreadyWrapped == false {
			return NewNetworkPeerConnector(wrapped, this.properties)
		}
	}

	return obj
}

func (this *NetworkPeerConnector) sendDurationForSize(size uint64) time.Duration {
	transmissionTime := time.Duration(float64(size)/this.properties.Bandwidth) * time.Second
	return this.properties.Latency + transmissionTime
}

func (this *NetworkPeerConnector) scheduleFor(
	origin id.Holder,
	duration time.Duration,
	action func(),
	logInfo ...interface{}) {

	originPeer, isPeer := this.tryWrap(origin).(*NetworkPeerConnector)
	detail := log.Fields{
		"EntryType":   "NodeAction",
		"Function":    function(action),
		"Origin":      origin.Id(),
		"Destination": this.Id(),
		"Parameter":   slice.ToString(logInfo, ",")}

	if isPeer {
		originPeer.schedule(duration, action, detail)
	} else {
		action()
	}
}

func (this *NetworkPeerConnector) schedule(
	duration time.Duration,
	action func(),
	detail log.Fields) {

	if len(this.sendQueue) == 0 {
		this.properties.EventScheduler.Schedule(duration, this.sendNextItem)
	}

	this.sendQueue = append(
		this.sendQueue,
		sendItem{
			duration:   duration,
			sendAction: action,
			detail:     detail})

	if slog.IsEnabled() {
		this.addSendQueue(detail)
		log.WithFields(detail).Info("Scheduled")
	}
}

func (this *NetworkPeerConnector) sendNextItem() {
	switch len(this.sendQueue) {
	case 1:
		break
	default:
		this.properties.EventScheduler.Schedule(this.sendQueue[1].duration, this.sendNextItem)
	}

	nextItem := this.sendQueue[0]

	nextItem.sendAction()
	this.sendQueue = this.sendQueue[1:]

	if slog.IsEnabled() {
		this.addSendQueue(nextItem.detail)
		log.WithFields(nextItem.detail).Info("Execute")
	}
}

func (this *NetworkPeerConnector) addSendQueue(detail log.Fields) {
	result := make([]log.Fields, len(this.sendQueue)+1)
	currentDuration := time.Duration(0)
	for i, item := range this.sendQueue {
		currentDuration += item.duration
		itemLog := copyPlainLogFields(item.detail)
		itemLog["Duration"] = currentDuration
		result[i+1] = itemLog
	}

	result[0] = log.Fields{"Deferred": true}

	detail["SendQueue"] = result
}

func copyPlainLogFields(src log.Fields) log.Fields {
	result := log.Fields{}
	for key, value := range src {
		if _, isSubLog := value.([]log.Fields); isSubLog {
			continue
		}
		result[key] = value
	}
	return result
}

func function(action func()) string {
	fullName := runtime.FuncForPC(reflect.ValueOf(action).Pointer()).Name()
	nameComponents := strings.Split(fullName, ".")
	return nameComponents[len(nameComponents)-2]
}
