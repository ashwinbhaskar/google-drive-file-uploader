(ns google-drive-file-uploader.drive
  (:require [failjure.core :as f]
            [google-drive-file-uploader.config :as config]
            [clj-http.lite.client :as http]
            [jsonista.core :as json]
            [google-drive-file-uploader.utils :as utils]
            [camel-snake-kebab.core :as csk]))


(def mapper
  (json/object-mapper
    {:encode-key-fn utils/snake-case-keyword-keys
     :decode-key-fn utils/kebab-caseize-keyword}))

(defn- folder? [{mime-type :mime-type}]
  (= "application/vnd.google-apps.folder" mime-type))

(defn get-files [access-token]
  (let [url (config/get-files-url)
        {:keys [status body] :as response} (http/get url {:headers          {"Authorization" (str "Bearer " access-token)}
                                                          :throw-exceptions false})]
    (condp = status
      200 (-> body
              (json/read-value mapper))
      response)))

(defn upload-file-multipart
  ([folder-hierarchy file-path access-token]
   (upload-file-multipart folder-hierarchy file-path (utils/formatted-date-time) access-token))
  ([folder-hierarchy file-path file-name access-token]
   (println "Uploading file..")
   (let [url               (-> (config/file-upload-url)
                               (str "?uploadType=multipart"))
         parents           (clojure.string/split folder-hierarchy #"/")
         multipart-content (-> [[{:name      "metadata"
                                  :content   (-> {:name    file-name
                                                  :parents parents}
                                                 json/write-value-as-string)
                                  :mime-type "application/json"
                                  :encoding  "UTF-8"}]
                                [{:name      "file"
                                  :content   (clojure.java.io/file file-path)
                                  :mime-type "application/vnd.android.package-archive"
                                  :encoding  "UTF-8"}]]
                               utils/snake-caseize-and-stringify-keyword)
         content-length    (-> multipart-content
                               json/write-value-as-string
                               .getBytes
                               count)
         {:keys [status body] :as response} (http/post url {:headers          {"Authorization"  (str "Bearer " access-token)
                                                                               "Content-Type"   "multipart/related"
                                                                               "Content-Length" (str content-length)}
                                                            :multipart        multipart-content
                                                            :content-type     :json
                                                            :accept           :json
                                                            :throw-exceptions false})]
     (println response)
     (condp = status
       200 true
       false))))

(defn authorization-token [refresh-token]
  (println "getting auth token..")
  (let [url           (config/new-access-token-url)
        client-id     (config/client-id)
        client-secret (config/client-secret)
        {:keys [status body]} (http/post url {:body             (-> {:client-id     client-id
                                                                     :client-secret client-secret
                                                                     :grant-type    "refresh_token"
                                                                     :refresh-token refresh-token}
                                                                    utils/snake-case-keyword-keys
                                                                    json/write-value-as-string)
                                              :content-type     :json
                                              :throw-exceptions false})]
    (condp = status
      200 (-> body
              json/read-value
              utils/kebab-caseize-keys
              :access-token)
      (throw (ex-info (str "Error retrieving authorization-token" {:status status
                                                                   :body   body}) {})))))

(defn upload-file-to-folder [folder-name file-path file-name]
  (f/try-all [trimmed-folder-name (clojure.string/trim folder-name)
              access-token        (authorization-token (config/refresh-token))
              folder-id           (->> (get-files access-token)
                                       :files
                                       (filter folder?)
                                       (some (fn [{name :name :as e}]
                                               (if (= trimmed-folder-name name)
                                                 e)))
                                       :id)]
    (if (nil? folder-id)
      (format "Folder %s does not exists" folder-name)
      (upload-file-multipart folder-id file-path file-name access-token))
    (f/when-failed [e]
      (str e))))
