# google-drive-file-uploader ![Clojure CI](https://github.com/ashwinbhaskar/google-drive-file-uploader/workflows/Clojure%20CI/badge.svg?branch=master)

A Clojure CLI program to upload files to google-drive

## Usage

Clone the repo and run `lein uberjar`
Assuming you name the standalone jar file generated as `google-drive-file-uploader.jar`
```
java -jar google-drive-file-uploader.jar uf --folder "<name-of-folder-to-upload-to>"
 --file-path "<full-path-of-file-to-upload>"
 --file-name "<name of the file that will be shown in drive>"
 --access-token "<access-token>" //can be ignored if set in env variable GD_ACCESS_TOKEN
 --refresh-token "<refresh-token>" //can be ignored if set in env variable GD_REFRESH_TOKEN
 --client-id "<client-id>" //can be ignored if set in env variable GD_CLIENT_ID
 --client-secret "<client-secret>" //can be ignored if set in env variable GD_CLIENT_SECRET
```
will upload your file to google drive to the folder name mentioned in the command line argument.

## Important Note
 1. The parameters `access-token`, `refresh-token`, `client-id` and `client-secret` can be set in environment variables `GD_ACCESS_TOKEN`, `GD_REFRESH_TOKEN`, `GD_CLIENT_ID` and `GD_CLIENT_SECRET` respectively.
 2. You can choose to set skip `refresh-token`, `client-id` and `client-secret` if you give a valid `access-token`
 3. If you don't give the `access-token` then `refresh-token`, `client-id` and `client-secret` are mandatory
