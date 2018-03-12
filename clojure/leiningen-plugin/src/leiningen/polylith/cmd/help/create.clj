(ns leiningen.polylith.cmd.help.create)

(defn help []
  (println "  Create a workspace or component.")
  (println)
  (println "    Create component 'n':")
  (println "      lein polylith create c[omponent] n")
  (println)
  (println "    Create workspace 'n' in namespace 'ns':")
  (println "      lein polylith create w[orkspace] n ns [top-dir]")
  (println)
  (println "  If left out, the top directory will correspond to the package, e.g.:")
  (println "  'com/my/company' if package is 'com.my.company'.")
  (println "  Set to blank if package name only exists in the system project.clj files,")
  (println "  but not as a package structure under src, e.g.:")
  (println "    (defproject com.my.comp/development \"1.0\"")
  (println "      ...)")
  (println)
  (println "  example:")
  (println "    lein polylith create c mycomponent")
  (println "    lein polylith create component mycomp")
  (println "    lein polylith create w myworkspace com.my.company")
  (println "    lein polylith create w myworkspace com.my.company \"\""))

