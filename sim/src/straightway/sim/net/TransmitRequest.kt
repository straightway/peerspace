package straightway.sim.net

import straightway.units.*

class TransmitRequest(val message: Message, val sender: Channel) {
    val receiver: Channel get() = _receiver!!
    infix fun to(receiver: Channel): TransmitRequest {
        this._receiver = receiver
        return this
    }

    val latency: UnitNumber<Time> get() = _latency
    infix fun withLatency(latency: UnitNumber<Time>): TransmitRequest {
        this._latency = latency
        return this
    }

    private var _receiver: Channel? = null
    private var _latency: UnitNumber<Time> = 0[second]
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
