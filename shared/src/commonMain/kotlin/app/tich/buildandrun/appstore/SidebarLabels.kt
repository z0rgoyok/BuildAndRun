package app.tich.buildandrun.appstore

import app.tich.buildandrun.presentation.i18n.UiText
import app.tich.buildandrun.resources.*

data class SidebarLabels(
    val archived: String,
    val addRepository: String,
    val expandAll: String,
    val collapseAll: String,
    val help: String,
    val showInFinder: String,
    val copyPath: String,
    val copyFilesSettings: String,
    val restoreProject: String,
    val archiveProject: String,
    val removeFromList: String,
)

internal fun buildSidebarLabels(): SidebarLabels =
    SidebarLabels(
        archived = resolveText(text = UiText(resource = Res.string.sidebar_archived)),
        addRepository = resolveText(text = UiText(resource = Res.string.sidebar_add_repository)),
        expandAll = resolveText(text = UiText(resource = Res.string.sidebar_expand_all)),
        collapseAll = resolveText(text = UiText(resource = Res.string.sidebar_collapse_all)),
        help = resolveText(text = UiText(resource = Res.string.sidebar_help)),
        showInFinder = resolveText(text = UiText(resource = Res.string.action_show_in_finder)),
        copyPath = resolveText(text = UiText(resource = Res.string.action_copy_path)),
        copyFilesSettings = resolveText(text = UiText(resource = Res.string.action_copy_files_settings)),
        restoreProject = resolveText(text = UiText(resource = Res.string.action_restore_project)),
        archiveProject = resolveText(text = UiText(resource = Res.string.action_archive_project)),
        removeFromList = resolveText(text = UiText(resource = Res.string.action_remove_from_list)),
    )
