package dev.annabrackat.shiftnote

const val NOTE_MAX_LENGTH = 80

fun toggleItem(shift: Shift, itemId: String): DomainResult<Shift> {
    if (shift.phase != Phase.OPEN) return phase(shift)
    return shift.mapItem(itemId) { item ->
        if (item.done) item.copy(done = false, note = "") else item.copy(done = true)
    }
}

fun setNote(shift: Shift, itemId: String, note: String): DomainResult<Shift> {
    if (shift.phase != Phase.OPEN) return phase(shift)
    if (note.length > NOTE_MAX_LENGTH) {
        return err("note", "A note cannot be longer than 80 characters.")
    }
    val current = shift.items.find { it.id == itemId } ?: return missing()
    if (!current.done) {
        return err("state", "A note can be added only after the item is done.")
    }
    return shift.mapItem(itemId) { it.copy(note = note) }
}

fun submit(shift: Shift, role: Role): DomainResult<Shift> {
    if (shift.phase != Phase.OPEN) return phase(shift)
    if (role != Role.TECH) {
        return err("role", "Only a technician can submit the checklist.")
    }
    if (shift.items.any { it.required && !it.done }) {
        return err("required", "Required items are still open.")
    }
    return DomainResult.Ok(shift.copy(phase = Phase.PENDING_ACK))
}

fun acknowledge(shift: Shift, role: Role): DomainResult<Shift> {
    if (shift.phase != Phase.PENDING_ACK) return phase(shift)
    if (role != Role.PHARMACIST) {
        return err("role", "Only a pharmacist can acknowledge the checklist.")
    }
    return DomainResult.Ok(shift.copy(phase = Phase.CLOSED))
}

fun reopen(shift: Shift, role: Role): DomainResult<Shift> {
    if (shift.phase == Phase.CLOSED) {
        return err("terminal", "A closed shift cannot be reopened.")
    }
    if (role != Role.PHARMACIST) {
        return err("role", "Only a pharmacist can reopen the checklist.")
    }
    if (shift.phase != Phase.PENDING_ACK) return phase(shift)
    return DomainResult.Ok(shift.copy(phase = Phase.OPEN))
}

fun render(shift: Shift): String = buildString {
    append(shift.initials)
    append('\n')
    append(shift.phase.name)
    shift.items.forEach { item ->
        append('\n')
        append(if (item.done) "[x] " else "[ ] ")
        append(item.label)
        if (item.required) append(" (required)")
    }
}

private fun phase(shift: Shift) = err(
    "phase",
    "The shift is ${shift.phase.name} and does not accept that action.",
)

private fun missing() = err("missing", "That item is not on this shift.")

private fun err(code: String, message: String) = DomainResult.Err(code, message)

private fun Shift.mapItem(itemId: String, block: (Item) -> Item): DomainResult<Shift> {
    val index = items.indexOfFirst { it.id == itemId }
    if (index < 0) return missing()
    val next = items.toMutableList()
    next[index] = block(items[index])
    return DomainResult.Ok(copy(items = next.toList()))
}
