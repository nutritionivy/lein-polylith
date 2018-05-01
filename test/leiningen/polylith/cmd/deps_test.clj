(ns leiningen.polylith.cmd.deps-test
  (:require [clojure.test :refer :all]
            [leiningen.polylith.cmd.test-helper :as helper]
            [leiningen.polylith.file :as file]
            [leiningen.polylith :as polylith]))

(use-fixtures :each helper/test-setup-and-tear-down)

(deftest polylith-deps--interface-deps-with-namespace--print-component-dependencies
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "my.company")
          core-content [(str "(ns my.company.comp2.core\n"
                             "  (:require [my.company.interface1.interface :as interface1]))\n\n"
                             "(defn add-two [x]\n  (interface1/add-two x))")]
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "my.company")
                   (polylith/polylith project "create" "c" "comp1a" "interface1")
                   (polylith/polylith project "create" "c" "comp1b" "interface1")
                   (polylith/polylith project "create" "c" "comp2")
                   (file/replace-file (str ws-dir "/components/comp2/src/my/company/comp2/core.clj") core-content)
                   (polylith/polylith project "deps"))]
      (is (= (str "comp1a:\n"
                  "comp1b:\n"
                  "comp2:\n"
                  ;; We don't print comp1b, because it's not part of any environment or system.
                  ;; When creating 'comp1a' it is also added to the 'development' environment,
                  ;; but when adding 'comp1b' a component, 'comp1a', with interface 'interface1'
                  ;; already exists and is therefore not added.
                  "  comp1a\n")
             output)))))

(deftest polylith-deps--interface-deps-without-namespace--print-component-dependencies
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "")
          core-content [(str "(ns comp2.core\n"
                             "  (:require [interface1.interface :as interface1]))\n\n"
                             "(defn add-two [x]\n  (interface1/add-two x))")]
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "")
                   (polylith/polylith project "create" "c" "comp1" "interface1")
                   (polylith/polylith project "create" "c" "comp2")
                   (file/replace-file (str ws-dir "/components/comp2/src/comp2/core.clj") core-content)
                   (polylith/polylith project "deps"))]
      (is (= (str "comp1:\n"
                  "comp2:\n"
                  "  comp1\n")
             output)))))


(deftest polylith-deps--interface-deps-with-namespace-from-base--print-component-dependencies
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "my.company")
          core-content [(str "(ns my.company.system1.core\n"
                             "  (:require [my.company.interface1.interface :as interface1])\n"
                             "  (:gen-class))\n\n"
                             "(defn -main [& args]\n"
                             "  (interface1/add-two 1)\n"
                             "  (println \"Hello world!\"))\n")]
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "my.company")
                   (polylith/polylith project "create" "s" "system1")
                   (polylith/polylith project "create" "c" "comp1a" "interface1")
                   (polylith/polylith project "create" "c" "comp1b" "interface1")
                   (polylith/polylith project "create" "c" "comp2")
                   (file/replace-file (str ws-dir "/systems/system1/src/my/company/system1/core.clj") core-content)
                   (polylith/polylith project "deps"))]
      (is (= (str "comp1a:\n"
                  "comp1b:\n"
                  "comp2:\n"
                  "system1:\n"
                  "  comp1a\n")
             output)))))

(deftest polylith-deps--interface-deps-without-namespace-from-base--print-component-dependencies
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "")
          core-content [(str "(ns system1.core\n"
                             "  (:require [interface1.interface :as interface1])\n"
                             "  (:gen-class))\n\n"
                             "(defn -main [& args]\n"
                             "  (interface1/add-two 1)\n"
                             "  (println \"Hello world!\"))\n")]
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "")
                   (polylith/polylith project "create" "s" "system1")
                   (polylith/polylith project "create" "c" "comp1a" "interface1")
                   (polylith/polylith project "create" "c" "comp1b" "interface1")
                   (polylith/polylith project "create" "c" "comp2")
                   (file/replace-file (str ws-dir "/systems/system1/src/system1/core.clj") core-content)
                   (polylith/polylith project "deps"))]
      (is (= (str "comp1a:\n"
                  "comp1b:\n"
                  "comp2:\n"
                  "system1:\n"
                  "  comp1a\n")
             output)))))

(deftest polylith-deps--interface-deps-with-namespace--print-interface-dependencies
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "my.company")
          core-content [(str "(ns my.company.comp2.core\n"
                             "  (:require [my.company.interface1.interface :as interface1]))\n\n"
                             "(defn add-two [x]\n  (interface1/add-two x))")]
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "my.company")
                   (polylith/polylith project "create" "c" "comp1" "interface1")
                   (polylith/polylith project "create" "c" "comp2")
                   (file/replace-file (str ws-dir "/components/comp2/src/my/company/comp2/core.clj") core-content)
                   (polylith/polylith project "deps" "i"))]
      (is (= (str "comp1:\n"
                  "comp2:\n"
                  "  interface1\n")
             output)))))

(deftest polylith-deps--interface-deps-without-namespace--print-interface-dependencies
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "")
          core-content [(str "(ns comp2.core\n"
                             "  (:require [interface1.interface :as interface1]))\n\n"
                             "(defn add-two [x]\n  (interface1/add-two x))")]
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "")
                   (polylith/polylith project "create" "c" "comp1" "interface1")
                   (polylith/polylith project "create" "c" "comp2")
                   (file/replace-file (str ws-dir "/components/comp2/src/comp2/core.clj") core-content)
                   (polylith/polylith project "deps" "i"))]
      (is (= (str "comp1:\n"
                  "comp2:\n"
                  "  interface1\n")
             output)))))

(deftest polylith-deps--interface-deps-with-namespace--print-function-dependencies
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "my.company")
          core-content [(str "(ns my.company.comp2.core\n"
                             "  (:require [my.company.comp1.interface :as comp1]))\n\n"
                             "(defn add-two [x]\n  (comp1/add-two x))")]
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "my.company")
                   (polylith/polylith project "create" "c" "comp1")
                   (polylith/polylith project "create" "c" "comp2")
                   (file/replace-file (str ws-dir "/components/comp2/src/my/company/comp2/core.clj") core-content)
                   (polylith/polylith project "deps" "f"))]
      (is (= (str "comp1:\n"
                  "comp2:\n"
                  "  my.company.comp1.interface/add-two\n")
             output)))))

(deftest polylith-deps--interface-deps-without-namespace--print-function-dependencies
  (with-redefs [file/current-path (fn [] @helper/root-dir)]
    (let [ws-dir (str @helper/root-dir "/ws1")
          project (helper/settings ws-dir "")
          core-content [(str "(ns comp2.core\n"
                             "  (:require [interface1.interface :as interface1]))\n\n"
                             "(defn add-two [x]\n  (interface1/add-two x))")]
          output (with-out-str
                   (polylith/polylith nil "create" "w" "ws1" "")
                   (polylith/polylith project "create" "c" "comp1" "interface1")
                   (polylith/polylith project "create" "c" "comp2")
                   (file/replace-file (str ws-dir "/components/comp2/src/comp2/core.clj") core-content)
                   (polylith/polylith project "deps" "f"))]
      (is (= (str "comp1:\n"
                  "comp2:\n"
                  "  interface1.interface/add-two\n")
             output)))))
