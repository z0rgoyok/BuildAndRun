import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var root: KmpRoot

    var body: some View {
        TabView {
            GeneralSettingsView(
                worktreeBasePath:
                    Binding(
                        get: { root.state.worktreeBasePath },
                        set: { root.store.onSetWorktreeBasePath(path: $0) }
                    )
            )
            .tabItem {
                Label("General", systemImage: "gear")
            }

            CopyPatternsSettingsView(
                patterns:
                    Binding(
                        get: { root.state.defaultCopyPatterns },
                        set: { root.store.onSetDefaultCopyPatterns(patterns: $0) }
                    )
            )
            .tabItem {
                Label("Copy Files", systemImage: "doc.on.doc")
            }
        }
        .frame(width: 450, height: 350)
    }
}

#Preview {
    SettingsView()
}
