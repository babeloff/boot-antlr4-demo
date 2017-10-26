
(def project 'babeloff/literate-antlr)
(def version "2017.10.20-SNAPSHOT")

(set-env!
    :source-paths #{"src/antlr4" "src/java"}
    :dependencies '[[org.clojure/clojure "RELEASE"]
                    [org.clojure/spec.alpha "0.1.134"]
                    [boot/core "RELEASE" :scope "test"]
                    [babeloff/boot-antlr4 "2017.10.20-SNAPSHOT"]
                    [org.antlr/antlr4 "4.7"]
                    [clj-jgit "0.8.10"]
                    [byte-streams "0.2.3"]
                    [me.raynes/fs "1.4.6"]

                    [org.apache.commons/commons-rdf-simple "0.3.0-incubating"]
                    [org.apache.commons/commons-rdf-jena "0.3.0-incubating"]])

(task-options!
 pom {:project     project
      :version     version
      :packaging   "jar"
      :description "help in generating the java based parser for literate antlr"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/CategoricalData/fql"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}
      :developers  {"Fred Eisele" ""}})


;; (import '(org.antlr.v4.gui TestRig))
(require '[babeloff.boot-antlr4 :as antlr :refer [antlr4 test-rig]]
         '(boot [core :as boot :refer [deftask]]
                [util :as util]
                [task-helpers :as helper])
         '(clojure.spec [alpha :as s]))

(deftask store
  [s show bool "show the arguments"]
  (comp
    (target :dir #{"target"})))

(deftask build
  [s show bool "show the arguments"]
  (comp
    (antlr4 :grammar "ANTLRv4Lexer.g4"
            :package "org.antlr.parser.antlr4"
            :show true)
    (antlr4 :grammar "ANTLRv4LiterateParser.g4"
            :package "org.antlr.parser.antlr4"
            :show true)
    (javac)))

(deftask exercise
  [s show bool "show the arguments"]
  (s/check-asserts true)
  (comp
    (test-rig :parser "org.antlr.parser.antlr4.ANTLRv4LiterateParser"
              :lexer "org.antlr.parser.antlr4.ANTLRv4Lexer"
              :start-rule "literaryGrammarSpec"
              :input ["src/antlr4/ANTLRv4Lexer.g4"
                      "src/antlr4/ANTLRv4Parser.g4"]
              :tree true
              :edn true
              :rdf :jena
              :postscript true
              :tokens true
              :show true)))

(deftask my-repl
  []
  (comp (build) (show :fileset true) (exercise) (repl) (store)))

(deftask dev
  []
  (comp
    (build)
    (exercise)
    (store)))

(deftask live  [] (comp) (watch) (dev))
