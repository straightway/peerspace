Peerspace Searching

# Peerspace Searching
It may be possible to set up an announce and search service using Peerspace _open lists_:

- Each user may announce information under a certain _search specification_ by posting it to an _open list_ being specific for that query. We call this list the _announcement list_ of that search.
- The id of that _announcement list_ is the hash code of the according _search specification_.
- It contains all results for that search, that have been announced by users. Whether these results are good or bad, right or wrong, depends on what the users post here.
- The result data chunks are symmetrically encrypted with the hash code of a defined variation of the _search specification_ as encryption key (e.g with a fixed additional prefix). This makes it possible for anyone querying the _announcement list_ to decrypt the result, because he knows the _search specification_. But peers storing the result cannot know the content, because there is no way to directly compute the decryption key from the id.

