import Shared
import SwiftUI

struct ColumnDropDelegate: DropDelegate {
    let columnId: KanbanColumnType
    @Binding var draggedTaskId: String?
    @Binding var isTargeted: Bool
    let onMove: (String, KanbanColumnType) -> Void

    func dropEntered(info: DropInfo) {
        withAnimation(DS.Animation.quick) {
            isTargeted = true
        }
    }

    func dropExited(info: DropInfo) {
        withAnimation(DS.Animation.quick) {
            isTargeted = false
        }
    }

    func performDrop(info: DropInfo) -> Bool {
        guard let draggedTaskId else { return false }
        onMove(draggedTaskId, columnId)
        self.draggedTaskId = nil
        isTargeted = false
        return true
    }

    func dropUpdated(info: DropInfo) -> DropProposal? {
        DropProposal(operation: .move)
    }
}
