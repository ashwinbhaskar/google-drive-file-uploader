(defproject google-drive-file-uploader "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]

                 ;;http client
                 [clj-http "3.10.0"]
                 ;;json
                 [metosin/jsonista "0.2.4"]
                 [camel-snake-kebab "0.4.0"]

                 ;;time
                 [clojure.java-time "0.3.2"]

                 ;;command-line
                 [cli-matic "0.3.11"]

                 ;;error handling
                 [failjure "2.0.0"]

                 ;;testing
                 [mock-clj "0.2.1"]]
  :main google-drive-file-uploader.core
  :repl-options {:init-ns google-drive-file-uploader.core})
