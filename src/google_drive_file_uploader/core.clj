(ns google-drive-file-uploader.core
  (:require [google-drive-file-uploader.config :as config]
            [clj-http.client :as http]
            [jsonista.core :as json]
            [google-drive-file-uploader.utils :as utils]
            [java-time :as t]))

(defn upload-file-simple [file-path access-token]
  (let [url (config/file-upload-url)
        {:keys [status body] :as response} (http/post url {:headers          {"Authorization" (str "Bearer " access-token)
                                                                              "Content-Type"  "application/vnd.android.package-archive"}
                                                           :body             (clojure.java.io/file file-path)
                                                           :throw-exceptions false})]
    (condp = status
      200 true
      response)))

(defn get-files [access-token]
  (let [url (config/get-files-url)
        {:keys [status body] :as response} (http/get url {:headers          {"Authorization" (str "Bearer " access-token)}
                                                          :throw-exceptions false})]
    (condp = status
      200 (do
            (println response)
            true)
      response)))

(defn upload-file-multipart [folder-hierarchy file-path access-token]
  (let [url     (-> (config/file-upload-url)
                    (str "?uploadType=multipart"))
        parents (clojure.string/split folder-hierarchy #"/")
        {:keys [status body] :as response} (http/post url {:headers          {"Authorization" (str "Bearer " access-token)}
                                                           :multipart        [{:name      "metadata"
                                                                               :content   (-> {:name    (str (utils/formatted-date-time)
                                                                                                             "capture-debug.apk")
                                                                                               :parents parents}
                                                                                              json/write-value-as-string)
                                                                               :mime-type "application/json"
                                                                               :encoding  "UTF-8"}
                                                                              {:name      "file"
                                                                               :content   (clojure.java.io/file file-path)
                                                                               :mime-type "application/vnd.android.package-archive"
                                                                               :encoding  "UTF-8"}]
                                                           :throw-exceptions false})]
    (condp = status
      200 (do
            (println response)
            true)
      response)))

(defn authorization-token [refresh-token]
  (let [url           (config/new-access-token-url)
        client-id     (config/client-id)
        client-secret (config/client-secret)
        {:keys [status body]} (http/post url {:body             (-> {:client-id     client-id
                                                                     :client-secret client-secret
                                                                     :grant-type    "refresh_token"
                                                                     :refresh-token refresh-token}
                                                                    utils/snake-case-keyword-keys
                                                                    json/write-value-as-string)
                                              :throw-exceptions false})]
    (condp = status
      200 (-> body
              json/read-value
              utils/kebab-caseize-keys
              :access-token)
      (throw (ex-info (str "Error retrieving authorization-token" {:status status
                                                                   :body   body}) {})))))
(defn orchestrate []
  (try
    (->> (config/refresh-token)
         authorization-token
         (upload-file-multipart (config/parent-directory-id) (config/file-directory)))
    (catch Exception e
      (println e))))

(defn -main []
  (orchestrate))

