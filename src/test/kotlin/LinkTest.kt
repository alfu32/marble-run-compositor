import eu.ec.oib.training.alferio.Link
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*

class LinkTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
    fun testLinkFromString(){
        val ln = Link.fromString("")
    }
}