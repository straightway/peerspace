

# Messaging
Peerspace messaging is a layer on top of the Peerspace base. It allows users exchanging arbitrary messages.

## Channels
A user may establish a writable uni-directional communication _channel_ using a list. Everyone having access to the list's access token can write on that _channel_, so this token can be distributed to other users to grant write access.

The read access to that _channel_ is controlled by encryption of the content being sent on that _channel_. Everyone who has the decryption key has read access.

The following setups exist:
- _Shout setup_
  - Write few, read many
   - Channel with symmetric _content key_
   - Only the few writers have the list access token
   - Typical scenario: Blog, Social Network Feed
- _Whisper setup_
  - Write many, read few
  - Channel with asymmetric _content key_
  - Many writers have the public part of the _content key_ and the list access token. They cannot read the content of the list.
  - The readers have the private part of the _content key_ and can read the content of the list.
  - Typical scenario: Mail box

## Exchanging messages
To implement a spam-safe distributed message exchange service using Peerspace, each participant creates a _whisper channel_ to receive initial _contact requests_. This is called his _contact channel_.

A _contact request_ consists of a _shout channel_ used to send messages to the receiver (the _send channel_ of the participant issuing the _contact request_) and the sender's _contact channel_. The _contact request_ is answered by the receiver issuing a _reverse contract request_ back into the sender's _contact channel_, which announces the answer _send channel_ to the sender.

The _send channels_ are unidirectional and individual per communication peer, i.e.  having contact to _n_ other participants means having _n_ _send channels_, one for each of them.

The _send channel_ is owned by the sender, which is the only one having write permission on that channel. The receiver must actively query that channel to receive messages from the sender. This makes it easy for the receiver to block messages from certain senders: Simply stop querying those _send channels_. The only place where unwanted messages can be received is the _contact channel_. But since this may only contain contact requests, it is not very attractive for spam.

Example:
Alice creates a _contact channel_. Bob wants to initially contact Alice, so he posts a _contact request_ into Alice's _contact channel_. This _contact request_ contains Bob's _contact channel_ and his _send channel_ to Alice. When Alice receives the contact request, she issues the _reverse contact request_ into Bob's _contact channel_, containing Alice's _send channel_ to Bob. Now Alice and Bob start querying their mutual _send channels_ and can post messages to each other using their own _send channel_ for that contact.

## Social networking
It is possible to build up a social network with the usual features using the channels described above.

Each participant establishes a social media feed as a _shout channel_. Following this feed means getting the crypto key of that channel and querying it.

Groups may be established by using a _shout channel_ with write permission for each group member.

The following features can be realized by choosing the proper content of the feed messages:
- Posts: Just send the posted content with a unique id.
- Comments: Refer to the commented post or comment (by id) and publish it in the own social media feed, including a comment id.
- Like/Dislike: Like comments, with a special data format to represent the like our dislike.

Comments and likes/dislikes are not directly displayed in a participant's feed, but at the item they refer to. Since they are published through a participant's feed, they are only visible to those other participants following that feed.
