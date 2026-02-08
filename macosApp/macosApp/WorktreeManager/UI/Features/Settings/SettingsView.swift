import SwiftUI

struct SettingsView: View {
    @State private var worktreeBasePath: String = ""

    var body: some View {
        GeneralSettingsView(worktreeBasePath: $worktreeBasePath)
        .frame(width: 450, height: 350)
    }
}

#Preview {
    SettingsView()
}
