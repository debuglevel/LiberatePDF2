#mkdir -p openapi/java
#docker run --rm -v ${PWD}/openapi:/local openapitools/openapi-generator-cli \
#  generate \
#  -i /local/microservice/build/tmp/kapt3/classes/main/META-INF/swagger/liberatepdf2-microservice-0.1.yml \
#  -g java \
#  -o /local/openapi/java \
#  --additional-properties=apiPackage=de.debuglevel.liberatepdf2.api
#rm -rf openapi/java

echo "== Checking if WSL..."
if command -v wslpath &> /dev/null
then
    echo "yes"
    LOCALDIR=$(wslpath -w $PWD)
else
    echo "no"
    LOCALDIR=$PWD
fi

echo
echo "== Backend"
echo "== Generating OpenAPI specs..."
cd microservice
./gradlew classes
cd ..

echo 
echo "== Kotlin"
echo "== Creating temporary directory..."
mkdir -p openapi/kotlin
echo "== Generating source files..."
docker run --rm -v ${LOCALDIR}:/local openapitools/openapi-generator-cli \
  generate \
  -i /local/microservice/build/tmp/kapt3/classes/main/META-INF/swagger/liberatepdf2-microservice-0.1.yml \
  -g kotlin \
  -o /local/openapi/kotlin \
  --additional-properties=packageName=de.debuglevel.liberatepdf2.restclient,groupId=de.debuglevel.liberatepdf2,artifactVersion=0.0.1
echo "== Removing existing source files..."
rm -rf javafx/src/main/kotlin/de/debuglevel/liberatepdf2/restclient
echo "== Copying source files..."
cp -a openapi/kotlin/src/main/. javafx/src/main/
echo "== Deleting temporary files..."
rm -rf openapi

echo 
echo "== TypeScript Angular"
echo "== Creating temporary directory..."
mkdir -p openapi/angular
echo "== Generating source files..."
docker run --rm -v ${LOCALDIR}:/local openapitools/openapi-generator-cli \
  generate \
  -i /local/microservice/build/tmp/kapt3/classes/main/META-INF/swagger/liberatepdf2-microservice-0.1.yml \
  -g typescript-angular \
  -o /local/openapi/angular \
  --additional-properties=fileNaming=kebab-case
echo "== Removing existing source files..."
rm -rf angular/src/app/restclient
echo "== Copying source files..."
cp -a openapi/angular/. angular/src/app/restclient/
echo "== Deleting temporary files..."
rm -rf openapi

