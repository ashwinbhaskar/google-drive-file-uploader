(ns google-drive-file-uploader.drive-test
  (:require [clojure.test :refer :all]
            [google-drive-file-uploader.drive :as drive]
            [mock-clj.core :as m]))

(deftest upload-file-to-folder-test
  (testing "Should check the validity of access token before calling get-files"
    (m/with-mock [drive/valid-access-token?   true
                  drive/get-files             {:files [{:id "foo-id-1"
                                                        :name "foo-folder"
                                                        :mime-type "application/vnd.google-apps.folder"}]}
                  drive/upload-file-multipart true]
      (is (drive/upload-file-to-folder {:folder "foo-folder"
                                        :file-path "/users/foo/a.apk"
                                        :file-name "foo-name.apk"
                                        :access-token "foo-access-token"
                                        :refresh-token "foo-refresh-token"
                                        :client-id "foo-client-id"
                                        :client-secret "foo-client-secret"}))
      (is (= 1
             (m/call-count #'drive/valid-access-token?)))
      (is (= 1
            (m/call-count #'drive/get-files)))
      (is (= 1
             (m/call-count #'drive/upload-file-multipart)))
      (is (= ["foo-access-token"]
             (m/last-call #'drive/valid-access-token?)))
      (is (= ["foo-access-token"]
             (m/last-call #'drive/valid-access-token?)))
      (is (= ["foo-id-1" "/users/foo/a.apk" "foo-name.apk" "foo-access-token"]
             (m/last-call #'drive/upload-file-multipart))))))
