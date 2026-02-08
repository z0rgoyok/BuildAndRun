import Shared
import SwiftUI

struct OpenEditorMenu: View {
    @ObservedObject var root: KmpRoot
    let worktreePath: String

    private var configuredEditors: [AppStore.EditorItem] {
        root.state.editors.filter { $0.isInstalled && $0.isEnabled }
    }

    private var hasRememberedEditor: Bool {
        root.state.rememberEditorChoice && root.state.preferredEditorId != nil
    }

    private var selectedEditorId: String {
        root.state.preferredEditorId ?? ""
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
            Label("Open", systemImage: "arrow.up.forward.app")
        } primaryAction: {
            root.store.onOpenInEditor(
                worktreePath: worktreePath,
                editorId: root.state.preferredEditorId,
            )
        }
        .disabled(configuredEditors.isEmpty)
    }

    private var menuWithoutPrimaryAction: some View {
        Menu {
            menuContent
        } label: {
            Label("Open", systemImage: "arrow.up.forward.app")
        }
        .disabled(configuredEditors.isEmpty)
    }

    @ViewBuilder
    private var menuContent: some View {
        if configuredEditors.isEmpty {
            Text("No configured editors")
                .foregroundStyle(.secondary)
        } else {
            Picker(
                "",
                selection: Binding(
                    get: { selectedEditorId },
                    set: { newId in
                        root.store.onSetPreferredEditor(editorId: newId)
                        root.store.onOpenInEditor(
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

        Button(root.state.rememberEditorChoice ? "Forget Editor Choice" : "Remember Editor Choice") {
            root.store.onSetRememberEditorChoice(value: !root.state.rememberEditorChoice)
            if root.state.rememberEditorChoice {
                root.store.onSetPreferredEditor(editorId: nil)
            }
        }

        Button("Configure Editors...") {
            root.presentSheet(.configureEditors)
        }
    }
}
