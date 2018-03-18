# Peerspace Requirements

## Participation of peers
**L001** For any computer connected to the internet, it shall be possible to participate in the Peerspace network as a peer by just running the software. No sign in is required.

## Privacy
**P001** It shall be highly improbable for any observer of a part of the Peerspace network to gather information about communications it does not take part in. This includes:

- **P002** Content
- **P003** Meta data

## Storage
**S001** It shall be possible for peers to store data within the Peerspace network.

- **S002** Data shall be stored anonymously.
- **S003** Data shall be stored encrypted. _Note: Thus the nodes generally cannot know which data they store. Any personalization of stored data shall be part of the encrypted data content and not generally visible._
- **S004** Data shall be stored only on the peer nodes. _Note: No central servers_
- Data shall be identified either by
  - **S005** content hash code, or by
  - **S006** list id. _Note: This identifies a list of multiple data items. This list may be extended by posting new data items under the same list id._

## Query
It shall be possible for peers to query the Peerspace network for stored data by:

  - **Q001** content hash, or
  - **Q002** list id, or
  - **Q003** list id and timestamp range. Either:
    - **Q004** all list items that have been created not earlier than a given timestamp.
    - **Q005** all list items that have been created not later than a given timestamp.
    - **Q006** all list items that have been created within a given time frame.
    - **Q007** the most recent item of a list.

**Q008** Performance: The first query reply shall arrive within 3 seconds in 95% of the cases if the peer is connected to the internet with at least 1 mbit/s upload and download rate.

##  Cleanup
**C001** Peers may refuse to store data.

**C002** Peers may delete data they stored to free space for new data.

_Note: As the peers do not know which data they store, data may silently disappear from the Peerspace network when the last peer storing the data deletes it._

## Robustness
The protocol driven by the peers shall be robust against:

- **R001**  manipulation of the transferred or stored data.
- **R002** flooding the network with useless data.
- **R003** peers refusing to follow that protocol.
- **R004** whether specific peers are online or offline.
