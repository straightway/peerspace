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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import straightway.dsl.*
import straightway.sim.core.*
import straightway.testing.*
import straightway.testing.flow.*
import straightway.units.*
import java.time.LocalDateTime

class AsyncSequentialChannelTest : TestBase<AsyncSequentialChannelTest.Environment>() {
    class Environment {
        fun channel(bandwidth: UnitValue<Int, Bandwidth>) =
            channels.getOrPut(bandwidth) { AsyncSequentialChannel(bandwidth) }

        private val channels = mutableMapOf<UnitValue<Int, Bandwidth>, Channel>()
    }

    @BeforeEach
    fun setup() {
        sut = Environment()
    }

    @Test
    fun receiverIsNoAsyncSequentialChannel_doesNotThrow() = sut.run {
        val otherChannel = ChannelMock("other", TimeLog(Simulator()))
        expect({ transmit(createMessage(100[bit]) from channel(10[bit / second]) to otherChannel) } does not - _throw - exception)
    }

    @Test
    fun senderIsNoAsyncSequentialChannel_doesNotThrow() = sut.run {
        val otherChannel = ChannelMock("other", TimeLog(Simulator()))
        expect({ transmit(createMessage(100[bit]) from otherChannel to channel(10[bit / second])) } does not - _throw - exception)
    }

    @Test
    fun lowerBandwidthDeterminesTransmissionTime() = sut.run {
        val time = transmit(createMessage(100[bit]) from channel(10[bit / second]) to channel(100[bit / second]))
        expect((time - ZERO_TIME) _is equal _to 10[second])
    }

    @Test
    fun secondTransmissionGoesOnTop_sameChannels_sameDirection() = sut.run {
        transmit(createMessage(100[bit]) from channel(10[bit / second]) to channel(100[bit / second]))
        val time = transmit(createMessage(100[bit]) from channel(10[bit / second]) to channel(100[bit / second]))
        expect((time - ZERO_TIME) _is equal _to 20[second])
    }

    @Test
    fun secondTransmissionGoesOnTop_sameChannels_oppositeDirection() = sut.run {
        transmit(createMessage(100[bit]) from channel(10[bit / second]) to channel(100[bit / second]))
        val time = transmit(createMessage(100[bit]) from channel(100[bit / second]) to channel(10[bit / second]))
        expect((time - ZERO_TIME) _is equal _to 20[second])
    }

    @Test
    fun secondTransmissionComesFirst_ifGapIsLargeEnough() = sut.run {
        transmit(createMessage(100[bit]) from channel(10[bit / second]) to channel(100[bit / second]))
        val time = transmit(createMessage(100[bit]) from channel(100[bit / second]) to channel(1000[bit / second]))
        expect((time - ZERO_TIME) _is equal _to 1[second])
    }

    @Disabled
    @Test
    fun secondTransmissionOverlapsFirstStransmission() = sut.run {

    }

    private companion object {
        val ZERO_TIME = LocalDateTime.of(0, 1, 1, 0, 0)
    }
}