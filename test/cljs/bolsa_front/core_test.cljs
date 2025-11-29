(ns bolsa-front.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [pjstadig.humane-test-output]
            [reagent.core :as reagent :refer [atom]]
            [bolsa-front.core :as rc]))

(deftest test-home
  (is (= true true)))

