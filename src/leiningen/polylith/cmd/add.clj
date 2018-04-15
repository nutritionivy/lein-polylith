(ns leiningen.polylith.cmd.add
  (:require [leiningen.polylith.utils :as utils]
            [leiningen.polylith.cmd.info :as info]
            [leiningen.polylith.file :as file]
            [leiningen.polylith.cmd.shared :as shared]
            [clojure.string :as str]))

(defn validate [ws-path component system]
  (let [components (info/all-components ws-path)
        systems (info/all-systems ws-path)]
    (cond
      (utils/is-empty-str? component) [false "Missing component name"]
      (utils/is-empty-str? system) [false "Missing system name"]
      (not (contains? components component)) [false (str "Component '" component "' not found")]
      (not (contains? systems system)) [false (str "System '" system "' not found")]
      :else [true])))

(defn add-component-to-system [ws-path top-dir component system]
  (let [component-dir (shared/full-name top-dir "/" (shared/src-dir-name component))
        system-dir (shared/full-name top-dir "/" system)
        relative-parent-path (shared/relative-parent-path system-dir)
        relative-component-path (str relative-parent-path "components/" component)
        system-path (str ws-path "/systems/" system)]
    (file/create-symlink (str system-path "/src/" component-dir)
                         (str relative-component-path "/src/" component-dir))
    (file/create-symlink (str system-path "/resources/" component)
                         (str "../../../components/" component "/resources/" component))))

(defn execute [ws-path top-dir [component system]]
  (let [[ok? message] (validate ws-path component system)]
    (if ok?
      (add-component-to-system ws-path top-dir component system)
      (println message))))