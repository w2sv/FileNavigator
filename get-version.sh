VERSION=$(./gradlew properties -q | grep "version:" | cut -d " " -f2)
echo "$VERSION"