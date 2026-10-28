package dev.annabrackat.shiftnote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.annabrackat.shiftnote.DomainResult
import dev.annabrackat.shiftnote.Phase
import dev.annabrackat.shiftnote.Role
import dev.annabrackat.shiftnote.Shift
import dev.annabrackat.shiftnote.acknowledge
import dev.annabrackat.shiftnote.reopen
import dev.annabrackat.shiftnote.render
import dev.annabrackat.shiftnote.seedShift
import dev.annabrackat.shiftnote.setNote
import dev.annabrackat.shiftnote.submit
import dev.annabrackat.shiftnote.toggleItem

class ShiftViewModel : ViewModel() {
    var role by mutableStateOf(Role.TECH)
        private set
    var shift by mutableStateOf(seedShift())
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun selectRole(next: Role) {
        role = next
        error = null
    }

    fun onToggle(itemId: String) = applyResult(toggleItem(shift, itemId))

    fun onNote(itemId: String, note: String) = applyResult(setNote(shift, itemId, note))

    fun onSubmit() = applyResult(submit(shift, role))

    fun onAcknowledge() = applyResult(acknowledge(shift, role))

    fun onReopen() = applyResult(reopen(shift, role))

    private fun applyResult(result: DomainResult<Shift>) {
        when (result) {
            is DomainResult.Ok -> {
                shift = result.value
                error = null
            }
            is DomainResult.Err -> error = result.message
        }
    }
}

@Composable
fun ShiftnoteApp(model: ShiftViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Shiftnote",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
        )
        Text(
            "${model.shift.initials} · ${model.shift.phase.name}",
            color = Navy,
            fontWeight = FontWeight.Medium,
        )
        Text(
            "Training checklist. Not a compliance record.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        RoleToggle(model.role, model::selectRole)
        model.error?.let { message ->
            Text(message, color = Danger, style = MaterialTheme.typography.bodyMedium)
        }
        if (model.shift.phase == Phase.CLOSED) {
            Text(
                render(model.shift),
                color = Ink,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Line, RoundedCornerShape(16.dp))
                    .background(Surface, RoundedCornerShape(16.dp))
                    .padding(12.dp),
            )
        } else {
            model.shift.items.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Line, RoundedCornerShape(16.dp))
                        .background(Surface, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.done,
                            onCheckedChange = { model.onToggle(item.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Navy,
                                uncheckedColor = Muted,
                            ),
                        )
                        Column {
                            Text(item.label, fontWeight = FontWeight.Medium, color = Ink)
                            if (item.required) {
                                Text(
                                    "Required",
                                    color = Navy,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = item.note,
                        onValueChange = { model.onNote(item.id, it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Note") },
                        singleLine = true,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = model::onSubmit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Surface),
                ) { Text("Submit") }
                Button(
                    onClick = model::onAcknowledge,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Surface),
                ) { Text("Acknowledge") }
            }
            TextButton(
                onClick = model::onReopen,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) { Text("Reopen", color = Navy) }
        }
    }
}

@Composable
private fun RoleToggle(role: Role, onSelect: (Role) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RoleChip("Tech", role == Role.TECH) { onSelect(Role.TECH) }
        RoleChip("Pharmacist", role == Role.PHARMACIST) { onSelect(Role.PHARMACIST) }
    }
}

@Composable
private fun RoleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Surface)
    } else {
        ButtonDefaults.buttonColors(containerColor = Surface, contentColor = Ink)
    }
    Button(onClick = onClick, colors = colors) { Text(label) }
}

private val Navy = Color(0xFF1B3A4B)
private val Paper = Color(0xFFF4EFE6)
private val Ink = Color(0xFF1A1814)
private val Surface = Color(0xFFFFFBF4)
private val Muted = Color(0xFF6E675E)
private val Line = Color(0xFFE4D9C8)
private val Danger = Color(0xFF8C2F2F)
