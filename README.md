# Straightway.Peerspace

Straightway.Peerspace is a peer to peer distributed network storage layer. It allows implementing
collaboration apps like social networks, data sharing, elections, virtual conferences and more.

All data is distributed in fixed size chunks over all peers of the network using a distributed
hashtable algorithm (DHT). Communication and storage is cryptographically secured.

Top level requirements are defined [here](Requirements.md).

The software architecture roughly follows the [OSI model](https://en.wikipedia.org/wiki/OSI_model):
 * [Data Link Layer](datalink/README.md)
 * [Network Layer](network/README.md)
 * [Transport Layer](transport/README.md)
 * [Service layer](services/README.md)
 * Application Layer (application specific, currently only in
   [networksimulator](networksimulator/src/main/kotlin/straightway/peerspace/networksimulator/activities))

Functionality required by more than one layer:

 * [Crypto](crypto/README.md)
 * [Data](data/README.md)
 
 Quality related:
 
 * [Integration tests](integrationtest/README.md)
 * [Simulation](networksimulator/README.md)

Presentations:
 * [For Non-Techies](documentation/StraightwayForNonTechies.pdf)
   ([source odp](documentation/StraightwayForNonTechies.odp))