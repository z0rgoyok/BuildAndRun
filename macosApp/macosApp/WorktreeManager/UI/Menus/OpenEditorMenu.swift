import SwiftUI

struct OpenEditorMenu: View {
    @ObservedObject var root: KmpRoot
    let worktreePath: String

    var body: some View {
        Menu {
            Button("Open in Editor") {}
                .disabled(true)
            Button("Configure Editors...") {
                root.presentSheet(.configureEditors)
            }
            .disabled(true)
        } label: {
            Label("Open", systemImage: "arrow.up.forward.app")
        } primaryAction: {
            _ = worktreePath
        }
        .disabled(true)
    }
}
