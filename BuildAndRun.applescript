-- Fast Build & Run launcher for BuildAndRun (KMP macOS app)
-- Default behavior: build Debug incrementally and only when inputs changed.

on sh(cmd)
	return do shell script cmd
end sh

set projectPath to (POSIX path of (path to me as text)) & "../"
set projectPath to sh("cd " & quoted form of projectPath & " && pwd")
set projectPathQuoted to quoted form of projectPath

set xcodeProjectPath to projectPath & "/macosApp/macosApp.xcodeproj"
set schemeName to "macosApp"

try
	set buildConfiguration to sh("echo ${BUILDANDRUN_BUILD_CONFIG:-Debug}")
	if (buildConfiguration is not "Debug") and (buildConfiguration is not "Release") then
		set buildConfiguration to "Debug"
	end if

	set forceRebuild to sh("echo ${BUILDANDRUN_FORCE_REBUILD:-0}")

	set derivedDataPath to sh("xcodebuild -project " & quoted form of xcodeProjectPath & " -scheme " & schemeName & " -configuration " & buildConfiguration & " -showBuildSettings 2>/dev/null | grep ' TARGET_BUILD_DIR = ' | sed 's/.*= //'")
	set appPath to derivedDataPath & "/macosApp.app"

	set buildNeeded to false
	if forceRebuild is "1" then
		set buildNeeded to true
	else
		try
			sh("test -d " & quoted form of appPath)
		on error
			set buildNeeded to true
		end try

		if buildNeeded is false then
			set needsBuildResult to sh("cd " & projectPathQuoted & " && " & ¬
				"app_mtime=$(stat -f %m " & quoted form of appPath & "/Contents/MacOS/macosApp 2>/dev/null || echo 0); " & ¬
				"kotlin_mtime=$(find shared/src -type f \\( -name '*.kt' -o -name '*.xml' \\) -print0 2>/dev/null | xargs -0 stat -f %m 2>/dev/null | sort -nr | head -1 || echo 0); " & ¬
				"swift_mtime=$(find macosApp/macosApp -type f -name '*.swift' -print0 2>/dev/null | xargs -0 stat -f %m 2>/dev/null | sort -nr | head -1 || echo 0); " & ¬
				"kotlin_mtime=${kotlin_mtime:-0}; swift_mtime=${swift_mtime:-0}; " & ¬
				"if [ \"$kotlin_mtime\" -gt \"$app_mtime\" ] || [ \"$swift_mtime\" -gt \"$app_mtime\" ]; then echo 1; else echo 0; fi")
			if needsBuildResult is "1" then set buildNeeded to true
		end if
	end if

	if buildNeeded then
		sh("cd " & projectPathQuoted & " && xcodebuild -project macosApp/macosApp.xcodeproj -scheme " & schemeName & " -configuration " & buildConfiguration & " -destination 'platform=macOS' build 2>&1")
	end if

	sh("open " & quoted form of appPath)

on error errMsg
	display alert "Build Failed" message errMsg as critical
end try
