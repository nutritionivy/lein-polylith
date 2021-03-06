(ns leiningen.polylith.cmd.compile
  (:require [leiningen.polylith.cmd.info :as info]
            [leiningen.polylith.cmd.shared :as shared]
            [leiningen.polylith.cmd.sync :as sync]
            [leiningen.polylith.cmd.diff :as diff]
            [leiningen.polylith.time :as time]))

(defn find-changes [ws-path top-dir args]
  (let [paths              (diff/changed-file-paths ws-path args)
        changed-components (info/changed-components ws-path paths)
        changed-bases      (info/changed-bases ws-path paths)
        changed-systems    (info/changed-systems ws-path top-dir paths)]
    (println)
    (apply println "Changed components:" changed-components)
    (apply println "Changed bases:" changed-bases)
    (apply println "Changed systems:" changed-systems)
    (println)
    [changed-components changed-bases changed-systems]))

(defn compile-it [ws-path dir changes]
  (doseq [change changes]
    (println "Compiling" (str dir "/" change))
    (println (shared/sh "lein" "compile" :dir (str ws-path "/" dir "/" change)))))

(defn compile-changes [ws-path components bases systems]
  (println "Compiling workspace interfaces")
  (println (shared/sh "lein" "install" :dir (str ws-path "/interfaces")))
  (compile-it ws-path "components" (sort components))
  (compile-it ws-path "bases" (sort bases))
  (compile-it ws-path "systems" (sort systems)))

(defn execute [ws-path top-dir args]
  (let [start-time           (time/current-time)
        skip-circular-deps?  (contains? (set args) "-circular-deps")
        skip-sync?           (contains? (set args) "-sync")
        skip-execution-time? (contains? (set args) "-execution-time")
        cleaned-args         (filter #(and (not= "-sync" %)
                                           (not= "-circular-deps" %)
                                           (not= "-execution-time" %))
                                     args)
        [changed-components
         changed-bases
         changed-systems] (find-changes ws-path top-dir cleaned-args)]
    (if (and (not skip-circular-deps?)
             (info/has-circular-dependencies? ws-path top-dir))
      (shared/throw-polylith-exception "Cannot compile: circular dependencies detected. Type 'info' for more details.\n")
      (when (or skip-sync? (sync/execute ws-path top-dir))
        (compile-changes ws-path changed-components changed-bases changed-systems)))
    (when-not skip-execution-time?
      (println (str "\nExecution time: " (time/milliseconds->minutes-and-seconds (- (time/current-time) start-time)))))))
