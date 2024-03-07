SHELL=/bin/bash

clean:
	@echo "Clean"
	@./gradlew clean

build-aab:
	@echo "Build AAB"
	@./gradlew :app:bundleRelease --console verbose

build-apk:
	@echo "Build APK"
	@./gradlew assembleRelease --console verbose

# ==============
# Publishing
# ==============

VERSION := $(shell grep -Po '^version=\K.*' gradle.properties)

build-and-publish-to-test-track:
	@echo -e "Retrieved Version: $(VERSION)\nHit enter to continue"
	@read

	@$(MAKE) clean  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@$(MAKE) build-aab

	@echo "Publish Bundle"
	@./gradlew publishBundle --track internal --console verbose

build-and-publish:
	@echo -e "Retrieved Version: $(VERSION)\n\n Hit enter if you have\n 1. Incremented the version\n 2. Updated the release notes\n\n Otherwise cancel target now."
	@read

	@./gradlew check
	@$(MAKE) clean  # Required as 'publishBundle' publishes all .aab's in specified archive dir

	@$(MAKE) build-aab
	@$(MAKE) build-apk

	@git add .; git commit -m "$(VERSION)"; git push;

	@$(MAKE) create-gh-release
	@$(MAKE) publish-bundle

create-gh-release:
	@echo "Create GitHub Release"
	@gh release create $(VERSION) app/build/outputs/apk/release/$(VERSION).apk -F app/src/main/play/release-notes/en-US/production.txt

publish-bundle:
	@echo "Publish Bundle"
	@./gradlew publishBundle --track production --console verbose

publish-listing:
	@./gradlew publishListing  --console verbose