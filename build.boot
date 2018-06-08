;; A project to provide an Antlr grammar parser

(def project 'babeloff/boot-antlr4-parser)
(def version "2017.10.31")

(set-env!
    :asset-paths #{}
    :source-paths #{"src_antlr4" "src_java"}
    :resource-paths #{"resource_clj"}
    :dependencies '[[org.clojure/clojure "RELEASE"]
                    [org.clojure/spec.alpha "0.1.143"]
                    [boot/core "RELEASE" :scope "test"]
                    [adzerk/boot-test "RELEASE" :scope "test"]
                    [radicalzephyr/bootlaces "0.1.14" :scope "test"]

                    [babeloff/boot-antlr4 "2018.06.07-SNAPSHOT"]
                    [org.antlr/antlr4 "4.7"]
                    [clj-jgit "0.8.10"]
                    [byte-streams "0.2.3"]
                    [me.raynes/fs "1.4.6"]

                    [org.apache.commons/commons-rdf-simple "0.5.0"]
                    [org.apache.commons/commons-rdf-jena "0.5.0"]])

(task-options!
 pom {:project     project
      :version     version
      :packaging   "jar"
      :description "help in generating the java based parser for literate antlr"
      :url         "https://github.com/babeloff/boot-antlr4-parser/wiki"
      :scm         {:url "https://github.com/babeloff/boot-antlr4-parser"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}
      :developers  {"Fred Eisele" ""}})

(require
  '[babeloff.boot-antlr4 :as antlr]
  '(boot [core :as boot :refer [deftask]]
         [util :as util]
         [task-helpers :as helper])
  '(clojure.spec [alpha :as s]))

(require
  '[adzerk.boot-test :refer [test]]
  '[radicalzephyr.bootlaces :as bl])

(bl/bootlaces! version)

(deftask clojars
  "release to clojars
  You will be prompted for
  your clojars user and password."
  []
  (comp
    (bl/build-jar)
    (bl/push-release)))

(deftask store
  [s show bool "show the arguments"]
  (comp
    (target :dir #{"target"})))

(deftask construct
  [s show bool "show the arguments"]
  (comp
    (antlr/generate
      :grammar "ANTLRv4Lexer.g4"
      :package "org.antlr.parser.antlr4"
      :show true)
    (antlr/generate
      :grammar "ANTLRv4Parser.g4"
      :package "org.antlr.parser.antlr4"
      :show true)
    (javac)))

(deftask exercise
  [s show bool "show the arguments"]
  (s/check-asserts true)
  (comp
    (antlr/exercise
      :parser "org.antlr.parser.antlr4.ANTLRv4Parser"
      :lexer "org.antlr.parser.antlr4.ANTLRv4Lexer"
      :start-rule "grammarSpec"
      :input ["src_antlr4/ANTLRv4Lexer.g4"
              "src_antlr4/ANTLRv4Parser.g4"]
      :tree true
      :edn true
      :rdf :jena
      :postscript true
      :tokens true
      :show true)))

(deftask my-repl
  []
  (comp (construct) (show :fileset true) (exercise) (repl) (store)))

(deftask dev
  []
  (comp
    (construct)
    (exercise)
    (store)))

(deftask live  [] (comp) (watch) (dev))

(deftask build
  "Build and install the project locally."
  []
  (comp (construct) (store) (pom) (bl/build-jar) (install)))
