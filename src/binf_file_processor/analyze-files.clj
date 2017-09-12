(ns binf-file-processor.analyze-files
  (:require [opennlp.nlp :as nlp]
            [opennlp.treebank :as treebank]
            [opennlp.tools.train :as train]
            [me.raynes.fs :as fs]
            [instaparse.core :as insta]
            [biotools.fasta :as fasta]
            [clojure.core.matrix :as matrix]
            [clojure.core.async :as async 
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]))

(defn get-fasta-ids [filename]
  (fasta/process-fasta filename
    (:id entry)))
