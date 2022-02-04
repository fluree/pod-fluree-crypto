(ns pod.fluree.crypto-impl
  (:require [fluree.crypto]
            [bencode.core :as bencode]
            [clojure.edn :as edn]
            [clojure.walk :as walk]
            [clojure.string :as str])
  (:import (java.io EOFException PushbackInputStream OutputStream))
  (:gen-class))

(set! *warn-on-reflection* true)

(def debug? true)

(defn debug [& msg]
  (when debug?
    (binding [*out* *err*]
      (apply println msg))))

(def stdin (PushbackInputStream. System/in))
(def stdout System/out)

(defn read-bencode [in]
  (bencode/read-bencode in))

(defn write-bencode [^OutputStream out v]
  (bencode/write-bencode out v)
  (.flush out))

(defn bytes->string [^bytes bs]
  (String. bs))

(defn podify-namespace
  ([ns] (podify-namespace ns *ns* nil))
  ([ns ns-prefix] (podify-namespace ns ns-prefix nil))
  ([ns ns-prefix pod-name]
   (let [pod-ns (if pod-name
                  [ns-prefix pod-name]
                  [ns-prefix])
         vars   (-> ns ns-publics keys)]
     {:name (symbol (str/join "." pod-ns))
      :vars (mapv (fn [v] {:name v}) vars)})))

(def describe-map
  (delay
    (walk/postwalk
      #(if (ident? %)
         (name %)
         %)
      {:format     :edn
       :namespaces [(podify-namespace 'fluree.crypto 'pod.fluree.crypto)]})))

(defn invoke-fn [id message]
  (let [var      (-> message
                     (get "var")
                     bytes->string
                     symbol)
        var-ns   (-> var namespace (str/split #"[.]") rest (->> (str/join "."))
                     symbol) ; drop leading pod.
        var-name (-> var name symbol)
        _        (debug "invoke var:" (str var-ns "/" var-name))
        f        (ns-resolve var-ns var-name)
        _        (debug "resolved var:" (pr-str f))
        args     (-> message (get "args") bytes->string
                     edn/read-string)
        _        (debug "args:" (pr-str args))
        result   (apply f args)]
    {"value"  (pr-str result)
     "id"     id
     "status" ["done"]}))

(defn error-response [id throwable]
  {"ex-message" (ex-message throwable)
   "ex-data"    (pr-str
                  (assoc (ex-data throwable)
                    :type
                    (str (class throwable))))
   "id"         id
   "status"     ["done" "error"]})

(defn -main []
  (loop []
    (let [message (try (read-bencode stdin)
                       (catch EOFException _
                         ::EOF))]
      (when-not (identical? ::EOF message)
        (let [op (-> message (get "op") bytes->string keyword)
              id (or (some-> message (get "id") bytes->string)
                     "unknown")]
          (case op
            :describe (do
                        (debug "got :describe")
                        (debug "responding with" (pr-str @describe-map))
                        (write-bencode stdout @describe-map)
                        (recur))
            :invoke (do
                      (debug "got :invoke")
                      (try
                        (let [response (invoke-fn id message)]
                          (debug "responding with" (pr-str response))
                          (write-bencode stdout response))
                        (catch Throwable e
                          (->> e (error-response id) (write-bencode stdout))))
                      (recur))
            :shutdown (System/exit 0)
            (do
              (let [response {"ex-message" "Unknown op"
                              "ex-data"    (pr-str {:op op})
                              "id"         id
                              "status"     ["done" "error"]}]
                (write-bencode stdout response))
              (recur))))))))
