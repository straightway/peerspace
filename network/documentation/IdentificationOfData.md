# Identification of data

Each chunk of data is identified by a key which either is

- the content hash code of the data chunk, or
- a list or mutable chunk id together with the timestamp of the creation of the data chunk.

Possible issues:
- [Overriding of list chunks](../issues/OverridingOfListChunks.md)
- [Past timestamps may be faked](../issues/FakePastTimestamps.md)
