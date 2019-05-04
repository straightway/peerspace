# Straightway.Peerspace Chunking

In Straightway.Peerspace, data on the [network layer](../../network/README.md)
is is transmitted in data chunks of fixed size. It is the responisbility of
the [transport layer](../README.md) to cut the data
into chunks of this size and encrypt the chunks properly. This document describes
the data format of the chunks.

## General

Each data chunk has a fixed size of _CHUNK_SIZE_. It is subdivided into a header and
the payload.

The content encoding must follow one of the encoding versions defined below. Which encoding
version is used, is defined by the first byte of the data chunk header. The further header
structure is defined by the encoding version.

It may be necessary in the future to enhance the header structure. This may be realized by
defining a new header version. Since chunks with all previous header version still may be
around, always all header versions must be supported by the software.

Integer values are always encoded in big endian byte order.

### Version 0

A version 0 chunk contains a header only consisting of the version number. The rest
of the chunk is the payload.


                 Header||
                Version||Payload
                  +----++----+
    Size in Bytes |   1||   *|
                  +----++----+
          Meaning |0x00||PYLD|
                  +----++----+

### Version 1

A version 1 chunk contains a header consisting of the version number and a field
MSZE (Minus SiZE), which contains the difference of the chunk size and the payload size in bytes.
The rest of the chunk os the payload.


                      Header||
                Version|    ||Payload
                  +----+----++----+
    Size in Bytes |   1|   1||   *|
                  +----+----++----+
          Meaning |0x01|MSZE||PYLD|
                  +----+----++----+

A version 1 chunk can be used for "almost full" payload. If you have a payload of
e.g. the _CHUNK_SIZE_ - 3, then it is impossible to encode this using a version 0
or version 2 chunk: The first one has a fixed payload size of _CHUNK_SIZE_ - 2, the
second one a maximum payload size of _CHUNK_SIZE_ - 4. In this case, a version 1
chunk can do the job with a MSZE value of 1, meaning the actual payload size is the
maximum payload size - 1, which is equal to _CHUNK_SIZE_ - 3.

The minimum payload size for version 1 chunks is _CHUNK_SIZE_ - 257.


### Version 2

A version 2 chunk contains a header starting with a verson byte of 0x01 and a series of
variable size _control blocks_, terminated by the CEND marker (0x00). The the payload size
and the payload content follow. If the payload does not take the whole size of the chunk,
it is filled up with zeros. If the chunk does not contain a CEND marker, the payload is
empty by definition.


                                         Header||
                Version||ControlBlock* ||*|CEND||Payload
                  +----++----+----+----++ +----++----+----+----+
    Size in Bytes |   1||   1|   2|CSZE||*|   1||   2|PSZE|   *|
                  +----++----+----+----++ +----++----+----+----+
          Meaning |0x02||TYPE|SZE+|CONT||*|0x00||PSZE|PYLD|0x00|
                  +----++----+----+----++ +----||----+----+----+

Each _control block_ has the following fields:
* TYPE: The type of _control block_. Must not be 0x00. For possible types see below. In the future,
  more types of _control blocks_ may be defined for the version 2 chunk header.
* SZE+: This field has two sub fields:
  * CSZE: Bits 0-11: The size of the _control block_ content (CONT), in bytes.
  * CPLS: Bits 12-15: Can be used to store additional info defined by the block type. 
* CONT: The content of the _control block_, length as specified by CSZE

#### Public Key Control Block (0x01)

* Type ID: 0x01
* CPLS: The cipher algorithm of the public key. The following values are defined (may be
  extended later):
  * 0x000: RSA2048 
* Multiplicity: 0..1
* Contains the public key used to create a signature. May refer to a _signature control block_
  (0x01) or to something completely different.
  
#### Referenced Chunk Control Block (0x02)

