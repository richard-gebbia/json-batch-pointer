package com.github.richardgebbia.jsonbatchpointer;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public final class JsonBatchPointer {
    private static IFn extractFn = null;

    public static String extract(String pointer, String jsonDoc) {
        if (extractFn == null) {
            extractFn = loadExtractFn();
        }

        return (String)extractFn.invoke(pointer, jsonDoc);
    }

    private static IFn loadExtractFn() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("json-batch-pointer.core"));

        return Clojure.var("json-batch-pointer.core", "extract-str");
    }
}