# demo project : literate antlr4

[![Build Status](https://travis-ci.org/babeloff/boot-antlr4-parser.svg?branch=master)](https://travis-ci.org/babeloff/boot-antlr4-parser)
[![Clojars Project](https://img.shields.io/clojars/v/babeloff/boot-antlr4-parser.svg)](https://clojars.org/babeloff/boot-antlr4-parser)

This demo does two things:

1. Demonstrates how the provided tasks may be used to do live-coding
2. Extend the Antlr4 grammar itself to allow a literate programming style.


## Live-coding

This adds tasks for parsing Antlr4 grammars, not for generating code from them:

    (require
       '(boot [core :as boot :refer [deftask]]
              [util :as util]
              [task-helpers :as helper])
       '(babeloff
              [boot-antlr4 :as antlr]
              [boot-antlr4-parser :as coach])

    (deftask parse-grammar
      [s show bool "show the arguments"]
      (s/check-asserts true)
      (let [input-dir-str "./src/antlr4"
            input-dir (Paths/get input-dir-str no-path-extensions)
            input-raw-s
               (let [children (Files/walk input-dir 1 no-file-visit-options)]
                 (iterator-seq (.iterator children)))
            gm (get-path-matcher "glob:\*.{g4}")
            input-file-s (filter #(matches gm %1) input-raw-s)]
        (comp
          (coach/exercise
            :start-rule "grammarSpec"
            :tree false
            :edn true
            :rdf :jena
            :postscript false
            :tokens false
            :input input-file-s))))

This sample shows the construction of a new task
using the `exercise` task provided by this project.

## Literate Antlr4

This is a work in progress.
The goal is to extend the work on literate programming to
include multiple isomorphic representations.
In this way error messages will provide links into the
source code in a useful fashion.
