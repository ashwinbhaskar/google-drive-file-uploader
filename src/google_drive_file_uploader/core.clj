(ns google-drive-file-uploader.core
  (:require [google-drive-file-uploader.drive :as drive]
            [cli-matic.core :refer [run-cmd]]
            [failjure.core :as f]
            [google-drive-file-uploader.utils :as utils])
  (:gen-class))

(defn fail [msg]
  (println msg)
  (System/exit 1))

(defn success
  ([]
   (System/exit 0))
  ([msg]
   (println msg)
   (System/exit 0)))

(defn upload [args]
  (f/if-let-ok? [result (drive/upload-file-to-folder args)]
    (success)
    (fail (f/message result))))

(def CONFIGURATION
  {:app      {:command     "google-drive-uploader"
              :description "A command-line to generate your google authenticator OTP"
              :version     "0.1"}
   :commands [{:command     "upload-file" :short "uf"
               :description ["Upload a file"]
               :opts        [{:option "folder" :short "f" :type :string :default ""}
                             {:option "file-path" :short "fp" :type :string :default :present}
                             {:option "file-name" :short "fn" :type :string :default (utils/formatted-date-time)}
                             {:option "access-token" :short "at" :type :string :env "GD_ACCESS_TOKEN"}
                             {:option "key-file" :short "k" :type :string :env "GD_KEY_FILE"}
                             {:option "refresh-token" :short "rt" :type :string :env "GD_REFRESH_TOKEN"}
                             {:option "client-id" :short "ci" :type :string :env "GD_CLIENT_ID"}
                             {:option "client-secret" :short "cs" :type :string :env "GD_CLIENT_SECRET"}]
               :runs        upload}]})

(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]
  (run-cmd args CONFIGURATION))