* Type ID: 0x04
* CPLS: Unused 
* Multiplicity: 0..*
* Contains a reference to another data chunk. The own payload of the referencing chunk plus the
  combined payloads of all directly and indirectly referenced chunks (in depth-first order) is
  called the _aggregated payload_.
  * The payload of the referencing chunk is the first part, the _aggregated payloads_ of the
    referenced chunks are added in the order of occurance of their _referenced chunk control
    blocks_.
  * To prevent infinitely large _aggregated chunks_, it is not allowed to create reference
    cycles. Chunks containing reference cycles shall be regarded as corrupt and be ignored.
  * Only untimed data chunks can be referenced, i.e. no data chunks being list items.

#### Redundancy Chunk Control Block (0x03)

* Type ID: 0x05
* CPLS: Unused
* Multiplicity: 0..*, only once after a Referenced Chunk Control Block
* Contains a reference to a redundant data chunk, allowing to reconstruct any
  other of the referenced chunks, if n-1 chunks referenced before this block are
  available. To achieve this, the redundant data chunk contains the bitwise xor
  combination of all referenced chunks. To also cover the control blocks of referenced
  chunks, the redundancy chunk is an unversioned chunk, which uses all bytes for the
  xor combination.
  
  A chunk may contain more than one Redundancy Chunk Control Block. Each of these
  blocks refers to the references before it, either to the beginning of the chunk
  or until the previous Redundancy Chunk Control Block.
  
### Version 3

A version 3 chunk contains a symmetric content key and a payload.

                                Header||
                Version|   Content Key||Payload
                  +----+----+----+----++----+
    Size in Bytes |   1|   1|   2|KYZE||   *|
                  +----+----+----+----++----+
          Meaning |0x03|CALG|KYSZ|CKEY||PYLD|
                  +----+----+----+----++----+

Following the version byte, it has the following fields:
* CALG: The symmetric cipher algorithm used for encryption. The following values are defined:
  * 0x00: AES256
  * More algorithms may be added in the future.
* KYSZ: The size of the following asymmetric key in bytes.
* CKEY: The asymmetric content key, normally by itself encrypted, e.g. with the public part of an
  asymmetric key pair.
* PYLD: The payload of the chunk, encrypted with CKEY.

The recepient must know how to decrypt the content key. The payload starts with the next byte
after the key and is encrypted using that content key. The payload will usually (but not
necessarily) contain the largest fitting encrypted chunk, so that it fits into the size of
_FULL_PAYLOAD_SIZE_ = _CHUNK_SIZE_ - _KYSZ_ - 4. The _BLOCK_SIZE_ of the ciper algorithm may cause
that the effective payload size is _FULL_PAYLOAD_SIZE_ - _FULL_PAYLOAD_SIZE_ % _BLOCK_SIZE_.
  
### Version 4

A version 4 chunk contains a digital signature and the signed payload.

                                          Header||
                Version|               Hash Code||Payload
                  +----+----+----+----+----+----++----+
    Size in Bytes |   1|   1|   1|   1|   2|KYZE||   *|
                  +----+----+----+----+----+----++----+
          Meaning |0x03|SMOD|SHAL|SCAL|SGSZ|SIGN||PYLD|
                  +----+----+----+----+----+----++----+

Following the version byte, it has the following fields:
* SMOD: The mode for verifying the signature:
  * 0x0: Signature is verifyable with a public key which must be known otherwise to the recepient
  * 0x1: Signature is verifyable with public key extracted from chunk key
  * 0x2: Signature is verifyable with a key which can be extracted from the payload (e.g.
    from an embedded version 2 chunk's _public key control block_ (0x01)
  * More signature types may be added in the future.
* SHAL: The hash algorithm used for signing. The following values are defined:
  * 0x00: SHA512
  * More algorithms may be added in the future.
* SCAL: The asymmetric cipher algorithm used for signing. The following values are defined:
  * 0x00: RSA2048
  * More algorithms may be added in the future.
* SGSZ: The size of the following digital signature in bytes.
* SIGN: The digital siganture of the payload.
* PYLD: The payload of the chunk.

The payload starts with the next byte after the signature. The payload has a size of
_FULL_PAYLOAD_SIZE_ = _CHUNK_SIZE_ - _SGSZ_ - 6. It may contain a sub chunk of that size.