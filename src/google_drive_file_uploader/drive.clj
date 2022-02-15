(ns google-drive-file-uploader.drive
  (:require [failjure.core :as f]
            [google-drive-file-uploader.config :as config]
            [clj-http.client :as http]
            [jsonista.core :as json]
            [google-drive-file-uploader.utils :as utils]
            [camel-snake-kebab.core :as csk])
  (:import (com.google.api.client.googleapis.auth.oauth2 GoogleCredential)
           (java.io FileInputStream)))

(defn get-access-token [filename]
 (.getAccessToken
  (doto
      (.createScoped
       (GoogleCredential/fromStream
        (FileInputStream. filename))
       ["https://www.googleapis.com/auth/drive"])
      (.refreshToken))))

(def mapper
  (json/object-mapper
   {:encode-key-fn utils/snake-case-keyword-keys
    :decode-key-fn utils/kebab-caseize-keyword}))

(defn- folder? [{mime-type :mime-type}]
  (= "application/vnd.google-apps.folder" mime-type))

(defn get-files [access-token]
  (let [url (config/get-files-url)
        {:keys [status body] :as response}
        (http/get url {:headers          {"Authorization" (str "Bearer " access-token)}
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
         multipart-content [{:name      "metadata"
                             :content   (-> {:name    file-name
                                             :parents parents}
                                            json/write-value-as-string)
                             :mime-type "application/json"
                             :encoding  "UTF-8"}
                            {:name      "file"
                             :content   (clojure.java.io/file file-path)
                             :mime-type "application/vnd.android.package-archive"
                             :encoding  "UTF-8"}]
         {:keys [status body] :as response} (http/post url {:headers          {"Authorization" (str "Bearer " access-token)}
                                                            :multipart        multipart-content
                                                            :throw-exceptions false})]
     (println response)
     (condp = status
       200 true
       false))))

(defn authorization-token [refresh-token client-id client-secret]
  (println "getting auth token..")
  (let [url (config/new-access-token-url)
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
(defn valid-access-token? [access-token]
  (println "Checking validity of access token..")
  (let [url (str (config/validate-access-token-url)
                 access-token)
        {status :status} (http/post url {:throw-exceptions false})]
    (= 200 status)))

(defn- validate [{:keys [access-token refresh-token client-id client-secret key-file] :as m} & _]
  (cond
    (and (empty? access-token)
         (empty? key-file)
         (or (empty? refresh-token)
             (empty? client-id)
             (empty? client-secret))) (f/fail "Either Access Token or Refresh Token, Client Id and Client Secret must be given")
    :else nil))

(defn upload-file-to-folder [{:keys [folder
                                     folder-id
                                     file-path
                                     file-name
                                     key-file
                                     access-token
                                     refresh-token
                                     client-id
                                     client-secret] :as map}]
  (f/try-all [_                   (validate map)
              access-token        (cond
                                    (valid-access-token? access-token) access-token

                                    (and refresh-token client-id client-secret)
                                    (authorization-token refresh-token client-id client-secret)

                                    key-file
                                    (get-access-token key-file))

              upload-folder-id    (or folder-id
                                      (->> (get-files access-token)
                                           :files
                                           (filter folder?)
                                           (some (fn [{name :name :as e}]
                                                   (let [trimmed-folder-name (clojure.string/trim folder)]
                                                     (when (= trimmed-folder-name name)
                                                       e))))
                                           :id))]
    (if (nil? upload-folder-id)
      (format "Folder %s does not exists" folder)
      (upload-file-multipart upload-folder-id file-path file-name access-token))))

(comment
 (upload-file-to-folder
  {:file-path "project.clj"
   :file-name "project.clj"
   :key-file "..."
   :folder-id "..."
   })
 )
