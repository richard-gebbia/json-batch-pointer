(ns json-batch-pointer.core-test
  (:require [clojure.test :refer :all]
            [json-batch-pointer.core :refer :all]))

(deftest extract-test
  (testing "basics"
    (is (= {"foo" 3} (extract ["foo"] {"foo" 3 "bar" 4})))
    (is (= {"0" 3} (extract [0] [3]))))
  
  (testing "sub-selectors"
    (is (= {"foo" {"bar" 3}} (extract [{"foo" ["bar"]}] {"foo" {"bar" 3 "baz" 12} "quux" :hello}))))
  
  (testing "each-item array selectors"
    (is (= [{"foo" 3 "bar" 4} {"foo" 5 "bar" 6}] 
           (extract [["foo" "bar"]] [{"foo" 3 "bar" 4 "baz" :hello} {"foo" 5 "bar" 6 "baz" :goodbye}]))))

  (testing "nonexistent key"
    (is (= {} (extract ["foo"] {"bar" 3})))
    (is (= {} (extract ["foo"] {})))
    (is (= {"foo" {}} (extract [{"foo" ["bar"]}] {"foo" 3})))
    (is (= {} (extract [{"foo" ["bar"]}] {"baz" 3}))))

  (testing "array length"
    (is (= {"length" 3} (extract ["length"] [1 2 3])))
    (is (= {"length" 0} (extract ["length"] [])))
    (is (= {} (extract ["length"] {"foo" "bar"}))))

  (testing "error cases"
    (is (thrown-with-msg? Exception #"Invalid selector" (extract [:foo] {:foo 3})))
    (is (thrown-with-msg? Exception #"Sub-selector error" (extract [{"foo" [:bar]}] {"foo" {:bar 3}})))
    (is (thrown-with-msg? Exception #"An each-item array selector can't be mixed with other selectors."
                          (extract [["foo"] 3] {}))))

  (testing "readme example"
    (is (= {"foo" 23
            "baz" {"quux" true
                   "bang" {"1" "wow"
                           "2" {"yello" "dello"}
                           "length" 3}
                   "pow" [{"a" 3 "b" "yes"}
                          {"a" 5}]}}
            (extract ["foo"
                      {"baz" ["quux"
                              {"bang" [1 "2" "length"]
                               "pow" [["a" "b"]]}]}
                      "nonexistent"]
                      {"foo" 23
                       "bar" "hello"
                       "baz" {"quux" true
                              "bang" [7 "wow" {"yello" "dello"}]
                              "pow" [{"a" 3 "b" "yes"}
                                     {"a" 5 "c" "no"}]}})))))
