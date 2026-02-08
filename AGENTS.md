Start your response with ✅5️⃣✅.

# Work Verification

To verify your work, run tests and IDEA inspections on the affected scope of changes.
You must fix (not hide) all errors and warnings.

# Code Style

Do not place more than one class at the top level of a file.
Keep files 350 lines or fewer.
Do not use stringly-typed keys/enums (no i18n keys, error codes, or enum values as String).
Localization: keep all UI strings in KMP shared string files (en/ru/uk); no platform-localized duplicates.
Do not write any documentation (no KDoc, no comments, no README files).
Don't use strings as enums. 
Max file length = 350 lines.

# Architecture

Use DDD and Clean Architecture. UI and presentation are separate layers.
Follow the fail-fast principle.

Each task must conclude with running tests and IDE inspections across the full affected scope. 
All issues and warnings must be fixed, not suppressed.
