#TODO
* [x] Re-prioritze data after successful query
* [ ] Implement cryptography
	* [ ] Future-safe algorithms
	* [ ] Encrypt data chunks
* [ ] Implement network simulation
	* [x] Event driven simulation
	* [ ] Which scenarios?
	* [ ] Measure from client perspective
		1. [x] Answer times
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
	* [x] Connect to other peers