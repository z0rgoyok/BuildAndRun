package app.tich.buildandrun.presentation.app.context.texts.impl

import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.presentation.i18n.UiTextLocalizer
import app.tich.buildandrun.resources.*

internal fun resolveBranchConflictMessageText(branch: String): String =
    UiTextLocalizer.resolve(UiText(resource = Res.string.branch_conflict_message, args = listOf(branch)))

internal fun resolveBranchConflictUseExistingDetailText(branch: String): String =
    UiTextLocalizer.resolve(UiText(resource = Res.string.branch_conflict_use_existing_detail, args = listOf(branch)))

internal fun resolveStatusToPushText(commits: String): String =
    UiTextLocalizer.resolve(UiText(resource = Res.string.detail_to_push, args = listOf(commits)))

internal fun resolveStatusBehindText(commits: String): String =
    UiTextLocalizer.resolve(UiText(resource = Res.string.detail_behind, args = listOf(commits)))

internal fun resolveNewTaskInText(column: String): String =
    UiTextLocalizer.resolve(UiText(resource = Res.string.detail_new_task_in, args = listOf(column)))
