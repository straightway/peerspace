# Peerspace Simulation

First of all: Peerspace is *not* a simulation environment for peer to peer networks! But it uses
simulation to tune and optimize the network behavior.
 
In a peer to peer network like Peerspace, the behavior of the network is determined by the behavior
of the peers. It is not easy to foresee, which network behavior can be achieved by which peer
behavior.

In addition, it is not easy to profile and fine-tune such a peer to peer network during regular
operation, particularly if the network has a strong emphasis on privacy and data security.

To be able to have a fairly working network right from the start, Peerspace comes with a simulation
option. This simulation is event driven and particularly simulates
- a bigger number of peers 
- an underlying network with varying upload and download bandwidths
- the users

It allows measuring network-wide properties, such as:
- Answer times for queries
- Query fail rate
- Usage of network bandwidth
- Usage of storage space
- Redundancy of stored data

For an optimal operation of the network, all these properties should be as low as possible. The
simulation is a good and cheap way to experiment with different configurations or implementations
of various aspects to achieve optimal performance.

The simulated network is set up of a number of peers with their simulated users. Each peer has its
own simulated network connections and storage and is controlled by a simulated user. This user
generates requests and sends them to the network via peer he is controlling.

The simulated user is known by other simulated users, either because he is "famous", or because of
a "personal" relation. While relations to "famous" users are uni-directional, personal relations
are bidirectional. This is the basis for usage profiles the users execute, e.g. writing messages
or following a social network feed.

Actions for simulated users:
- Contact other user
- Send personal message
- Post to social network feed
- Upload file referenced in a message
- Download file referenced in a message