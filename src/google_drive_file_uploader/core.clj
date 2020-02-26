(ns google-drive-file-uploader.core
  (:require [google-drive-file-uploader.drive :as drive]
            [cli-matic.core :refer [run-cmd]]
            [google-drive-file-uploader.utils :as utils])
  (:gen-class))

(defn here [& args]
  (println args))

(def CONFIGURATION
  {:app      {:command     "google-drive-uploader"
              :description "A command-line to generate your google authenticator OTP"
              :version     "0.1"}
   :commands [{:command     "upload-file" :short "uf"
               :description ["Upload a file"]
               :opts        [{:option "folder" :short "f" :type :string :default ""}
                             {:option "file-path" :short "fp" :type :string}
                             {:option "file-name" :short "fn" :type :string :default (utils/formatted-date-time)}
                             {:option "access-token" :short "at" :type :string :env "GD_ACCESS_TOKEN"}
                             {:option "refresh-token" :short "rt" :type :string :env "GD_REFRESH_TOKEN"}
                             {:option "client-id" :short "ci" :type :string :env "GD_CLIENT_ID"}
                             {:option "client-secret" :short "cs" :type :string :env "GD_CLIENT_SECRET"}]
               :runs        drive/upload-file-to-folder}]})

(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]
  (run-cmd args CONFIGURATION))

