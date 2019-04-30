## Deleting of data in the network

In order to delete data in the Straigway.Peerspace network, a special type of request could be
used. This _delete request_ contains the ID of the chunk to delete, and it is forwarded on the
same path as a storage request for that chunk would be.

To only allow the "owner" of a piece of data to delete this data, the data chunk is decorated with
the hash code of a randomly generated _deletion token_ when it is stored. This _deletion token_ is
attached to the _delete request_ to prove the validity of that request. A delete request is only
executed, if the the hash code _deletion token_ is equal to that in the chunk.

Each peer stores _delete requests_ for a certain time. If it receives a data chunk being subject
to a _delete request_, it is discarded immediately and not forwarded. In addition, the _delete
request_ is forwarded to the peer which sent that deleted data chunk.     
