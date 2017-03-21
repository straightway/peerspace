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
package straightway.simulation

import java.time.Duration
import java.time.LocalDateTime

data class Event(val time: LocalDateTime, val action: Scheduler.() -> Unit)

/**
 * Schedules events for simulated time points and executes them.
 */
class Scheduler {

    var currentTime: LocalDateTime = LocalDateTime.of(0, 1, 1, 0, 0)
        get
        private set

    val eventQueue: List<Event> get() = _eventQueue

    fun schedule(duration: Duration, action: Scheduler.() -> Unit) {
        val newEvent = Event(currentTime.plus(duration), action)
        for (i in _eventQueue.indices) {
            if (newEvent.time < _eventQueue[i].time) {
                _eventQueue.add(i, newEvent)
                return
            }
        }

        _eventQueue.add(newEvent)
    }

    fun run() {
        while (!_eventQueue.isEmpty()) {
            val nextEvent = _eventQueue.first()
            _eventQueue.removeAt(0)
            currentTime = nextEvent.time
            val nextAxtion = nextEvent.action
            nextAxtion()
        }
    }

    //<editor-fold desc="Private data">
    private val _eventQueue: MutableList<Event> = mutableListOf()
    //</editor-fold>
}