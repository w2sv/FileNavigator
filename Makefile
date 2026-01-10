SHELL=/bin/bash

VERSION := $(shell grep -Po '^version=\K.*' gradle.properties)

clean:
	@echo "Clean"
	@./gradlew clean

format:
	@./gradlew ktlintFormat

update-dependencies:
	@./gradlew versionCatalogUpdate

update-gradle:
	@./gradlew wrapper --gradle-version latest

# ==============
# Building
# ==============

compose-compiler-reports:
	@echo ./gradlew assembleRelease -PcomposeCompilerReports=true

generate-proto:
	@./gradlew generateDebugProto

baseline-profile:
	@echo "Generate baseline profile"
	@./gradlew :app:generateReleaseBaselineProfile --console verbose

build-aab:
	@echo "Build AAB"
	@./gradlew :app:bundleRelease --console verbose

build-apk:
	@echo "Build APK"
	@./gradlew assembleRelease --console verbose

run-with-navigator-start:
	@./gradlew installDebug
	@adb shell am force-stop com.w2sv.filenavigator
	@adb shell am start -n com.w2sv.filenavigator/.ui.MainActivity -a com.w2sv.filenavigator.START_NAVIGATOR

# ==============
# Publishing
# ==============

build-and-publish-to-test-track:
	@$(MAKE) clean  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@$(MAKE) build-aab

	@echo "Publish Bundle"
	@./gradlew publishBundle --track internal --console verbose

build-and-publish-bundle:
	@$(MAKE) clean  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@$(MAKE) build-aab

	@echo "Publish Bundle"
	@./gradlew publishBundle --track production --console verbose

publish-listing:
	@./gradlew publishListing  --console verbose
