package straightway.sim.net

class TransmitRequest(val message: Message, val sender: Channel) {
    val receiver: Channel get() = _receiver!!
    infix fun to(receiver: Channel): TransmitRequest {
        this._receiver = receiver
        return this
    }

    private var _receiver: Channel? = null
}

infix fun Message.from(sender: Channel) = TransmitRequest(this, sender)
fun transmit(request: TransmitRequest) = request.run {
    val sendOffer = sender.requestTransmission(request)
    val receiveOffer = receiver.requestTransmission(request)
    val slowerOffer = if (sendOffer.finishTime < receiveOffer.finishTime) receiveOffer else sendOffer

    sender.accept(slowerOffer)
    receiver.accept(slowerOffer)

    slowerOffer.finishTime
}
