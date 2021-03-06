(ns leiningen.polylith.cmd.test
  (:require [clojure.string :as str]
            [leiningen.polylith.cmd.compile :as compile]
            [leiningen.polylith.cmd.diff :as diff]
            [leiningen.polylith.cmd.info :as info]
            [leiningen.polylith.cmd.shared :as shared]
            [leiningen.polylith.cmd.success :as success]
            [leiningen.polylith.cmd.sync :as sync]
            [leiningen.polylith.file :as file]
            [leiningen.polylith.time :as time]))

(defn show-tests [tests]
  (if (empty? tests)
    (println "echo 'Nothing changed - no tests executed'")
    (println (str "lein test " (str/join " " tests)))))

(defn run-tests [test-namespaces ws-path]
  (if (zero? (count test-namespaces))
    (println "Nothing to test")
    (do
      (println "Start execution of tests in" (count test-namespaces) "namespaces:")
      (show-tests test-namespaces)
      (println (apply shared/sh (concat ["lein" "test"] test-namespaces [:dir (str ws-path "/environments/development")]))))))

(defn path->ns [path]
  (second (first (file/read-file path))))

(defn ->tests [ws-path top-dir base-or-component]
  (let [dir   (shared/full-dir-name top-dir base-or-component)
        path  (str ws-path "/environments/development/test/" dir)
        paths (map second (file/paths-in-dir path))]
    (map path->ns paths)))

(defn tests [ws-path top-dir changed-entities]
  (mapcat #(->tests ws-path top-dir %) changed-entities))

(defn all-test-namespaces [ws-path top-dir args]
  (let [paths                     (diff/changed-file-paths ws-path args)
        changed-bases             (info/changed-bases ws-path paths)
        changed-components        (info/changed-components ws-path paths)
        indirect-changed-entities (info/all-indirect-changes ws-path top-dir paths)
        changed-entities          (set (concat changed-components changed-bases indirect-changed-entities))
        entity-tests              (tests ws-path top-dir changed-entities)]
    (vec (sort (map str entity-tests)))))

(defn execute [ws-path top-dir args]
  (let [start-time           (time/current-time)
        skip-circular-deps?  (contains? (set args) "-circular-deps")
        skip-compile?        (contains? (set args) "-compile")
        skip-sync?           (contains? (set args) "-sync")
        skip-success?        (contains? (set args) "-success")
        skip-execution-time? (contains? (set args) "-execution-time")
        cleaned-args         (filter #(and (not= "-compile" %)
                                           (not= "-sync" %)
                                           (not= "-circular-deps" %)
                                           (not= "-success" %)
                                           (not= "-execution-time" %))
                                     args)
        tests                (all-test-namespaces ws-path top-dir cleaned-args)]
    (if (and (not skip-circular-deps?)
             (info/has-circular-dependencies? ws-path top-dir))
      (shared/throw-polylith-exception "Cannot compile: circular dependencies detected. Type 'info' for more details.\n")
      (when (or skip-sync? (sync/execute ws-path top-dir))
        (when-not skip-compile? (compile/execute ws-path top-dir (conj cleaned-args "-sync" "-circular-deps" "-execution-time")))
        (run-tests tests ws-path)
        (when-not skip-success? (success/execute ws-path cleaned-args))))
    (when-not skip-execution-time?
      (println (str "\nExecution time: " (time/milliseconds->minutes-and-seconds (- (time/current-time) start-time)))))))
