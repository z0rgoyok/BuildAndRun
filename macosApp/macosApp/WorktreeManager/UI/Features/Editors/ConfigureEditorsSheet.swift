import Shared
import SwiftUI

struct ConfigureEditorsSheet: View {
    @EnvironmentObject private var root: KmpRoot
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 0) {
            header
            content
        }
        .frame(width: 420, height: 520)
    }

    private var header: some View {
        HStack {
            Text("Configure Editors")
                .font(.headline)
            Spacer()
            Button("Done") {
                dismiss()
            }
            .keyboardShortcut(.defaultAction)
        }
        .padding()
        .background(Color(nsColor: .windowBackgroundColor))
    }

    private var content: some View {
        List {
            Section {
                ForEach(root.state.editors, id: \.id) { editor in
                    EditorRow(
                        editor: editor,
                        onToggle: { enabled in
                            root.store.onSetEditorEnabled(
                                editorId: editor.id,
                                enabled: enabled,
                            )
                        }
                    )
                }
            } footer: {
                Text("Disabled editors are hidden from Open menu.")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .listStyle(.inset)
    }
}

private struct EditorRow: View {
    let editor: AppStore.EditorItem
    let onToggle: (Bool) -> Void

    var body: some View {
        HStack {
            Image(systemName: editor.icon)
                .frame(width: 24)
                .foregroundStyle(editor.isInstalled ? .primary : .tertiary)

            VStack(alignment: .leading, spacing: 2) {
                Text(editor.name)
                    .foregroundStyle(editor.isInstalled ? .primary : .secondary)
                Text(editor.isInstalled ? "Installed" : "Not installed")
                    .font(.caption2)
                    .foregroundStyle(editor.isInstalled ? .green : .secondary)
            }

            Spacer()

            Toggle(
                "",
                isOn: Binding(
                    get: { editor.isEnabled },
                    set: { onToggle($0) }
                )
            )
            .labelsHidden()
        }
        .contentShape(Rectangle())
    }
}
