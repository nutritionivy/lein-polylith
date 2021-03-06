(ns leiningen.polylith.time
  (:require [clojure.pprint :as pp]
            [leiningen.polylith.file :as file])
  (:import (java.io FileNotFoundException)
           (java.util Date)
           (java.text SimpleDateFormat)))

(defn time-bookmarks [ws-path]
  (try
    (read-string (slurp (str ws-path "/.polylith/time.edn")))
    (catch FileNotFoundException _ {})))

(defn last-success-time [ws-path]
  (or (:last-success (time-bookmarks ws-path))
      0))

(defn set-bookmark! [ws-path bookmark]
  (println "set" bookmark "in .polylith/time.edn")
  (let [paths         (file/valid-paths ws-path)
        latest-change (file/latest-modified paths)
        bookmarks     (assoc (time-bookmarks ws-path)
                        bookmark latest-change)
        file          (str ws-path "/.polylith/time.edn")]
    (pp/pprint bookmarks (clojure.java.io/writer file))))

(def formatter (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss"))

(defn ->time [^Long timestamp]
  (.format formatter (Date. timestamp)))

(defn current-time []
  (.getTime (Date.)))

(defn milliseconds->minutes-and-seconds [milliseconds]
  (let [sec10 (int (/ milliseconds 100))
        seconds (/ (double (mod sec10 600)) 10.0)
        minutes (int (/ sec10 600))]
    (if (zero? minutes)
      (format "%.1f seconds" seconds)
      (format "%d minutes %.1f seconds" minutes seconds))))

(defn parse-timestamp [bookmark-or-point-in-time]
  (try
    [true (Long/parseLong bookmark-or-point-in-time)]
    (catch Exception _ [false])))

(defn parse-time-argument [ws-path bookmark-or-point-in-time]
  (let [[ok? timestamp] (parse-timestamp bookmark-or-point-in-time)]
    (if ok?
      timestamp
      (let [bookmarks     (time-bookmarks ws-path)
            bookmark      (keyword bookmark-or-point-in-time)
            point-in-time (bookmarks bookmark)]
        (or point-in-time 0)))))

(defn parse-time-args [ws-path [bookmark-or-point-in-time]]
  (let [time (if bookmark-or-point-in-time
               (parse-time-argument ws-path bookmark-or-point-in-time)
               (last-success-time ws-path))]
    time))
