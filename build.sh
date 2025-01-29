echo "cleanup"
rm -rf transformers/*.jar

echo "rebuilding the process executor"
./gradlew clean shadowJar

submodules=(
  #"transformer-base64encode"
  #"transformer-identity"
  #"transformer-base64decode"
  "workers-lib-std"
)

for submodule in "${submodules[@]}";do
  echo "==========================================="
  echo "=========== rebuilding ========  $submodule"
  echo "==========================================="
  ./gradlew ":$submodule:build"
done

echo "========================================================="
echo "=========== rebuilding ========  mbrun-intellij-plugin =="
echo "========================================================="
cd mbrun-intellij-plugin
./gradlew buildPlugin
cd ..
echo "========================================================================================="
echo "=========== copying ========  workers-lib-std-1.0-SNAPSHOT.jar to  src/test/resources/ =="
echo "========================================================================================="
cp workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar src/test/resources/

