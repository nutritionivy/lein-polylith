(ns leiningen.polylith.cmd.create.system
  (:require [leiningen.polylith.cmd.shared :as shared]
            [leiningen.polylith.cmd.create.base :as create-base]
            [leiningen.polylith.file :as file]
            [clojure.string :as str]))

(defn- ->str [v]
  (if (string? v) (str "\"" v "\"") v))

(defn- source->str [ks k v]
  (let [s (str "\n                 ")]
    (str "  " k ks " " "[" (str/join s (map ->str v)) "]")))

(defn- kv->str [[k v]]
  (condp = k
    :source-paths (source->str "" k v)
    :test-paths (source->str "  " k v)
    (str "  " k " " (->str v))))

(defn- ->prettify [[_ project-name version & key-values]]
  (str "(defproject " project-name " \"" version "\"\n"
       (str/join "\n" (map kv->str (partition 2 key-values)))
       ")\n"))

(defn sources [bases src]
  (vec (concat [src] (map #(str src "-" %) bases))))

(defn update-sources-in-project-file! [bases dir]
  (let [content (vec (read-string (slurp dir)))
        src-index (inc (ffirst (filter #(= :source-paths (second %)) (map-indexed vector content))))
        test-index (inc (ffirst (filter #(= :test-paths (second %)) (map-indexed vector content))))
        srcs (sources bases "sources/src")
        tests (sources bases "tests/test")
        new-content (assoc content src-index srcs test-index tests)]
    (spit dir (->prettify new-content))))

(defn create-dev-links [ws-path dev-dir base system]
  (let [root (str ws-path "/environments/" dev-dir)
        bases (shared/all-bases ws-path)
        base-path (str "../../../bases/" base)
        base-src (str root "/sources/src-" base)
        base-test (str root "/tests/test-" base)
        system-path (str "../../../systems/" system)]
    (update-sources-in-project-file! bases (str root "/project.clj"))
    (file/create-symlink (str root "/docs/" base "-Readme.md")
                         (str base-path "/Readme.md"))
    (file/create-symlink (str root "/docs/" system "-Readme.md")
                         (str system-path "/Readme.md"))
    (file/create-symlink (str root "/resources/" base)
                         (str base-path "/resources/" base))
    (file/create-symlink (str root "/project-files/bases/" base "-project.clj")
                         (str "../" base-path "/project.clj"))
    (file/create-symlink (str root "/project-files/systems/" system "-project.clj")
                         (str "../" system-path "/project.clj"))
    (file/create-symlink (str base-src)
                         (str "../../../bases/" base "/src"))
    (file/create-symlink (str base-test)
                         (str "../../../bases/" base "/test"))))

(defn create [ws-path top-dir top-ns clojure-version system base-name base-top-ns base-top-dir]
  (let [base (if (str/blank? base-name) system base-name)
        bases (conj (shared/all-bases ws-path) base)
        proj-ns (shared/full-name top-ns "/" system)
        base-dir (shared/full-name base-top-dir "/" (shared/src-dir-name base))
        base-ns (shared/full-name base-top-ns "." base)
        system-path (str ws-path "/systems/" system)
        project-content [(str "(defproject " proj-ns " \"0.1\"")
                         (str "  :description \"A " system " system.\"")
                         (str "  :source-paths " (sources bases "sources/src"))
                         (str "  :test-paths " (sources bases "tests/test"))
                         (str "  :dependencies [" (shared/->dependency "org.clojure/clojure" clojure-version) "]")
                         (str "  :aot :all")
                         (str "  :main " base-ns ".core)")]
        build-content ["#!/usr/bin/env bash"
                       "set -e"
                       ""
                       "lein uberjar"]
        doc-content [(str "# " system " system")
                     ""
                     "add documentation here..."]
        dev-dirs (file/directory-names (str ws-path "/environments"))]
    (when-not (file/file-exists (str ws-path "/bases/" base-dir))
      (create-base/create-base ws-path base-dir top-ns base-top-ns base clojure-version))

    (file/create-dir system-path)
    (file/create-dir (str system-path "/resources"))
    (file/create-file (str system-path "/resources/.keep") [""])
    (file/create-dir (str system-path "/sources"))
    (file/create-file (str system-path "/project.clj") project-content)
    (file/create-file (str system-path "/build.sh") build-content)
    (file/create-file (str system-path "/Readme.md") doc-content)
    (file/make-executable (str system-path "/build.sh"))
    (shared/create-src-dirs! ws-path (str "systems/" system "/sources/src") [(shared/src-dir-name top-dir)])
    (file/create-symlink (str system-path "/sources/src-" base)
                         (str "../../../bases/" base "/src"))
    (file/create-symlink (str system-path "/resources/" base)
                         (str "../../../bases/" base "/resources/" base))

    (doseq [dev-dir dev-dirs]
      (create-dev-links ws-path dev-dir base system))))
