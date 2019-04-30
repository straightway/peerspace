# Querying

To query data, the query request must be forwarded to a peer storing the queried data.

If a peer cannot answer a query directly, it forwards the query to one or more peers probably being
able to answer the query (i.e having a lower _distance_ to that data). If no such peer exists, the
query remains unanswered by this peer.

When a peer can answer a query, it pushes the result back to the peer it received the query request
from. This is also true for peers which received a query request and could not answer it
immediately, but later received an answer from another peer (a _transitive answer_). Therefore, a
peer must store for a certain time the information, when it received which queries from which other
peer, to be able to send results from _transitive answers_ back to the originator.

Querying for list results means specifying the list id and a condition for the timestamp. This may
either be a range or only the most recent entry. In any case it is clear, which epoch is queried,
so that the query can be forwarded accordingly by the peers. If the border of the time frame range
is close to the border of an epoch, it may be necessary to check both epochs on either side of the
border, because the peers may have slightly deviating system times.

It might be worth thinking about signaling back when a query definitely fails (e.g. if a peer
cannot satisfy a query and does not know any peer being closer). In contrast to silently failing,
this would enhance the responsiveness for the network users in this case.

## Problems

- [Query flooding](../issues/QueryFlooding.md)