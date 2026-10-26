package dev.annabrackat.shiftnote

fun seedShift(): Shift = Shift(
    id = "ab-close",
    initials = "AB",
    phase = Phase.OPEN,
    items = listOf(
        Item("fridge", "Fridge temp logged", required = true, done = false, note = ""),
        Item("waiting", "Waiting bin walked", required = true, done = false, note = ""),
        Item("controls", "Control log signed", required = true, done = false, note = ""),
        Item("rts", "Return to stock filed", required = false, done = false, note = ""),
        Item("counter", "Counter cleared", required = false, done = false, note = ""),
    ),
)
