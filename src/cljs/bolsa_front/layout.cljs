(ns bolsa-front.layout
  (:require [reagent.core :as r]
            [bolsa-front.externals :as evt])) ;; <--- Usa o externals (Onde está o app-state)

(defn nav-item [label page-id current-page hash]
  [:a {:href (str "#/" hash) 
       :style {:color (if (= page-id current-page) "#007bff" "#ccc")
               :margin-right "25px"
               :padding-bottom "10px"
               :border-bottom (if (= page-id current-page) "2px solid #007bff" "2px solid transparent")
               :text-decoration "none"
               :font-weight "500"
               :cursor "pointer"}}
   label])

(defn nav-bar [] 
  (let [page-atual (:pagina-atual @evt/app-state)] 
    [:nav {:style {:background-color "#1e1e1e"
                   :padding "20px 40px"
                   :border-bottom "1px solid #3a3a3a"
                   :display "flex"
                   :align-items "center"
                   :position "sticky" :top "0" :z-index "50"}}

     ;; Logo
     [:div {:style {:flex-grow 1 :display "flex" :align-items "center"}}
      [:h2 {:style {:font-size "24px" :color "white" :margin "0 10px 0 0"}} "UniBolsa"]
      ]

     ;; Links da Navegação
     ;; Sintaxe: (nav-item "Texto" :chave-que-o-core-usa pagina-atual "link-da-url")
     [:div {:style {:display "flex"}}
      (nav-item "Dashboard"  :home      page-atual "dashboard") ;; Use :home para bater com o core
      (nav-item "Transações"    :buysell   page-atual "cotacao")
      (nav-item "Cotação" :cotacao page-atual "transacoes")
      (nav-item "Carteira"   :carteira  page-atual "carteira")]]))

;; Layout Principal
(defn main-layout [titulo content] ;; Ajustei argumentos para bater com a chamada da pagina
  [:div {:style {:min-height "100vh"
                 :background-color "#121212"
                 :font-family "sans-serif"}}
   [nav-bar] 
   [:div {:style {:max-width "1200px"
                  :margin "0 auto"
                  :padding "30px 20px"}}
    content]])