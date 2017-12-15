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
package straightway.integrationtest

import straightway.*
import straightway.data.*
import straightway.infrastructure.*

class NetworkClient(override val id: Id) : Identifyable, PushTarget {

    override fun receiveData(request: PushRequest) {
        _receivedData += request
    }

    val receivedData: List<PushRequest> get() = _receivedData
    private val _receivedData = mutableListOf<PushRequest>()
}