(ns babeloff.boot-antlr4-parser
  "an antlr task that uses the ANTLRv4 lexers and parsers."
  {:boot/export-tasks true}
  (:require
    (boot [core :as boot :refer [deftask]]
          [util :as util]
          [task-helpers :as helper])
    (clojure [pprint :as pp]
             [edn :as edn]
             [reflect :as reflect]
             [string :as string])
    (clojure.java [io :as io]
                  [classpath :as cp])
    (clojure.spec [alpha :as s])
    (clojure.spec.test [alpha :as stest])
    (me.raynes [fs :as fs])
    [rdf :as rdf]
    (babeloff [boot-antlr4 :as antlr]
              [options :as opts]))
  (:import
    (org.antlr.parser.antlr4 ANTLRv4Lexer
                             ANTLRv4Parser)
    (org.antlr.v4.runtime TokenStream
                          CommonTokenStream
                          CharStreams)
    (clojure.lang Reflector)
    (java.nio.file Paths)
    (java.nio.charset Charset))

(defn get-target-path
  [file-path]
  (->> (fs/split file-path)
       (map #(case % ".." "dots" "." "dot" %))))

(deftask parse
  "A task that runs the ANTLR4 parser."
  [r start-rule     LIB_DIR str    "the name of the first rule to match"
   e encoding       STYLE   str    "specify output STYLE for messages in antlr, gnu, vs2005"
   i input          OPT     [str]  "the file name of the input grammar to parse"
   s show                   bool   "show the constructed properties"
   a tokens                 bool   "produce the annotated token stream"
   t tree                   bool   "produce the annotated parse tree in lisp form"
   _ edn                    bool   "produce the annotated parse tree in edn form"
   _ rdf            ENGINE  kw     "produce the annotated parse tree in rdf form using the specified ENGINE :simple, :clojure, :jena"
   z postscript             bool   "produce a postscript output of the parse tree"
   x trace                  bool   "show the progress that the parser makes"
   d diagnostics            bool   "show some diagnostics"
   f sll                    bool   "use the fast SLL prediction mode"])
(cond
  (not parser)
  (do
    (boot.util/fail "The --parser argument is required")
    (*usage*))

  (not start-rule)
  (do
    (boot.util/fail "The --start-rule argument is required")
    (*usage*))

  :else
  (let [target-dir (boot/tmp-dir!)
          target-dir-str (.getCanonicalPath target-dir)]
      (fn middleware [next-handler]
        (fn handler [fileset]

          (boot/empty-dir! target-dir)
          (util/info "target: %s\n" target-dir-str)
          (util/info "parser: %s\n" parser)
          (util/info "lexer: %s\n" lexer)
          (util/info "working directory: %s\n"
            (-> (Paths/get "." (make-array String 0))
               .toAbsolutePath .normalize .toString))

         (when show
           (util/info "parse options: %s\n" *opts*))

         (let [lexer-inst (ANTLRv4Lexer. nil)
               parser-inst (ANTLRv4Parser. nil)
               char-set (Charset/defaultCharset)
               target-file-fn identity]

           (anltr/parse-file lexer-inst parser-inst char-set
              start-rule input target-file-fn
              {:encoding encoding :show show :tokens tokens
                :lisp tree :edn edn :rdf rdf :postscript postscript
                :trace trace :diagnostics diagnostics :sll sll}))

            ;; prepare fileset and call next-handler
         (let [fileset' (-> fileset
                             (boot/add-asset target-dir)
                             boot/commit!)
               result (next-handler fileset')]
           ;; post processing
           ;; the goal here is to perform any side effects
           result)))))
