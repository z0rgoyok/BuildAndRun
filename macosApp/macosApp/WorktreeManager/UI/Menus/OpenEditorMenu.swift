import Shared
import SwiftUI

struct OpenEditorMenu: View {
    @ObservedObject var root: KmpRoot
    let worktreePath: String

    private var labels: KanbanLabels { root.store.kanbanLabels }

    private var configuredEditors: [EditorItem] {
        root.editorsState.editors.filter { $0.isInstalled && $0.isEnabled }
    }

    private var hasRememberedEditor: Bool {
        root.editorsState.rememberEditorChoice && root.editorsState.preferredEditorId != nil
    }

    private var selectedEditorId: String {
        root.editorsState.preferredEditorId ?? ""
    }

    var body: some View {
        if hasRememberedEditor {
            menuWithPrimaryAction
        } else {
            menuWithoutPrimaryAction
        }
    }

    private var menuWithPrimaryAction: some View {
        Menu {
            menuContent
        } label: {
            Label(labels.open, systemImage: "arrow.up.forward.app")
        } primaryAction: {
            root.store.editors.onOpenInEditor(
                worktreePath: worktreePath,
                editorId: root.editorsState.preferredEditorId,
            )
        }
        .disabled(configuredEditors.isEmpty)
    }

    private var menuWithoutPrimaryAction: some View {
        Menu {
            menuContent
        } label: {
            Label(labels.open, systemImage: "arrow.up.forward.app")
        }
        .disabled(configuredEditors.isEmpty)
    }

    @ViewBuilder
    private var menuContent: some View {
        if configuredEditors.isEmpty {
            Text(labels.noConfiguredEditors)
                .foregroundStyle(.secondary)
        } else {
            Picker(
                "",
                selection: Binding(
                    get: { selectedEditorId },
                    set: { newId in
                        root.store.editors.onSetPreferredEditor(editorId: newId)
                        root.store.editors.onOpenInEditor(
                            worktreePath: worktreePath,
                            editorId: newId,
                        )
                    }
                )
            ) {
                ForEach(configuredEditors, id: \.id) { editor in
                    Text(editor.name).tag(editor.id)
                }
            }
            .pickerStyle(.inline)
            .labelsHidden()
        }

        Divider()

        Button(root.editorsState.rememberEditorChoice ? labels.forgetEditorChoice : labels.rememberEditorChoice) {
            root.store.editors.onSetRememberEditorChoice(value: !root.editorsState.rememberEditorChoice)
            if root.editorsState.rememberEditorChoice {
                root.store.editors.onSetPreferredEditor(editorId: nil)
            }
        }

        Button(labels.configureEditors) {
            root.presentSheet(.configureEditors)
        }
    }
}
