# Task C — prune non-UI Swift

## What was done
- Added controlled pruning switch in `macosApp/Configuration/Config.xcconfig`:
  - `PORT_SYNC_NON_UI_SWIFT_PRUNE=NO` (default, safe)
  - `PORT_SYNC_NON_UI_SWIFT_EXCLUDED_NAMES_YES` with patterns for:
    - `WorktreeManager/Application/**/*.swift`
    - `WorktreeManager/Domain/**/*.swift`
    - `WorktreeManager/Infrastructure/**/*.swift`
    - `WorktreeManager/Presentation/**/*.swift`
    - `WorktreeManager/DecomposeKit/**/*.swift`
  - `EXCLUDED_SOURCE_FILE_NAMES` now resolves through that switch.

## Coordination status
- Required input file `.coord/port-sync/results/task-B.md` is absent in repository.
- Because Task B result is unavailable, no destructive file removals/moves were performed.

## Validation
### Baseline build (prune OFF)
Command:
`xcodebuild -project macosApp/macosApp.xcodeproj -scheme macosApp -configuration Debug -sdk macosx CODE_SIGNING_ALLOWED=NO build`

Result: **PASS** (exit code 0)

### Prune simulation (prune ON)
Command:
`xcodebuild -project macosApp/macosApp.xcodeproj -scheme macosApp -configuration Debug -sdk macosx CODE_SIGNING_ALLOWED=NO PORT_SYNC_NON_UI_SWIFT_PRUNE=YES build`

Result: **FAIL** (exit code 65)

Representative compiler errors confirm UI bridge is not fully wired yet:
- `cannot find type 'SettingsComponent' in scope`
- `cannot find type 'CopyPattern' in scope`
- `cannot find type 'Repository' in scope`
- `cannot find type 'KanbanColumnType' in scope`
- `cannot find type 'PRStatus' in scope`

## Inspections
- IDEA inspection on changed file passed:
  - `macosApp/Configuration/Config.xcconfig` — no problems.

## Next step to finish pruning
1. Worker B delivers/commits UI bridge and `results/task-B.md`.
2. Set `PORT_SYNC_NON_UI_SWIFT_PRUNE=YES` and re-run xcodebuild validation.
3. After green build, perform destructive cleanup/move to `imported/` if still needed.

## Status ping (2026-02-08)
- Progress: pruning переключатель внедрён и проверен в сборке.
- Blocker: отсутствует `.coord/port-sync/results/task-B.md`, и при `PORT_SYNC_NON_UI_SWIFT_PRUNE=YES` UI всё ещё ссылается на Swift non-UI типы.
- Action taken: destructive удаления не выполнялись; оставлен безопасный режим по умолчанию (`PORT_SYNC_NON_UI_SWIFT_PRUNE=NO`).
