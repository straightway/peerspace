# Data distribution

Each peer has a list of other known peers and knows how to contact them. The communication between
the peers provides the following primitive types of requests:

- **push**: Push data into the network by passing it to another peer which in turn may pass it
  further on to other peers.
- **query**: Search for data. Query requests are passed on from peer to peer to find the peer
  storing the data. The query results are pushed back along the query path in case the data is
  found.

## Routing

To find out the best peer where a data chunk should be stored, the _distance_ between the peer and
a chunk of data is considered. This _distance_ is defined as the absolute difference of the hash
code of the peer's id and the hash code of the data chunk's key.

Using this _distance_, a peer can easily decide if a chunk of data should be pushed on to some
other peer, which has a lower distance to it.

### Forwarding and storage of list items

It is desirable to have list items distributed over multiple peers, instead of storing them all on
the same peers.

Also, if the timestamp had a resolution of e.g. milliseconds (which is desirable to have a low
probability of two peers creating the same timestamp for a new list item), it would make sense to
define epochs for timestamps, which include all timestamps of a given time range.

Since we assume that the access frequency for a given list item becomes lower and lower with
growing age, these epochs should be relative to the current time, and become bigger the older they
are. Proposal:

- 1 hour
- 1 day
- 1 week
- 1 month
- 1 year
- 10 years
- older than 10 years

The _distance_ of a list item to a peer's id should be determined from the combined hash code of
the item's list id and epoch.

This has of course the consequence, that the peer's  _distance_ to an item changes when the item
enters the next epoch, which in turn may result in forwarding and/or deleting the item.

### Forwarding and mutable chunks

Mutable chunks work very much like lists. The only difference is, that peers only store the most
recent instance of a mutable chunk, i.e. receiving a mutable chunk instance with a newer timestamp
than the existing one overrides it. Received mutable chunk instances with an older timestamp than
the existing one are ignored.

### Problems

- [Update races](../issues/UpdateRaces.md)
