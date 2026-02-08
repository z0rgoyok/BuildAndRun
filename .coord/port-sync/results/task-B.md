# Task B — UI bridge to KMP (partial report)

## Current progress
- Read constraints and ownership scope.
- Audited all files in `macosApp/macosApp/WorktreeManager/UI/**` and `macosApp/macosApp/WorktreeManager/App/**` for dependencies on Swift non-UI layers.
- Audited current KMP-exported API from `Shared.framework` to determine replaceable/non-replaceable paths.

## Blockers
1. `SharedMacOSAppStore` currently exposes only a subset of actions/state (add repository, select, create worktree, basic kanban), while UI/App in owned scope depends on many additional behaviors:
   - repository lifecycle: archive/restore/remove;
   - worktree lifecycle: lock/unlock/remove/prune/complete;
   - git actions: push/pull/merge/refresh status;
   - PR flow: create/open PR;
   - settings/editors/copy-patterns/full status cells and activity center integration.
2. These required APIs are currently provided by Swift non-UI components (`RootComponent`, `WorkspaceComponent`, `SettingsComponent`, `AppStore`, domain entities), and there is no equivalent complete bridge surface exported from `Shared` yet.
3. Per constraints, Kotlin files cannot be edited in this task, so missing bridge surface cannot be added from Worker B side.

## Needed to unblock
- Worker A needs to expose full parity bridge/state/actions in `Shared` (or confirm the final bridge API contract), including the operations listed above.

## Next step after unblock
- Replace `RootComponent`/`WorkspaceComponent`/`SettingsComponent` dependencies in owned UI/App files with Swift UI-only adapters over `Shared` types, preserving existing UI/UX structure.
