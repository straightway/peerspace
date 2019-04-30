# No strictly downward way to the "best" peer 

The routes data chunks take through the network must not have any circles. This can be
achieved by strictly routing data chunks only to nearer peers. But then we might have the
situation, that data chunks may not be able to reach the "best" destination peer in some
cases, because this "best" peer may only be known to another "worse" peer. Since the data
chunk would never be routed to a "worse" peer, it could not (immediately) reach the
"best" peer.
