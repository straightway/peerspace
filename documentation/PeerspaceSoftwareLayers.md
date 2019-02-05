# Peerspace Software Layers

Peerspace consists of multiple sofware layery, some of them roughly similar to parts of the
[OSI model](https://en.wikipedia.org/wiki/OSI_model):

1. Data Link Layer (currently only in networksimulator)
2. Network Layer (straightway.peerspace.net)
3. Transport Layer (straightway.peerspace.transport)
4. Application Layer (application specific)

## Data Link Layer

On this layer, the peers connect to each other. Peer connections are generally short-living
and only used for the transmission of single requests (push, query). If this turns out to
have considerable performance drawbacks, this might be weakened in the future.

The main interface which must be implemented by the network layer is the
[Channel](../network/src/main/kotlin/straightway/peerspace/net/Channel.kt) interface. It
allows transmitting any serializable object with asynchronous notification of
success or failure.

The data link layer is generally exchangeable. The current implementation focuses on
network simluation to prove overall feasibility of the concepts behind Peerspace and
fine-tune the parameters and configurations. However, the productive implementation
currently plans to user TOR hidden services as network layer (see also
[here](PeerspaceIdeas.md#Networking)). This solves the NAT traversal problem, because
TOR is started with outbound connections and allows inbound connections through hidden
services. And it adds an extra portion of encryption and anonymity, although this is
generally not needed with Peerspace, because the communication is already encrypted and
anonymous. If in the future NAT traversal may not be a problem any more, the data link layer
may be echanged by something leaner and simpler than a TOR bridge. 

## Network Layer

On the network layer, data chunks of fixed size are distributed between the peers of the
network using a distributed hash table algorithm (DHT). Also these chunks can be queried.
They are cached locally on each peer receiving a chunk of data, thus storing the data
redundantly distributed over the network.

The network layer relies on the data link layer to contact other peers. Its main access
facade for layers above the network layer is the
[PeerClient](../network/src/main/kotlin/straightway/peerspace/net/PeerClient.kt) interface.

## Transport Layer

This layer transmits and queries arbitrary data using the network layer.
* Cut the data into chunks and recombines the chunks after receiving them.
* Provide high level control over receive states of queried data.
* Care about encryption and signing of transmitted data.
