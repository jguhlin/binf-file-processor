(ns binf-file-processor.core
  (:require [opennlp.nlp :as nlp]
            [opennlp.treebank :as treebank]
            [opennlp.tools.train :as train]
            [me.raynes.fs :as fs]
            [instaparse.core :as insta]
            [binf-file-processor.identify-filetype :as ident]
            [binf-file-processor.analyze-files :as analyze]
            [clojure.core.matrix :as matrix]
            [clojure.core.async :as async 
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]))

(def at-loc "resources/example-files/At10/")
(def gm-loc "resources/example-files/Gm1.1/")
(def mt-loc "resources/example-files/Mt4.0/")
(def pv-loc "resources/example-files/Pv218/")
(def vv-loc "resources/example-files/Vv2.1/")

(defn process-fasta [filename category]
  {[filename category] (analyze/get-fasta-ids filename)})


  

(for [[filename [type category]] (ident/get-all-files mt-loc)]
  (case type
    :fasta (process-fasta filename category)
    nil))

  

;(analyze/get-fasta-ids 
;  (first
;  (first 
;  (filter (fn [[x [y z]]] 
;  (= :fasta y
;(ident/get-all-files mt-loc)


