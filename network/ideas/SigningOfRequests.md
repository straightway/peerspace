# Signing of Requests

E.g. a query request must contain the id of the peer it received the request from, in order to push
query requests back. To make sure malicious peers cannot issue query requests containing the id of
other peers, query requests must be digitally signed by the issuer. The receiver of the request
must be able to verify that signature. An easy way to achieve this is to use the public key of the
signature key pair as peer id.
