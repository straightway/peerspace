# Straightway.Peerspace Transport Layer

This layer transmits and queries arbitrary data using the network layer.
* [Cut the data into chunks and recombine the chunks after receiving them.](documentation/Chunking.md)
* Provide high level control over receive states of queried data.
* Care about encryption and signing of transmitted data.

The main access facade is the
[Transport](src/main/kotlin/straightway/peerspace/transport/Transport.kt)
interface.
