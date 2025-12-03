(ns bolsa-front.pages.buysell
  (:require [bolsa-front.layout :as layout]
            [bolsa-front.externals :as evt]))

;; Conteúdo da Tela de Cotação
(defn buysell-content []
  [:div {:style {:color "white" :padding "50px" :text-align "center"}}
   [:h1 "🔍 Consultar e Negociar"]
   [:p "Esta é a página buysell.cljs funcionando!"]
   
   ;; Só pra testar se o estado funciona
   [:p "Saldo atual: " (:saldo @evt/app-state)]])

(defn buysell-page []
  ;; Chama o layout principal envolvendo o conteúdo desta página
  [layout/main-layout "Cotação" [buysell-content]])