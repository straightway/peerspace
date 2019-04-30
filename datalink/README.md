# Straigtway.Peerspace Data Link Layer

On this layer, the peers connect to each other. Peer connections are generally short-living
and only used for the transmission of single requests (push, query). If this turns out to
have considerable performance drawbacks, this might be weakened in the future.

The main interface which must be implemented by the network layer is the
[Channel](../network/src/main/kotlin/straightway/peerspace/net/Channel.kt) interface. It
allows transmitting any serializable object with asynchronous notification of
success or failure.

The data link layer is generally exchangeable. The current implementation focuses on
network simluation to prove overall feasibility of the concepts behind Peerspace and
fine-tune the parameters and configurations.

However, the productive implementation currently plans to user TOR hidden services as network
layer. To be able to receive requests from other peers, each peer must open a network port for this
purpose. But it is not desirable to be obliged to change any firewall or NAT configuration for
that. So the idea is to use TOR for that purpose: Each peer starts a hidden service to be reachable
for other peers. To be isolated from other applications using TOR, an own TOR instance should be
launched.

This solves the NAT traversal problem, because TOR is started with outbound connections and allows
inbound connections through hidden services. And it adds an extra portion of encryption and
anonymity, although this is generally not needed with Peerspace, because the communication is
already encrypted and anonymous. If in the future NAT traversal may not be a problem any more,
the data link layer may be echanged by something leaner and simpler than a TOR bridge. 
