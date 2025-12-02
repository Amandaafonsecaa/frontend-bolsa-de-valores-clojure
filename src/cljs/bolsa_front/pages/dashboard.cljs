(ns bolsa-front.pages.dashboard
  (:require [bolsa-front.externals :as evt]
            [bolsa-front.layout :as layout]))  

(defn metric-card [title value subtitle]
  [:div {:style {:background-color "#2e2e2e"
                 :padding "20px"
                 :border-radius "8px"
                 :box-shadow "0 4px 8px rgba(0, 0, 0, 0.2)"
                 :color "white"
                 :flex "1"
                 :min-width "300px"}}
   [:div {:style {:font-size "14px" :color "#ccc" :margin-bottom "10px"}}
    title]
   [:div {:style {:font-size "32px" :font-weight "bold" :margin-bottom "5px"}}
    (str "R$ " (.toFixed value 2))]
   (when subtitle
     [:div {:style {:font-size "12px" :color "#4CAF50"}}
      subtitle])])

(defn table-header [columns]
  [:thead
   [:tr
    (for [col columns] 
      [:th {:key col
            :style {:padding "12px 15px"
                    :text-align "left"
                    :border-bottom "2px solid #3a3a3a"}} col])]])

;; Componente para o corpo da tabela de ativos
(defn holdings-table [holdings]
  [:div {:style {:background-color "#2e2e2e"
                 :padding "20px"
                 :border-radius "8px"
                 :margin-top "30px"
                 :box-shadow "0 4px 8px rgba(0, 0, 0, 0.2)"
                 :color "white"}}
   [:h3 {:style {:margin-top "0" :border-bottom "1px solid #3a3a3a" :padding-bottom "15px"}}
    "Current Holdings (Saldo por Ativo)"]

   [:table {:style {:width "100%" :border-collapse "collapse"}}

    (table-header ["Ticker Symbol" "Quantity"])

    [:tbody
     (if (empty? holdings)
       [:tr
        [:td {:col-span 2 :style {:padding "15px" :text-align "center" :color "#ccc"}}
         "Nenhum ativo encontrado no extrato."]]
       (for [acao holdings]
         [:tr {:key (:ticker acao)} ;; Chave única para cada linha
          [:td {:style {:padding "12px 15px" :border-bottom "1px solid #3a3a3a"}}
           (:ticker acao)]
          [:td {:style {:padding "12px 15px" :border-bottom "1px solid #3a3a3a"}}
           (:quantidade acao)]]))]]])


(defn dashboard-content []
  ;; estado atual
  (let [estado @evt/app-state
        acoes (:acoes estado)
        saldo (:saldo estado)
        total-investido (:total-investido estado)
        lucro-prejuizo (:lucro-prejuizo estado)
        carregando? (:carregando? estado)
        erro (:erro estado)

        patrimonio-liquido (+ saldo total-investido)
        percentual-lucro (if (pos? total-investido)
                           (* 100 (/ lucro-prejuizo total-investido))
                           0)]

    [:div {:style {:color "white"}}

     ;; titulo principal e botão de refresh
     [:div {:style {:display "flex"
                    :justify-content "space-between"
                    :align-items "center"
                    :margin-bottom "30px"
                    :margin-top "30px"}}
      [:h1 "Dashboard"]

      [:button {:on-click evt/atualizar-tudo!
                :disabled carregando?
                :style {:background-color "#007bff"
                        :color "white"
                        :padding "10px 20px"
                        :border "none"
                        :border-radius "4px"
                        :cursor "pointer"
                        :font-weight "bold"}}
       (if carregando? "⏳ Carregando..." "🔄 Refresh Data")]]

     ;; cards de métricas
     [:div {:style {:display "flex" :gap "20px" :margin-bottom "30px" :flex-wrap "wrap"}}
      ;; card do patrimônio líquido
      (metric-card "Net Worth (Patrimônio Líquido)"
                   patrimonio-liquido
                   (str (if (pos? lucro-prejuizo) "↑" "↓")
                        " R$ " (.toFixed lucro-prejuizo 2)
                        " (" (.toFixed percentual-lucro 2) "%)"))

      ;; card do valor total investido
      (metric-card "Total Invested (Valor Total Investido)"
                   total-investido
                   nil)]
     ;; tabela das posições atuais
     (holdings-table acoes)

     (when erro
       [:p {:style {:color "red" :margin-top "20px" :text-align "center"}}
        "❌ Erro: " erro])

     [:p {:style {:color "#ccc" :text-align "center" :margin-top "50px" :font-size "12px"}}
      "Dados fornecidos pela BrAPI - Atualização em tempo real"]]))



(defn dashboard-page []
  ;; a página é envolvida pelo layout principal, passando o conteúdo
  [layout/main-layout "Dashboard" [dashboard-content]])
