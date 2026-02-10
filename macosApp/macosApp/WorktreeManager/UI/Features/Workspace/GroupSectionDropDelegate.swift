import SwiftUI

struct GroupSectionDropDelegate: DropDelegate {
    let targetGroupId: String
    @Binding var draggedGroupId: String?
    let onReorder: (String, String) -> Void

    func dropEntered(info: DropInfo) {}

    func performDrop(info: DropInfo) -> Bool {
        guard let draggedGroupId, draggedGroupId != targetGroupId else { return false }
        onReorder(draggedGroupId, targetGroupId)
        self.draggedGroupId = nil
        return true
    }

    func dropUpdated(info: DropInfo) -> DropProposal? {
        DropProposal(operation: .move)
    }
}
