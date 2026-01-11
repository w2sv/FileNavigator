SHELL=/bin/bash

VERSION := $(shell grep -Po '^version=\K.*' gradle.properties)

update-dependencies:
	@./gradlew versionCatalogUpdate

update-gradle:
	@./gradlew wrapper --gradle-version latest

# ==============
# Development
# ==============

format:
	@./gradlew ktlintFormat

compose-compiler-reports:
	@echo ./gradlew assembleRelease -PcomposeCompilerReports=true

generate-proto:
	@./gradlew generateDebugProto

generate-module-graph:
	@./gradlew generateModulesGraphvizText --no-configure-on-demand -Pmodules.graph.output.gv=all_modules
	@dot -Tsvg all_modules -o module-graph.svg

take-screenshot-and-expand-status-bar:
	@adb shell screencap -p /sdcard/Pictures/screenshot.png
	@adb shell cmd statusbar expand-notifications

run-with-navigator-start:
	@./gradlew installDebug
	@adb shell am force-stop com.w2sv.filenavigator
	@adb shell am start -n com.w2sv.filenavigator/.ui.MainActivity -a com.w2sv.filenavigator.START_NAVIGATOR

# ==============
# Building
# ==============

baseline-profile:
	@echo "Generate baseline profile"
	@./gradlew :app:generateReleaseBaselineProfile --console verbose

build-aab:
	@echo "Build AAB"
	@./gradlew :app:bundleRelease --console verbose

build-apk:
	@echo "Build APK"
	@./gradlew assembleRelease --console verbose

# ==============
# Publishing
# ==============

build-and-publish-to-test-track:
	@./gradlew clean  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@$(MAKE) build-aab

	@echo "Publish Bundle"
	@./gradlew publishBundle --track internal --console verbose

build-and-publish-bundle:
	@./gradlew clean  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@$(MAKE) build-aab

	@echo "Publish Bundle"
	@./gradlew publishBundle --track production --console verbose

publish-listing:
	@./gradlew publishListing  --console verbose
