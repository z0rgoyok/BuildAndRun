import Shared
import SwiftUI

struct WorktreePRBadge: View {
    let pr: PRStatus

    var body: some View {
        HStack(spacing: 3) {
            Image(systemName: prIcon)
            Text("PR #\(pr.number)")
        }
        .foregroundStyle(prColor)
    }

    private var prIcon: String {
        if pr.state === PRState.merged { return "checkmark.circle.fill" }
        if pr.state === PRState.closed { return "xmark.circle.fill" }
        return "arrow.triangle.pull"
    }

    private var prColor: Color {
        if pr.state === PRState.merged { return .purple }
        if pr.state === PRState.closed { return .red }
        return .green
    }
}
