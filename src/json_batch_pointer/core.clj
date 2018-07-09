(ns json-batch-pointer.core)

(defn what-is
  [pred val]
  (pred val))

(defn extract-field
  [field json]
  (condp what-is field
    ;; special case, getting the length of an array
    #(= "length" %)
      (when (vector? json) (count json))

    string?
      ;; first just try to grab the value from the object
      (let [val (get json field)]
        (or val
            ;; if we can't, try and parse it as a number and get a vector value
            (try (->> (Integer/parseUnsignedInt field)
                      (get json))
                 (catch NumberFormatException _ nil))))
    
    number? 
      (get json field)
      
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