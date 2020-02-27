(ns google-drive-file-uploader.utils
  (:require [clojure.walk :refer [postwalk]]
            [camel-snake-kebab.core :as csk]
            [java-time :as t]))

(defn snake-caseize-if-keyword [x]
  (if (keyword? x)
    (csk/->snake_case_keyword x)
    x))

(defn snake-case-keyword-keys [map]
  (postwalk snake-caseize-if-keyword map))

(defn kebab-caseize-keyword [x]
  (csk/->kebab-case-keyword x))

(defn kebab-caseize-keys [map]
  (postwalk (fn [form]
              (if (vector? form)
                (update form 0 kebab-caseize-keyword)
                form))
            map))

(defn formatted-date-time []
  (t/format "MMM:dd:uuuu:HH:mm"
            (t/zoned-date-time (t/instant)
                               (t/zone-id))))


