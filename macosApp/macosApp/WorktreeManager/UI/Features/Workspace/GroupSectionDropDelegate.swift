import SwiftUI

struct GroupSectionDropDelegate: DropDelegate {
    enum DropPlacement {
        case before
        case after
    }

    let targetGroupId: String
    @Binding var draggedGroupId: String?
    @Binding var activeTargetGroupId: String?
    @Binding var activeDropPlacement: DropPlacement
    let onReorder: (String, String, DropPlacement) -> Void

    func dropEntered(info: DropInfo) {
        updateDropTarget(with: info)
    }

    func dropExited(info: DropInfo) {
        if activeTargetGroupId == targetGroupId {
            activeTargetGroupId = nil
        }
    }

    func performDrop(info: DropInfo) -> Bool {
        updateDropTarget(with: info)
        guard let draggedGroupId, draggedGroupId != targetGroupId else { return false }
        onReorder(draggedGroupId, targetGroupId, activeDropPlacement)
        self.draggedGroupId = nil
        activeTargetGroupId = nil
        return true
    }

    func dropUpdated(info: DropInfo) -> DropProposal? {
        updateDropTarget(with: info)
        return DropProposal(operation: .move)
    }

    private func updateDropTarget(with info: DropInfo) {
        activeTargetGroupId = targetGroupId
        activeDropPlacement = info.location.y < DS.Sizes.treeRowHeight / 2 ? .before : .after
    }
}
