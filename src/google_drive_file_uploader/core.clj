(ns google-drive-file-uploader.core
  (:require [google-drive-file-uploader.drive :as drive]
            [cli-matic.core :refer [run-cmd]]
            [google-drive-file-uploader.utils :as utils])
  (:import (lockfix LockFix))
  (:gen-class))

(defmacro locking*                                          ;; patched version of clojure.core/locking to workaround GraalVM unbalanced monitor issue
  "Executes exprs in an implicit do, while holding the monitor of x.
  Will release the monitor of x in all circumstances."
  {:added "1.0"}
  [x & body]
  `(let [lockee# ~x]
     (LockFix/lock lockee# (^{:once true} fn* [] ~@body))))

(defn dynaload ;; patched version of clojure.spec.gen.alpha/dynaload to use patched locking macro
  [s]
  (let [ns (namespace s)]
    (assert ns)
    (locking* #'clojure.spec.gen.alpha/dynalock
              (require (symbol ns)))
    (let [v (resolve s)]
      (if v
        @v
        (throw (RuntimeException. (str "Var " s " is not on the classpath")))))))

(alter-var-root #'clojure.spec.gen.alpha/dynaload (constantly dynaload))

(def CONFIGURATION
  {:app         {:command     "google-drive-uploader"
                 :description "A command-line to generate your google authenticator OTP"
                 :version     "0.1"}
   :global-opts [{:option "access-token"
                  :as     "The access token used to make the request"
                  :type   :string}
                 {:option "refresh-token"
                  :as     "The refresh token to fetch access token in case the later expires"
                  :type   :string}]
   :commands    [{:command     "upload-file" :short "uf"
                  :description ["Upload a file"]
                  :opts        [{:option "folder" :short "folder" :type :string :default ""}
                                {:option "file-path" :short "fp" :type :string}
                                {:option "file-name" :short "fn" :type :string :default (utils/formatted-date-time)}]
                  :runs        drive/upload-file-to-folder}]})

(defn -main
  "This is our entry point.
  Just pass parameters and configuration.
  Commands (functions) will be invoked as appropriate."
  [& args]
  (run-cmd args CONFIGURATION))

