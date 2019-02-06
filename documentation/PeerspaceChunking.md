# Peerspace Chunking

In peerspace, data on the [network layer](PeerspaceSoftwareLayers.md#Network Layer)
is is transmitted in data chunks of fixed size. It is the responisbility of
the [transport layer](PeerspaceSoftwareLayers.md#Transport Layer) to cut the data
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

### Version 0

A version 0 chunk header only consists of the first version byte, which must be 0x00, the
complete rest of the chunk is the payload.

                 Header||
                Version||Payload
                  +----++----+
    Size in Bytes |   1||   *|
                  +----++----+
          Meaning |0x00||PYLD|
                  +----++----+

### Version 1

A version 1 chunk contains a header starting with a verson byte of 0x01 and a series of
variable size _control blocks_, terminated by the CEND marker (0x00). The the payload follows
and takes the rest of the chunk. 

                                         Header||
                Version||ControlBlock* ||*|CEND||Payload
                  +----++----+----+----++ +----++----+
    Size in Bytes |   1||   1|   2|SIZE||*|   1||   *|
                  +----++----+----+----++ +----++----+
          Meaning |0x01||TYPE|SIZE|CONT||*|0x00||PYLD|
                  +----++----+----+----++ +----||----+

Each _control block_ has the following format:
* TYPE: The type of _control block_. Must not be 0x00. For possible types see below. In the future,
  more types of _control blocks_ may be defined for the version 1 chunk header.
* SIZE: The size of the _control block_ content (CONT), in bytes
* CONT: The content of the _control block_, length as specified by SIZE

#### Signature Control Block (0x01)

* Type ID: 0x01
* Multiplicity: 0..1
* Contains a digital signature for the chunk. All data immediately after this block is signed.
  The signature may have been created from
  * the private part belonging to the _public key control block_ (0x02)
  * a key pair who's public part is encoded in the chunk key
  * any other key, which then must be known to the recepient in order to verify the signature 

**Content Format**

         Size of the ID|Referenced chunk ID
                  +----+----+
    Size in Bytes |   1|   *|
                  +----+----+
          Meaning |STPE|SGNE|
                  +----+----+

* STPE: The signature type. Defines how the signature can be verified.
  * 0x00: Signature is verifyable with a public key which must be known otherwise to the recepient
  * 0x01: Signature is verifyable with public key extracted from chunk key
  * 0x02: Signature is verifyable with the key stored in the _public key control block_ (0x02)
  * More signature types may be added in the future.

#### Public Key Control Block (0x02)

* Type ID: 0x02
* Multiplicity: 0..1
* Contains the public key used to create a signature. May refer to a _signature control block_
  (0x01) or to something completely different.
  
#### Content Key Control Block (0x03)

* Type ID: 0x03
* Multiplicity: 0..1
* Contains a symmetric content key which is by itself encrypted, normally with the public part
  of an asymmetric key pair. The recepient must know how to decrypt the content key. All data
  immediately after this block is encrypted using the content key, including any following _control
  blocks_.
  
#### Referenced Chunk Control Block (0x04)

* Type ID: 0x04
* Multiplicity: 0..*
* Contains a reference to another data chunk.
  * The _agregated payload_ of the referencing chunk is the concatenation of the payloads
    of all referenced chunks, in the order they are defined, plus the own payload of the
    referencing chunk.
  * This also recursively applies to referenced chunks which in turn reference other chunks.
  * To prevent infinitely large _aggregated chunks_, it is not allowed to create reference
    cycles. Chunks containing reference cycles shall be regarded as corrupt and be ignored.
  * Only untimed data chunks can be referenced, i.e. no data chunks being list items.