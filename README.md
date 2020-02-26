# google-drive-file-uploader

A Clojure CLI program to upload files to google-drive

## Usage

Clone the repo and run `lein uberjar`
Assuming you name the standalone jar file generated as `google-drive-file-uploader.jar`
```
java -jar google-drive-file-uploader.jar uf --folder "<name-of-folder-to-upload-to>" --file-path "<full-path-of-file-to-upload>" --file-name "<name of the file that will be shown in drive>" --refresh-token "<refresh-token>" --client-id "<client-id>" --client-secret "<client-secret>"
```
will upload your file to google drive to the folder name mentioned in the command line argument.
