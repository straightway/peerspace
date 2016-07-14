#TODO
* [x] Peer announcment strategy
	* [x] Peers can get to know new peers by asking known peers about their peers
* [ ] Re-prioritze data after successful query
* [ ] Implement cryptography
	* [ ] Future-safe algorithms
	* [ ] Encrypt data chunks
* [ ] Implement network simulation
	* [ ] Event driven simulation
	* [ ] Which scenarios?
	* [ ] Measure from client perspective
		1. [ ] Answer times
		2. [ ] Availability of desired information
		3. [ ] Throughput
	* [ ] Scenarios with not well-behaving nodes
		* [ ] Denial of service
			* [ ] Push flooding
			* [ ] Query flooding
			* [ ] Anouncement flooding
			* [ ] Wrong routing data
* [ ] Optimize strategies
* [ ] Implement TOR network connection
	* [ ] Startup TOR
	* [ ] Launch TOR hidden service and listen for connections from other peers
	* [ ] Connect to other peers