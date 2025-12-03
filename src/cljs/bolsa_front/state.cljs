(ns bolsa-front.state
  (:require [reagent.core :as r]))

;; esse é o tado global para a página atual (compartilhado entre core e layout)
(defonce current-page (r/atom "Dashboard"))

