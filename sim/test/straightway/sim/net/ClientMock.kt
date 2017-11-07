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
package straightway.sim.net

class ClientMock(val id: String, val log: LogList) : Client {
    override val uploadChannel: ChannelMock = ChannelMock(id + "_upload", log)
    override val downloadChannel: ChannelMock = ChannelMock(id + "_download", log)
    override fun receive(sender: Client, message: Message) {
        log.add("Receive ${message} from ${sender} to ${this}")
    }

    override fun toString() = id
}