## Privileges

The read and write access to plain data chunks is unrestricted on the basic level. In order to
control read access, the content must be encrypted.

The same is true for read access to lists. However, to restrict write access to lists, each list
entry must be cryptographically signed using an asymmetric key pair. The public key is used as list
id, the private key is called _list access token_ and used to sign the data. This allows any peer
to check if the sender of a list entry has the write privilege on that list (i.e. access to the
_list access_token_).

Peers ignore all invalid requests they receive.

A list may grant read and write privilege to anyone, then being called an _open list_. In this
case, the list is given an arbitrary name, and the list key consists of the hash of that name.
The content is encrypted using a symmetric key consisting of the hash of a modification of the
list name (e.g. plus a fixed suffix). Please notice that _open lists_ may be subject for spamming.