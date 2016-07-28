/****************************************************************************
   Copyright 2016 github.com/straightway

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
****************************************************************************/

// TODO Check if this is the right package for this class
package peer

import (
	"time"

	"github.com/straightway/straightway/general"
)

type Configuration struct {
	MaxConnections      int
	MaxChunkSize        uint64
	MaxAnnouncedPeers   int
	TimedQueryTimeout   time.Duration
	UntimedQueryTimeout time.Duration
}

func DefaultConfiguration() *Configuration {
	return &Configuration{
		MaxConnections:      20,
		MaxAnnouncedPeers:   50,
		MaxChunkSize:        0xffff,
		TimedQueryTimeout:   general.ParseDuration("1h"),
		UntimedQueryTimeout: general.ParseDuration("5m")}
}
