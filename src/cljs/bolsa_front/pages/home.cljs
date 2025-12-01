(ns bolsa-front.pages.home
  (:require [bolsa-front.externals :as evt])) ;; Importa a lógica

(defn home-page []
  ;; 1. Pega o estado atual (o "ouvido" do Reagent)
  (let [estado @evt/app-state]
    
    [:div {:style {:padding "50px" :text-align "center" :font-family "sans-serif"}}
     
     ;; Título
     [:h1 "🧪 Teste de Conexão"]
     
     ;; Mostrador de Saldo (Vem do Backend)
     [:div {:style {:margin "20px" :font-size "24px"}}
      [:strong "Saldo Atual: "]
      [:span {:style {:color "blue"}} 
       (str "R$ " (:saldo estado))]]

     ;; Botão de Teste
     [:button {:on-click evt/atualizar-tudo!
               :style {:background-color "#4CAF50" ;; Verde
                       :color "white"
                       :padding "15px 32px"
                       :font-size "16px"
                       :border "none"
                       :cursor "pointer"}}
      (if (:carregando? estado) 
        "⏳ Buscando..." 
        "🔄 Testar Conexão Agora")]
     
     ;; Debug: Se der erro, mostra aqui
     (when (:erro estado)
       [:p {:style {:color "red" :margin-top "20px"}} 
        "❌ Erro: " (:erro estado)])]))