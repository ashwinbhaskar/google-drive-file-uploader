(ns google-drive-file-uploader.config
  (:require [clojure.java.io :as io]))

(def config (-> (io/resource "config.edn")
                slurp
                clojure.edn/read-string))

(defn refresh-token []
  (:refresh-token config))

(defn file-upload-url []
  (:file-upload-url config))

(defn file-directory []
  (:file-directory config))

(defn new-access-token-url []
  (:new-access-token-url config))

(defn client-id []
  (:client-id config))

(defn client-secret []
  (:client-secret config))
