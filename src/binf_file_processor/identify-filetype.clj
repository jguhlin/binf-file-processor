(ns binf-file-processor.identify-filetype
  (:require [opennlp.nlp :as nlp]
            [opennlp.treebank :as treebank]
            [opennlp.tools.train :as train]
            [me.raynes.fs :as fs]
            [instaparse.core :as insta]
            [clojure.core.matrix :as matrix]
            [clojure.core.async :as async 
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]))

(matrix/set-current-implementation :vectorz)

(def tokenize (nlp/make-tokenizer "resources/opennlp-models/en-token.bin"))

; Come up with a good way to detect file types-- confirm by having biotools interrogate
; Then... using the same NLP approach, try to determine a way to generate a graph
; in core.matrix from all of the file types, so that they are ultimately connected
; Genome -> Gene -> mRNA -> Protein ... etc
;             --> Pathway Entry
;             --> IP Scan Entry
;             .........................etc
; Basically use the NLP for the chatbot to determine how the files fit together
; and come up with a good model, instead of heuristically trying to guess each time...

(def tokenize-filename 
  "Converts filenames into individual tokens"
  (insta/parser
    "<FT> = (DATUM | !DATUM <IG+>)*
     IG = ('_'|'.'|'0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9')
     DATUM = #'[a-zA-Z]+'"))

(defn process-filename [filename]
  "Returns a lowercase, tokenized version of a filename for further processing."
  (->> filename
       clojure.string/lower-case
       tokenize-filename
       (map second)
       (into #{})))

(defn identify-fasta-category [filename]
  "Some files must go into a category, such as 'protein', 'mRNA', 'nucleic acid sequence', or 'whole genome.'
  This function attempts to categorize them with a variety of methods."
  (condp (fn [x y] (contains? y x)) (process-filename filename)
    "prot"            :protein
    "proteinseq"      :protein
    "protein"         :protein
    "genesproteinseq" :protein
    "proteinseq"      :protein
    "mRNA"            :mRNA
    "coding"          :mRNA
    "transcript"      :mRNA
    "transcripts"     :mRNA
    "cds"             :dna
    "exons"           :dna
    "gene"            :dna
    "genes"           :dna
    "chr"             :dna
    "assembly"        :assembly
                      :unknown))
   
(defn identify-type [filename]
  "Identify file type, usually, but not always, by file extension"
  (condp (fn [x y] (contains? y x)) (process-filename filename)
    "gff3"     [:gff3     :features]
    "gff"      [:gff3     :features]
    "fasta"    [:fasta    (identify-fasta-category filename)]
    "fa"       [:fasta    (identify-fasta-category filename)]
    "fna"      [:fasta    (identify-fasta-category filename)]
    "fnn"      [:fasta    (identify-fasta-category filename)]
    "prot"     [:fasta    (identify-fasta-category filename)] ; Probably
    "tsv"      [:tsv      :text]
    "obo"      [:obo      :ontology]
    "pathway"  [:pathway  :pathway]
    "pathways" [:pathways :pathway]
               [:unknown  :unknown]))

(defn identify-file 
  "Identifies a single file to identify the proper interpreter"
  [file-obj]
  (let [filename (.getName file-obj)]
    [(.getAbsolutePath file-obj)
     (identify-type filename)]))

(defn get-all-files 
  "Get a list of all files in a directory and pair them with the proper interpreters"
  [dir]
  (let [files (fs/list-dir dir)]
    (map identify-file files)))
  
