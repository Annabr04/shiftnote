# Shiftnote

End-of-shift checklist for a retail pharmacy counter. A technician walks the list. A pharmacist acknowledges it. The rules live in a pure Kotlin module, so they can be tested without an emulator.

This is a training checklist, not a compliance record. It does not store patient names, dates of birth, addresses, or a drug list. It is not a prescription fill workflow and it is not a cycle count.

Anna Brackat. Items are the operational close: fridge temp, waiting bin, control log.

## Resume bullet

Built Shiftnote, an Android end-of-shift checklist in Kotlin and Jetpack Compose. Modeled technician submit, pharmacist acknowledgement, and a terminal closed phase in a pure JVM module, and unit-tested those rules on GitHub Actions without an emulator.

## What to run

Domain tests do not need the Android SDK or a device. JDK 17 is enough.

```bash
gradle :domain:test
```

GitHub Actions runs `gradle :domain:test --no-daemon` on every push and pull request. See [`.github/workflows/domain-tests.yml`](.github/workflows/domain-tests.yml). The workflow uses `actions/setup-java` with Temurin 17 and `gradle/actions/setup-gradle@v4` with Gradle 9.6.0.

Open the folder in current Android Studio and run the `app` configuration. Studio can install the Gradle 9.6 wrapper if it asks. The wrapper jar is not committed.

## Rules

Expected failures are a sealed `DomainResult`, not exceptions. The caller keeps the previous shift when the result is `Err`.

- `toggleItem` only while the phase is `OPEN`. It flips `done`. Clearing `done` also clears the note.
- `setNote` only while `OPEN`, and only if that item is done. A blank note is allowed. Longer than 80 characters fails with code `note`. A note on an item that is not done fails with code `state`.
- `submit` is `TECH` only, and only while `OPEN`. If any required item is not done, it fails with code `required` and the phase stays `OPEN`. Otherwise the phase becomes `PENDING_ACK`.
- `acknowledge` is `PHARMACIST` only, and only while `PENDING_ACK`. The phase becomes `CLOSED`.
- `reopen` lets a pharmacist send `PENDING_ACK` back to `OPEN`. A technician who tries that fails with code `role`. Nobody can reopen `CLOSED` (code `terminal`).
- `render` is plain text. It includes the initials, the phase, and each item as `[x]` or `[ ]`, with `(required)` on required items. No extra prose.

Initials are two or three letters. The seed shift uses initials `AB`, phase `OPEN`, five items, three of them required, and nothing done.

Wrong phase fails with code `phase`. The wrong role fails with code `role`.

## Layout

| Module | Responsibility |
| --- | --- |
| `domain` | Shift, items, phase, and role rules. No Android imports. |
| `app` | Jetpack Compose list, checkbox, note field, role toggle, Submit, Acknowledge, and Reopen. Shows the domain error. Does not re-check the required-item rule. |

## Stack

- Kotlin 2.2.10, JDK 17
- Android Gradle Plugin 9.4.0, Gradle 9.6, compileSdk 37, minSdk 26, targetSdk 36
- Jetpack Compose BOM 2026.09.00, activity-compose 1.13.0, lifecycle-viewmodel-compose 2.11.0
- Classic Android DSL, with `android.newDsl=false` and `android.builtInKotlin=false`
