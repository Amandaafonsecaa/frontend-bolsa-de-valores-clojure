(ns bolsa-front.app
  (:require [bolsa-front.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
