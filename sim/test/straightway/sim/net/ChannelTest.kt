package straightway.sim.net

/*
class ChannelTest : TestBase<ChannelTest.Environment>() {

    inner class Environment() {
        val simulator = Simulator()
        val network = Network(simulator, latency = 50[milli(second)])
        var sender = createSender(100[byte / second])
        var receiver = createReceiver(100[byte / second])
        var message = createMessage(100[byte])

        fun assertProperMessageReceiveTime(bandwidth: UnitNumber<Bandwidth>) {
            val actualReceiveTime = receiver.receiveProtocol.single().time
            val transmissionDuration = (message.size / bandwidth)[second]
            val expectedReceiveTime = startTime + network.latency + transmissionDuration
            expect(actualReceiveTime _is equalWithin(1[micro(second)].toDuration()) _to expectedReceiveTime)
        }
    }

    @BeforeEach
    fun setup() {
        sut = Environment()
    }

    @Test
    fun send_message() {
        sut.run {
            network.send(sender, receiver, message)
            simulator.run()
            assertProperMessageReceiveTime(sut.sender.uploadBandwidth)
        }
    }

    @Test
    fun send_messageOfDifferentSize() {
        sut.run {
            message = createMessage(200[byte])
            network.send(sender, receiver, message)
            simulator.run()
            assertProperMessageReceiveTime(sut.sender.uploadBandwidth)
        }
    }

    @Test
    fun send_lowerUploadBandwidth() {
        sut.run {
            sender = createSender(sut.receiver.downloadBandwidth / 2)
            network.send(sender, receiver, message)
            simulator.run()
            assertProperMessageReceiveTime(sut.sender.uploadBandwidth)
        }
    }

    @Test
    fun send_lowerDownloadBandwidth() {
        sut.run {
            sender = createSender(sut.receiver.downloadBandwidth * 2)
            network.send(sender, receiver, message)
            simulator.run()
            assertProperMessageReceiveTime(sut.receiver.downloadBandwidth)
        }
    }

    data class ReceiveProtocolEntry(val time: LocalDateTime, val sender: Client, val message: Message)
    inner class ClientMockWithReceiveProtocol(
        private val _uploadBandwidth: UnitValue<Int, Bandwidth>?,
        private val _downloadBandwidth: UnitValue<Int, Bandwidth>?) : Client {

        override val uploadBandwidth get() = _uploadBandwidth!!
        override val downloadBandwidth get() = _downloadBandwidth!!
        val receiveProtocol: List<ReceiveProtocolEntry> get() = _receiveProtocol

        override fun receive(sender: Client, message: Message) {
            _receiveProtocol += ReceiveProtocolEntry(sut.simulator.currentTime, sender, message)
        }

        val _receiveProtocol = mutableListOf<ReceiveProtocolEntry>()
    }

    private fun createSender(uploadBandwidth: UnitValue<Int, Bandwidth>) =
        ClientMockWithReceiveProtocol(uploadBandwidth, null)

    private fun createReceiver(downloadBandwidth: UnitValue<Int, Bandwidth>) =
        ClientMockWithReceiveProtocol(null, downloadBandwidth)

    private companion object {
        val startTime = Simulator().currentTime
        fun createMessage(size: UnitValue<Int, AmountOfData>) =
            Message("Message(size=$size Bytes, ID=${UUID.randomUUID()}", size)
    }
}
*/
