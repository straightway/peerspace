# Straightway.Peerspace Network Layer

On the network layer, data chunks of fixed size are distributed between the peers of the
network using a distributed hash table algorithm (DHT). Also these chunks can be queried.
They are cached locally on each peer receiving a chunk of data, thus storing the data
redundantly distributed over the network. The chunks contain unstructured binary data.

In order to execute storage request for a data chunks, peers may displace other chunks
they store. This way, the Peerspace network may "forget" data. This is not a bug but
a feature. Efforts are made to make it highly probable, that the displaced data is
"irrelevant", i.e. no one is interested in that data any more. This is done by tracking
the last access time of that data, either by storage or by query. It is assumed, that
data becomes more and more "irrelevant" with growing time distance to the last access.
Peers may push the data again or query it to keep it alive. 

The network layer also cares about tracking known peers. A fresh installation only knows
a few fixed peers. All peers can answer _known peer requests_, and after a fresh installation,
these fixed peers can are contacted in order to get to know more peers. Also while handling
storage or query requests, the involved peers are stored as _known peers_ and can be contacted
later in order to issue requests.

The network layer relies on the data link layer to contact other peers. Its main access
facade for layers above the network layer is the
[PeerClient](src/main/kotlin/straightway/peerspace/net/PeerClient.kt) interface.

Further ideas:

 * [Deleting of data in the network](ideas/DeletingDataInTheNetwork.md)
 * [Signing of Requests](ideas/SigningOfRequests.md) 

See also:

* [Issues](issues)