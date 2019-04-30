# Storage space management

The storage space on each peer is limited. So a peer must free some space from time to time
in order to store new data. To have as much data stored in the nwtwork as possible, this should
only be done if there is no space left on the peer's storage (i.e. the configured
amount of storage is entirely used).

There are two ways to free space:

1. Discard data
2. Move data to another peer

It is subject to a data storage strategy deciding which of both ways is applied to which chunk of
data. It could consider the following criteria:

- Overall age of the data
- Storage duration of the data on the peer
- _Distance_ of the data to the peer
- Availability of peers with a lower _distance_

The strategy might even decide to refuse the storage of certain chunks at all, e.g. if the
_distance_ to the peer exceeds a certain limit.

### Problems

- [Redundancy vs. Capacity](../issues/RedundancyVsCapacity.md)