# Peerspace Ideas
## Data distribution
Each peer has a list of other known peers and knows how to contact them. The communication between the peers provides the following primitive types of requests:

- **push**: Push data into the network by passing it to another peer which in turn may pass it further on to other peers.
- **query**: Search for data. Query requests are passed on from peer to peer to find the peer storing the data. The query results are pushed back along the query path in case the data is found.

### Identification of data
Each chunk of data is identified by a key which either is

- the content hash code of the data chunk, or
- a list or mutable chunk id together with the timestamp of the creation of the data chunk

### Data forwarding
To find out the best peer where a data chunk should be stored, the _distance_ between the peer and a chunk of data is considered. This _distance_ is defined as the absolute difference of the hash code of the peer's id and the hash code of the data chunk's key.

Using this _distance_, a peer can easily decide if a chunk of data should be pushed on to some other peer, which has a lower distance to it.

### Forwarding and storage of list items
It is desirable to have list items distributed over multiple peers, instead of storing them all on the same peers.

Also, if the timestamp had a resolution of e.g. milliseconds (which is desirable to have a low probability of two peers creating the same timestamp for a new list item), it would make sense to define epochs for timestamps, which include all timestamps of a given time range.

Since we assume that the access frequency for a given list item becomes lower and lower with growing age, these epochs should be relative to the current time, and become bigger the older they are. Proposal:

- 1 hour
- 1 day
- 1 week
- 1 month
- 1 year
- 10 years
- older than 10 years

The _distance_ of a list item to a peer's id should be determined from the combined hash code of the item's list id and epoch.

This is of course the consequence, that the peer's  _distance_ to an item changes when the item enters the next epoch, which in turn may result in forwarding and/or deleting the item.

### Forwarding and mutable chunks
Mutable chunks work very much like lists. The only difference is, that peers only store the most recent instance of a mutable chunk, i.e. receiving a mutable chunk instance with a newer timestamp than the existing one overrides it. Received mutable chunk instances with an older timestamp than the existing one are ignored.

### Redundancy vs. capacity
To be robust against failed queries due to (temporary) offline peers, data must be stored redundantly on multiple peers. As the overall storage capacity of the Peerspace network is limited, redundancy further reduces this capacity. So a trade-off between redundancy and capacity must made by a smart distribution algorithm.

## Privileges

The read and write access to plain data chunks is unrestricted on the basic level. In order to control read access, the content must be encrypted.

The same is true for read access to lists. However, to restrict write access to lists, each list entry must be cryptographically signed using an asymmetric key pair. The public key is used as list id, the private key is called _list access token_ and used to sign the data. This allows any peer to check if the sender of a list entry has the write privilege on that list (i.e. access to the _list access_token_).

Peers ignore all invalid requests they receive.

## Storage space management
Since the storage space on each peer is limited. So a peer must free some space from time to time in order to store new data. To have as much information available as possible, this should only be done of there is no space left on the peer's storage.

There are two ways to free space:

1. Discard data
2. Move data to another peer

It is subject to a data storage strategy deciding which of both ways is applied to which chunk of data. It could consider the following criteria:

- Overall age of the data
- Storage duration of the data on the peer
- _Distance_ of the data to the peer
- Availability of peers with a lower _distance_

The strategy might even decide to refuse the storage of certain chunks at all, e.g. if the _distance_ to the peer exceeds a certain limit.

## Querying
To query data, the query request must be forwarded to a peer storing the queried data.

If a peer cannot answer a query directly, it forwards the query to one or more peers probably being able to answer the query (i.e having a lower _distance_ to that data). If no such peer exists, the query remains unanswered by this peer.

When a peer can answer a query, it pushes the result back to the peer it received the query request from. This is also true for peers which received a query request and could not answer it immediately, but later received an answer from another peer (a _transitive answer_). Therefore, a peer must store for a certain time the information, when it received which queries from which other peer, to be able to send results from _transitive answers_ back to the originator.

Querying for list results means specifying the list id and a condition for the timestamp. This may either be a range or only the most recent entry. In any case it is clear, which epoch is queried, so that the query can be forwarded accordingly by the peers. If the border of the time frame range is close to the border of an epoch, it may be necessary to check both epochs on either side of the border, because the peers may have slightly deviating system times.

It might be worth thinking about signaling back when a query definitely fails (e.g. if a peer cannot satisfy a query and does not know any peer being closer). In contrast to silently failing, this would enhance the responsiveness for the network users in this case.

## Signing of Requests
E.g. a query request must contain the id of the peer which issued the request, in order to push query requests back. To make sure malicious peers cannot issue query requests containing the id of other peers, query requests must be digitally signed by the issuer. The receiver of the request must be able to verify that signature. An easy way to achieve this is to use the public key of the signature key pair as peer id.

## Networking
To be able to receive requests from other peers, each peer must open a network port for this purpose. But it is not desirable to be obliged to change any firewall or NAT configuration for that. So the idea is to use TOR for that purpose: Each peer starts a hidden service to be reachable for other peers. To be isolated from other applications using TOR, an own TOR instance should be launched.

