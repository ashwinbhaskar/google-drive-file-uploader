(ns google-drive-file-uploader.core
  (:require [google-drive-file-uploader.drive :as drive]
            [cli-matic.core :refer [run-cmd]]
            [failjure.core :as f]
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

