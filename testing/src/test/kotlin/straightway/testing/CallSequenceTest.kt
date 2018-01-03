package straightway.testing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CallSequenceTest {
    @Test
    fun test_verifyActionExecutionOrder() {
        val sut = CallSequence(0, 2, 1)
        sut.actions[0]()
        sut.actions[2]()
        sut.actions[1]()
        sut.assertCompleted()
    }

    @Test
    fun test_getActionsInProperOrder() {
        val sut = CallSequence(0, 2, 1)
        for (action in sut.orderedActions) {
            action()
        }
        sut.assertCompleted()
    }

    @Test
    fun test_toString() {
        val sut = CallSequence(0, 2, 1)
        assertEquals("CallSequence(0, 2, 1)", sut.toString())
    }
}