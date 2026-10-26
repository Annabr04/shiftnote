package dev.annabrackat.shiftnote

enum class Role { TECH, PHARMACIST }

enum class Phase { OPEN, PENDING_ACK, CLOSED }

/**
 * One line on the close list. [note] is at most 80 characters.
 * The label is operational. It is not a patient, a drug, or a count.
 */
data class Item(
    val id: String,
    val label: String,
    val required: Boolean,
    val done: Boolean,
    val note: String,
)

/**
 * A counter close. [initials] are two or three letters.
 * This is a training checklist, not a compliance record.
 */
data class Shift(
    val id: String,
    val initials: String,
    val items: List<Item>,
    val phase: Phase,
)

sealed interface DomainResult<out T> {
    data class Ok<T>(val value: T) : DomainResult<T>
    data class Err(val code: String, val message: String) : DomainResult<Nothing>
}
