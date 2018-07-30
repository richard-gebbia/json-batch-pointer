(ns json-batch-pointer.core
  (:require [cheshire.core :as json]))

(defn what-is
  [pred val]
  (pred val))

(defn neg-int?
  [x]
  (and (integer? x) (neg? x)))

(defn extract-index
  [index json]
  (if (neg-int? index)
    (get json (+ index (count json)))
    (get json index)))

(defn extract-field
  [field json]
  (condp what-is field
    ;; special case, getting the length of an array
    #(and (vector? json) (= "length" %))
      (count json)

    ;; special case, getting the last item in an array, like how it's done in JSON Patch
    #(and (vector? json) (= "-" %))
      (last json)

    string?
      ;; first just try to grab the value from the object
      (let [val (get json field)]
        (or val
            ;; if we can't, try and parse it as a number and get a vector value
            (try
              (let [index (Integer/parseInt field)]
                (extract-index index json))
              (catch NumberFormatException _ nil))))
    
    integer?
      (extract-index field json)
      
    (throw (ex-info "Invalid selector" {:selector field}))))

(defn maybe-assoc
  "Like `assoc` except it's a no-op if the value to associate is `nil`."
  [m k v]
  (if (some? v) (assoc m k v) m))

(declare extract)

(defn sub-extract
  [ptr json]
  (try
    (extract ptr json)
    (catch Exception e (throw (ex-info "Sub-selector error"
      {:ptr ptr
       :exception e})))))

(defn extract
  [ptr json]
  (when-let [array-selector (and (< 1 (count ptr)) (first (filter vector? ptr)))]
    (throw (ex-info "An each-item array selector can't be mixed with other selectors."
                    {:item array-selector})))
  (if-let [array-selector (and (= 1 (count ptr)) (vector? (first ptr)) (first ptr))]
    (mapv #(extract array-selector %) json)
    (reduce (fn [state ptr-elem]
                (if (map? ptr-elem)
                  (reduce (fn [state [k v]] 
                            (or (some->> (get json k)
                                         (sub-extract v)
                                         (maybe-assoc state k))
                                state))
                            state
                            ptr-elem)
                  (maybe-assoc state (str ptr-elem) (extract-field ptr-elem json))))
            {}
            ptr)))

(defn extract-str
  [ptr-str json-str]
  (json/encode (extract (json/decode ptr-str) (json/decode json-str))))