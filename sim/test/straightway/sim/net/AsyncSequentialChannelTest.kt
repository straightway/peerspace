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
import org.junit.jupiter.api.Test
import straightway.dsl.*
import straightway.sim.core.*
import straightway.testing.*
import straightway.testing.flow.*
import straightway.units.*

class AsyncSequentialChannelTest : TestBase<AsyncSequentialChannelTest.Environment>() {
    class Environment(val bandwidths: List<UnitValue<Int, Bandwidth>>) {
        constructor(vararg bandwidth: UnitValue<Int, Bandwidth>) : this(bandwidth.toList())

        val channels: List<AsyncSequentialChannel> by lazy { bandwidths.map { AsyncSequentialChannel(it) } }
        val message = createMessage(100[bit])
    }

    @BeforeEach
    fun setup() {
        sut = Environment(10[bit / second], 100[bit / second], 1000[bit / second])
    }

    @Test
    fun receiverIsNoAsyncSequentialChannel_doesNotThrow() {
        sut.run {
            val otherChannel = ChannelMock("other", TimeLog(Simulator()))
            expect({ transmit(message from channels[0] to otherChannel withLatency 0[second]) } does not - _throw - exception)
        }
    }

    @Test
    fun senderIsNoAsyncSequentialChannel_doesNotThrow() {
        sut.run {
            val otherChannel = ChannelMock("other", TimeLog(Simulator()))
            expect({ transmit(message from otherChannel to channels[0] withLatency 0[second]) } does not - _throw - exception)
        }
    }

    @Test
    fun lowerBandwidthDeterminesTransmissionTime() {
        sut.run {
            val time = transmit(message from channels[0] to channels[1] withLatency 0[second])
            expect(time _is equal _to 10[second])
        }
    }

    @Test
    fun secondTransmissionGoesOnTop_sameChannels_sameDirection() {
        sut.run {
            transmit(message from channels[0] to channels[1] withLatency 0[second])
            val time = transmit(message.from(channels[0]).to(channels[1]).withLatency(0[second]))
            expect(time _is equal _to 20[second])
        }
    }

    @Test
    fun secondTransmissionGoesOnTop_sameChannels_oppositeDirection() {
        sut.run {
            transmit(message from channels[1] to channels[0] withLatency 0[second])
            val time = transmit(message from channels[0] to channels[1] withLatency 0[second])
            expect(time _is equal _to 20[second])
        }
    }

    @Test
    fun fasterChannelIsFreeEarlier() {
        sut.run {
            transmit(message from channels[0] to channels[1] withLatency 0[second])
            val time = transmit(message from channels[2] to channels[1] withLatency 0[second])
            expect(time _is equal _to 2[second])
        }
    }
}