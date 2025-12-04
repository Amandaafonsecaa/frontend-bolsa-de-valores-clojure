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
    (if (number? value)
      (str "R$ " (.toLocaleString (js/Number. value) "pt-BR" {:minimumFractionDigits 2 :maximumFractionDigits 2}))
      "R$ 0,00")]
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

(defn holdings-table [saldo-por-ativo]
  (let [holdings-list (if (map? saldo-por-ativo)
                        (->> saldo-por-ativo
                             (filter (fn [[ticker qtd]] 
                                      (and ticker 
                                           qtd 
                                           (number? qtd) 
                                           (pos? qtd))))
                             (map (fn [[ticker qtd]] {:ticker (str ticker) :quantidade qtd}))
                             (filter (fn [acao] (and (:ticker acao) (not (empty? (:ticker acao)))))))
                        [])]
    [:div {:style {:background-color "#2e2e2e"
                   :padding "20px"
                   :border-radius "8px"
                   :margin-top "30px"
                   :box-shadow "0 4px 8px rgba(0, 0, 0, 0.2)"
                   :color "white"}}
     [:h3 {:style {:margin-top "0" :border-bottom "1px solid #3a3a3a" :padding-bottom "15px"}}
      "Saldo por Ativo"]

     [:table {:style {:width "100%" :border-collapse "collapse"}}

      (table-header ["Símbolo da Ação" "Quantidade"])

      [:tbody
       (if (empty? holdings-list)
         [:tr {:key "empty"}
          [:td {:col-span 2 :style {:padding "15px" :text-align "center" :color "#ccc"}}
           "Nenhum ativo encontrado na carteira."]]
         (doall (for [[idx acao] (map-indexed vector holdings-list)]
                  (let [ticker (:ticker acao)
                        quantidade (:quantidade acao)]
                    [:tr {:key (str "holding-" idx "-" ticker)}
                     [:td {:style {:padding "12px 15px" :border-bottom "1px solid #3a3a3a"}}
                      ticker]
                     [:td {:style {:padding "12px 15px" :border-bottom "1px solid #3a3a3a"}}
                      (str quantidade)]]))))]]]))


(defn dashboard-content []
  (let [estado @evt/app-state
        saldo-por-ativo (:saldo-por-ativo estado)
        total-investido (:total-investido estado)
        carregando? (:carregando? estado)
        erro (:erro estado)
        patrimonio (:patrimonio estado)
        patrimonio-liquido (if (number? patrimonio) patrimonio 0)
        total-investido-num (if (number? total-investido) total-investido 0)
        lucro-prejuizo (- patrimonio-liquido total-investido-num)
        percentual-lucro (if (and (number? total-investido-num) (pos? total-investido-num))
                           (* 100 (/ lucro-prejuizo total-investido-num))
                           0)]

    [:div {:style {:color "white"}}
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
       (if carregando? "Carregando..." " Recarregar Dados")]]
     [:div {:style {:display "flex" :gap "20px" :margin-bottom "30px" :flex-wrap "wrap"}}
      (metric-card "Patrimônio Líquido"
                   patrimonio-liquido
                   (str (if (pos? lucro-prejuizo) "↑" "↓")
                        " R$ " (.toLocaleString (js/Number. (Math/abs lucro-prejuizo)) "pt-BR" {:minimumFractionDigits 2 :maximumFractionDigits 2})
                        " (" (.toFixed percentual-lucro 2) "%)"))
      (metric-card "Valor Total Investido"
                   total-investido-num
                   nil)]
     (holdings-table saldo-por-ativo)

     (when erro
       [:p {:style {:color "red" :margin-top "20px" :text-align "center"}}
        "❌ Erro: " erro])

     [:p {:style {:color "#ccc" :text-align "center" :margin-top "50px" :font-size "12px"}}
      "Dados fornecidos pela BrAPI - Atualização em tempo real"]]))



(defn dashboard-page []
  [layout/main-layout "Dashboard" [dashboard-content]])
