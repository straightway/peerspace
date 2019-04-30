# Query Flooding

As queries must be kept alive for some time in order to forward _transitive answers_,
a malicious peer could issue a large amount of queries to uses as many system resources
as possible for the maintenance of these pending queries on other peers. This might lead
to denial of service of the network.

This problem could be mitigated by limiting the number of handled queries. This would
at least protect the peers themselves, although it might still lead to a denial of
service of parts of the network.   