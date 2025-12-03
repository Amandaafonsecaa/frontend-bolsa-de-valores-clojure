(ns bolsa-front.pages.carteira
  (:require [reagent.core :as r]
            [bolsa-front.layout :as layout]
            [bolsa-front.externals :as evt]))

;; Estado local para filtros de data
(defonce filtro-state (r/atom {:data-inicio ""
                               :data-fim ""
                               :transacoes []
                               :carregando? false}))

;; Função para formatar data do formato ISO para dd/mm/yyyy
(defn formatar-data [data-str]
  (if (and data-str (not= data-str ""))
    (try
      ;; Tenta parsear como ISO (yyyy-mm-dd ou yyyy-mm-ddTHH:mm:ss)
      (let [data-iso (if (re-find #"T" data-str)
                      (subs data-str 0 (clojure.string/index-of data-str "T"))
                      data-str)
            partes (clojure.string/split data-iso #"-")
            ano (nth partes 0)
            mes (nth partes 1)
            dia (nth partes 2)]
        (str dia "/" mes "/" ano))
      (catch js/Error e
        ;; Fallback: tenta usar Date
        (try
          (let [data (js/Date. data-str)
                dia (.getDate data)
                mes (inc (.getMonth data))
                ano (.getFullYear data)]
            (str (if (< dia 10) "0" "") dia "/"
                 (if (< mes 10) "0" "") mes "/"
                 ano))
          (catch js/Error e2
            data-str))))
    ""))

;; Função para converter dd/mm/yyyy para formato ISO (yyyy-mm-dd)
(defn converter-data-para-iso [data-str]
  (if (and data-str (not= data-str ""))
    (try
      (let [partes (clojure.string/split data-str #"/")
            dia (nth partes 0)
            mes (nth partes 1)
            ano (nth partes 2)]
        (str ano "-" mes "-" dia))
      (catch js/Error e
        data-str))
    nil))

;; Função para buscar extrato filtrado
(defn buscar-extrato-filtrado! []
  (swap! filtro-state assoc :carregando? true)
  (let [data-inicio (:data-inicio @filtro-state)
        data-fim (:data-fim @filtro-state)
        params (cond-> {}
                 (not= data-inicio "") (assoc :data_inicio (converter-data-para-iso data-inicio))
                 (not= data-fim "") (assoc :data_fim (converter-data-para-iso data-fim)))]
    
    (evt/extrato-filtrado! params
      (fn [transacoes]
        (swap! filtro-state assoc :transacoes transacoes :carregando? false))
      (fn [erro]
        (js/console.error "Erro ao buscar extrato:" erro)
        (swap! filtro-state assoc :carregando? false)))))

;; Calcular totais
(defn calcular-totais [transacoes]
  (let [total-transacoes (count transacoes)
        total-comprado (reduce + 0 (map :total (filter #(= (:tipo %) :compra) transacoes)))
        total-vendido (reduce + 0 (map :total (filter #(= (:tipo %) :venda) transacoes)))]
    {:total-transacoes total-transacoes
     :total-comprado total-comprado
     :total-vendido total-vendido}))

;; Componente de card de resumo
(defn card-resumo [titulo valor cor icone]
  [:div {:style {:background-color "#2e2e2e"
                 :padding "20px"
                 :border-radius "8px"
                 :box-shadow "0 4px 8px rgba(0, 0, 0, 0.2)"
                 :color "white"
                 :flex "1"
                 :min-width "250px"
                 :margin-bottom "20px"}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :margin-bottom "10px"}}
    [:span {:style {:font-size "24px" :margin-right "10px"}} icone]
    [:div {:style {:font-size "14px" :color "#ccc"}} titulo]]
   [:div {:style {:font-size "32px"
                  :font-weight "bold"
                  :color cor}}
    (if (number? valor)
      (if (= titulo "Total de Transações")
        (str valor)
        (str "R$ " (.toLocaleString (js/Number. valor) "pt-BR" {:minimumFractionDigits 2 :maximumFractionDigits 2})))
      "R$ 0,00")]])

;; Componente de filtro de data
(defn filtro-data []
  [:div {:style {:background-color "#2e2e2e"
                 :padding "20px"
                 :border-radius "8px"
                 :margin-bottom "20px"
                 :display "flex"
                 :gap "15px"
                 :align-items "flex-end"
                 :flex-wrap "wrap"}}
   [:div {:style {:flex "1" :min-width "200px"}}
    [:label {:style {:display "block"
                     :color "#ccc"
                     :margin-bottom "5px"
                     :font-size "14px"}}
     "Data Inicial"]
    [:input {:type "text"
             :placeholder "dd/mm/yyyy"
             :value (:data-inicio @filtro-state)
             :on-change (fn [e]
                         (swap! filtro-state assoc :data-inicio (-> e .-target .-value)))
             :style {:width "100%"
                     :padding "10px"
                     :background-color "#1e1e1e"
                     :border "1px solid #3a3a3a"
                     :border-radius "4px"
                     :color "white"
                     :font-size "14px"}}]]
   
   [:div {:style {:flex "1" :min-width "200px"}}
    [:label {:style {:display "block"
                     :color "#ccc"
                     :margin-bottom "5px"
                     :font-size "14px"}}
     "Data Final"]
    [:input {:type "text"
             :placeholder "dd/mm/yyyy"
             :value (:data-fim @filtro-state)
             :on-change (fn [e]
                         (swap! filtro-state assoc :data-fim (-> e .-target .-value)))
             :style {:width "100%"
                     :padding "10px"
                     :background-color "#1e1e1e"
                     :border "1px solid #3a3a3a"
                     :border-radius "4px"
                     :color "white"
                     :font-size "14px"}}]]
   
   [:button {:on-click buscar-extrato-filtrado!
             :disabled (:carregando? @filtro-state)
             :style {:background-color "#007bff"
                     :color "white"
                     :padding "10px 20px"
                     :border "none"
                     :border-radius "4px"
                     :cursor (if (:carregando? @filtro-state) "not-allowed" "pointer")
                     :font-weight "bold"
                     :display "flex"
                     :align-items "center"
                     :gap "8px"
                     :height "fit-content"}}
    [:span "🔍"]
    (if (:carregando? @filtro-state) "Filtrando..." "Filtrar")]])

;; Componente de tabela de transações
(defn tabela-transacoes [transacoes]
  [:div {:style {:background-color "#2e2e2e"
                 :padding "20px"
                 :border-radius "8px"
                 :box-shadow "0 4px 8px rgba(0, 0, 0, 0.2)"
                 :color "white"
                 :overflow-x "auto"}}
   [:h3 {:style {:margin-top "0"
                 :margin-bottom "20px"
                 :border-bottom "1px solid #3a3a3a"
                 :padding-bottom "15px"}}
    "Histórico de Transações"]
   
   (if (empty? transacoes)
     [:div {:style {:padding "40px"
                    :text-align "center"
                    :color "#ccc"}}
      "Nenhuma transação encontrada no período selecionado."]
     [:table {:style {:width "100%"
                      :border-collapse "collapse"}}
      [:thead
       [:tr
        [:th {:style {:padding "12px 15px"
                      :text-align "left"
                      :border-bottom "2px solid #3a3a3a"
                      :color "#ccc"
                      :font-weight "500"}} "Data"]
        [:th {:style {:padding "12px 15px"
                      :text-align "left"
                      :border-bottom "2px solid #3a3a3a"
                      :color "#ccc"
                      :font-weight "500"}} "Tipo"]
        [:th {:style {:padding "12px 15px"
                      :text-align "left"
                      :border-bottom "2px solid #3a3a3a"
                      :color "#ccc"
                      :font-weight "500"}} "Ticker"]
        [:th {:style {:padding "12px 15px"
                      :text-align "left"
                      :border-bottom "2px solid #3a3a3a"
                      :color "#ccc"
                      :font-weight "500"}} "Quantidade"]
        [:th {:style {:padding "12px 15px"
                      :text-align "left"
                      :border-bottom "2px solid #3a3a3a"
                      :color "#ccc"
                      :font-weight "500"}} "Preço"]
        [:th {:style {:padding "12px 15px"
                      :text-align "left"
                      :border-bottom "2px solid #3a3a3a"
                      :color "#ccc"
                      :font-weight "500"}} "Total"]]]
      [:tbody
       (for [transacao transacoes]
         (let [tipo (:tipo transacao)
               tipo-str (if (= tipo :compra) "COMPRA" "VENDA")
               cor-tipo (if (= tipo :compra) "#4CAF50" "#f44336")]
           [:tr {:key (str (:data transacao) "-" (:ticker transacao) "-" (:quantidade transacao))
                 :style {:border-bottom "1px solid #3a3a3a"}}
            [:td {:style {:padding "12px 15px"}}
             (formatar-data (:data transacao))]
            [:td {:style {:padding "12px 15px"}}
             [:span {:style {:background-color cor-tipo
                            :color "white"
                            :padding "4px 12px"
                            :border-radius "12px"
                            :font-size "12px"
                            :font-weight "bold"}}
              tipo-str]]
            [:td {:style {:padding "12px 15px"}}
             (:ticker transacao)]
            [:td {:style {:padding "12px 15px"}}
             (:quantidade transacao)]
            [:td {:style {:padding "12px 15px"}}
             (str "R$ " (.toLocaleString (js/Number. (:preco transacao)) "pt-BR" {:minimumFractionDigits 2 :maximumFractionDigits 2}))]
            [:td {:style {:padding "12px 15px"}}
             (str "R$ " (.toLocaleString (js/Number. (:total transacao)) "pt-BR" {:minimumFractionDigits 2 :maximumFractionDigits 2}))]]))]])])

;; Conteúdo principal da página
(defn carteira-content []
  (let [transacoes (:transacoes @filtro-state)
        totais (calcular-totais transacoes)]
    
    ;; Carregar extrato inicial quando o componente montar
    (r/create-class
     {:component-did-mount
      (fn []
        (buscar-extrato-filtrado!))
      
      :reagent-render
      (fn []
        [:div {:style {:color "white" :padding-top "30px"}}
         
         ;; Título
         [:h1 {:style {:margin-bottom "30px"}}
          "Extrato da Carteira"]
         
         ;; Cards de resumo
         [:div {:style {:display "flex"
                        :gap "20px"
                        :margin-bottom "30px"
                        :flex-wrap "wrap"}}
          (card-resumo "Total de Transações"
                      (:total-transacoes totais)
                      "#007bff"
                      "📊")
          (card-resumo "Total Comprado"
                      (:total-comprado totais)
                      "#4CAF50"
                      "📈")
          (card-resumo "Total Vendido"
                      (:total-vendido totais)
                      "#f44336"
                      "📉")]
         
         ;; Filtros de data
         [filtro-data]
         
         ;; Tabela de transações
         [tabela-transacoes transacoes]
         
         ;; Mensagem de erro se houver
         (when (:erro @evt/app-state)
           [:p {:style {:color "#f44336"
                       :margin-top "20px"
                       :text-align "center"}}
            "❌ Erro: " (:erro @evt/app-state)])])})))

;; Página principal
(defn carteira-page []
  [layout/main-layout "Carteira" [carteira-content]])

