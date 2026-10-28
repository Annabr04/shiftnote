package dev.annabrackat.shiftnote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DomainTest {
    @Test
    fun cannotSubmitWhileARequiredItemIsOpen() {
        val shift = seedShift()
        assertEquals("AB", shift.initials)
        assertTrue(shift.initials.length in 2..3)
        assertEquals(Phase.OPEN, shift.phase)
        assertEquals(5, shift.items.size)
        assertEquals(3, shift.items.count { it.required })
        assertTrue(shift.items.none { it.done || it.note.isNotEmpty() })

        val blocked = submit(shift, Role.TECH)
        assertEquals("required", (blocked as DomainResult.Err).code)
        assertEquals(Phase.OPEN, shift.phase)

        val almost = shift.items.filter { it.required }.dropLast(1).fold(shift) { current, item ->
            (toggleItem(current, item.id) as DomainResult.Ok<Shift>).value
        }
        assertTrue(almost.items.any { it.required && !it.done })
        val stillOpen = submit(almost, Role.TECH)
        assertEquals("required", (stillOpen as DomainResult.Err).code)
        assertEquals(Phase.OPEN, almost.phase)
    }

    @Test
    fun optionalItemsDoNotBlockSubmit() {
        val ready = withRequiredDone()
        assertTrue(ready.items.any { !it.required && !it.done })
        val result = submit(ready, Role.TECH)
        assertIs<DomainResult.Ok<Shift>>(result)
        assertEquals(Phase.PENDING_ACK, result.value.phase)
        assertTrue(result.value.items.filter { !it.required }.none { it.done })
    }

    @Test
    fun techCannotAcknowledge() {
        val pending = pendingShift()
        val result = acknowledge(pending, Role.TECH)
        assertEquals("role", (result as DomainResult.Err).code)
        assertEquals(Phase.PENDING_ACK, pending.phase)
    }

    @Test
    fun pharmacistCannotSubmit() {
        val ready = withRequiredDone()
        val result = submit(ready, Role.PHARMACIST)
        assertEquals("role", (result as DomainResult.Err).code)
        assertEquals(Phase.OPEN, ready.phase)
    }

    @Test
    fun noteRejectedOver80Characters() {
        val shift = (toggleItem(seedShift(), "fridge") as DomainResult.Ok<Shift>).value
        val tooLong = setNote(shift, "fridge", "a".repeat(81))
        assertEquals("note", (tooLong as DomainResult.Err).code)
        assertEquals("", shift.items.first { it.id == "fridge" }.note)

        val exact = setNote(shift, "fridge", "a".repeat(80)) as DomainResult.Ok<Shift>
        assertEquals(80, exact.value.items.first { it.id == "fridge" }.note.length)

        val blank = setNote(exact.value, "fridge", "") as DomainResult.Ok<Shift>
        assertEquals("", blank.value.items.first { it.id == "fridge" }.note)
        assertTrue(blank.value.items.first { it.id == "fridge" }.done)
    }

    @Test
    fun noteRejectedIfTheItemIsNotDone() {
        val shift = seedShift()
        val result = setNote(shift, "waiting", "walked twice")
        assertIs<DomainResult.Err>(result)
        assertFalse(shift.items.first { it.id == "waiting" }.done)
        assertEquals("", shift.items.first { it.id == "waiting" }.note)
    }

    @Test
    fun closedShiftCannotReopen() {
        val closed = (acknowledge(pendingShift(), Role.PHARMACIST) as DomainResult.Ok<Shift>).value
        assertEquals(Phase.CLOSED, closed.phase)
        assertEquals("terminal", (reopen(closed, Role.PHARMACIST) as DomainResult.Err).code)
        assertEquals("terminal", (reopen(closed, Role.TECH) as DomainResult.Err).code)
        assertEquals(Phase.CLOSED, closed.phase)
    }

    @Test
    fun renderContainsTheInitialsAndMarksDoneItems() {
        val fresh = render(seedShift())
        assertTrue(fresh.contains("AB"))
        assertFalse(fresh.contains("[x]"))

        val opened = toggleItem(seedShift(), "fridge") as DomainResult.Ok<Shift>
        assertEquals(
            """
            AB
            OPEN
            [x] Fridge temp logged (required)
            [ ] Waiting bin walked (required)
            [ ] Control log signed (required)
            [ ] Return to stock filed
            [ ] Counter cleared
            """.trimIndent(),
            render(opened.value),
        )
    }

    @Test
    fun renderOfClosedShiftIncludesPhaseAndRequiredMarks() {
        val closed = (acknowledge(pendingShift(), Role.PHARMACIST) as DomainResult.Ok<Shift>).value
        assertEquals(Phase.CLOSED, closed.phase)
        val text = render(closed)
        assertTrue(text.startsWith("AB\nCLOSED\n"))
        assertTrue(text.contains("[x] Fridge temp logged (required)"))
        assertTrue(text.contains("[x] Waiting bin walked (required)"))
        assertTrue(text.contains("[x] Control log signed (required)"))
        assertFalse(text.contains("[ ] Waiting bin walked"))
    }

    @Test
    fun clearingDoneClearsTheNote() {
        val done = (toggleItem(seedShift(), "controls") as DomainResult.Ok<Shift>).value
        val noted = (setNote(done, "controls", "signed at close") as DomainResult.Ok<Shift>).value
        assertEquals("signed at close", noted.items.first { it.id == "controls" }.note)
        val cleared = (toggleItem(noted, "controls") as DomainResult.Ok<Shift>).value
        val item = cleared.items.first { it.id == "controls" }
        assertFalse(item.done)
        assertEquals("", item.note)
        val again = (toggleItem(cleared, "controls") as DomainResult.Ok<Shift>).value
        val redone = again.items.first { it.id == "controls" }
        assertTrue(redone.done)
        assertEquals("", redone.note)
    }

    @Test
    fun pharmacistCanReopenAPendingShift() {
        val pending = pendingShift()
        assertEquals("role", (reopen(pending, Role.TECH) as DomainResult.Err).code)
        assertEquals(Phase.PENDING_ACK, pending.phase)
        val opened = reopen(pending, Role.PHARMACIST) as DomainResult.Ok<Shift>
        assertEquals(Phase.OPEN, opened.value.phase)
        assertEquals(pending.items, opened.value.items)
    }

    private fun withRequiredDone(shift: Shift = seedShift()): Shift =
        shift.items.filter { it.required }.fold(shift) { current, item ->
            (toggleItem(current, item.id) as DomainResult.Ok<Shift>).value
        }

    private fun pendingShift(): Shift =
        (submit(withRequiredDone(), Role.TECH) as DomainResult.Ok<Shift>).value
}
