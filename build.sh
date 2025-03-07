echo "cleanup"
rm -rf transformers/*.jar

echo "rebuilding the process executor"
./gradlew clean shadowJar

submodules=("transformer-base64encode" "transformer-identity" "transformer-base64decode" "worker-null")

for submodule in "${submodules[@]}";do
  echo "rebuilding $submodule"
  ./gradlew ":$submodule:build"
done
