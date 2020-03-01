# google-drive-file-uploader ![Clojure CI](https://github.com/ashwinbhaskar/google-drive-file-uploader/workflows/Clojure%20CI/badge.svg?branch=master)

A Clojure CLI program to upload files to google-drive

## Usage

Clone the repo and run `lein uberjar`
Assuming you name the standalone jar file generated as `google-drive-file-uploader.jar`
```
java -jar google-drive-file-uploader.jar uf --folder "APKs"
 --file-path "/users/johndoe/foo.apk"
 --file-name "foo-debug.apk"
 --access-token "1//0g5OOBsnCGBASNwF-i4Be9t3ByEpiSha7" //can be ignored if set in env variable GD_ACCESS_TOKEN
 --refresh-token "1//0g5O1fYfm6BsnCgYIARAAGBASNwF-LNYaJvVVTAkpkbGpG" //can be ignored if set in env variable GD_REFRESH_TOKEN
 --client-id "806260tdi0.apps.googleusercontent.com" //can be ignored if set in env variable GD_CLIENT_ID
 --client-secret "12oXYAcp6Vc6BXxMZf20UQEq" //can be ignored if set in env variable GD_CLIENT_SECRET
```
will upload your file to google drive to the folder name mentioned in the command line argument.

## Important Note
 1. The parameters `access-token`, `refresh-token`, `client-id` and `client-secret` can be set in environment variables `GD_ACCESS_TOKEN`, `GD_REFRESH_TOKEN`, `GD_CLIENT_ID` and `GD_CLIENT_SECRET` respectively.
 2. You can choose to set skip `refresh-token`, `client-id` and `client-secret` if you give a valid `access-token`
 3. If you don't give the `access-token` then `refresh-token`, `client-id` and `client-secret` are mandatory
 
## Docker
The jar of the program is available as a docker image - https://hub.docker.com/r/ashwinbhskr/google-drive-uploader

As an example, for folks looking to upload their android apk using this program, I have built a 
docker image using android build box - https://hub.docker.com/r/ashwinbhskr/android-build-box-with-drive-uploader

You can use the above image to upload your apks in your build pipeline.
```
 - ./gradlew test
 - ./gradlew assembleDebug
 - cd app/build/outputs/apk/debug/
 - java -jar /drive-uploader.jar uf --folder "Foo APKs" --file-path "app-debug.apk" --file-name "foo.apk"
```
