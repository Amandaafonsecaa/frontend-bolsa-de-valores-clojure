(ns bolsa-front.pages.carteira
  (:require [reagent.core :as r]
            [bolsa-front.layout :as layout]
            [bolsa-front.externals :as evt]
            [clojure.string]))

(defonce filtro-state (r/atom {:data-inicio ""
                               :data-fim ""
                               :transacoes []
                               :carregando? false}))

(defn eh-compra? [tipo]
  (if (nil? tipo)
    false
    (let [tipo-str (if (keyword? tipo) (name tipo) (str tipo))]
      (= tipo-str "compra"))))

(defn eh-venda? [tipo]
  (if (nil? tipo)
    false
    (let [tipo-str (if (keyword? tipo) (name tipo) (str tipo))]
      (= tipo-str "venda"))))

(defn formatar-data [data-str]
  (if (and data-str (not= data-str ""))
    (try
      (let [data-iso (if (re-find #"T" data-str)
                      (subs data-str 0 (clojure.string/index-of data-str "T"))
                      data-str)
            partes (clojure.string/split data-iso #"-")
            ano (nth partes 0)
            mes (nth partes 1)
            dia (nth partes 2)]
        (str dia "/" mes "/" ano))
      (catch js/Error e
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

(defn converter-data-para-iso [data-str]
  (if (and data-str (not= data-str ""))
    (try
      (if (re-find #"^\d{4}-\d{2}-\d{2}$" data-str)
        data-str
        (let [partes (clojure.string/split data-str #"/")
              dia (nth partes 0)
              mes (nth partes 1)
              ano (nth partes 2)]
          (str ano "-" mes "-" dia)))
      (catch js/Error e
        data-str))
    nil))

(defn converter-data-iso-para-input [data-iso]
  (if (and data-iso (not= data-iso ""))
    (try
      (if (re-find #"^\d{4}-\d{2}-\d{2}$" data-iso)
        data-iso
        (let [partes (clojure.string/split data-iso #"/")
              dia (nth partes 0)
              mes (nth partes 1)
              ano (nth partes 2)]
          (str ano "-" mes "-" dia)))
      (catch js/Error e
        data-iso))
    ""))

(defn extrair-data-sem-hora [data-completa]
  (if (and data-completa (not= data-completa ""))
    (try
      (let [data-str (str data-completa)
            pos-t (clojure.string/index-of data-str "T")]
        (if pos-t
          (subs data-str 0 pos-t)
          (if (re-find #"^\d{4}-\d{2}-\d{2}" data-str)
            (subs data-str 0 10)
            data-str)))
      (catch js/Error e
        (str data-completa)))
    ""))

(defn data-no-periodo? [data-transacao data-inicio data-fim]
  (let [data-transacao-iso (extrair-data-sem-hora data-transacao)
        data-inicio-iso (if (and data-inicio (not= data-inicio "")) (converter-data-para-iso data-inicio) nil)
        data-fim-iso (if (and data-fim (not= data-fim "")) (converter-data-para-iso data-fim) nil)]
    (cond
      (or (nil? data-transacao-iso) (= data-transacao-iso "")) false
      (and (nil? data-inicio-iso) (nil? data-fim-iso)) true
      (and data-inicio-iso data-fim-iso)
      (and (>= (compare data-transacao-iso data-inicio-iso) 0)
           (<= (compare data-transacao-iso data-fim-iso) 0))
      data-inicio-iso (>= (compare data-transacao-iso data-inicio-iso) 0)
      data-fim-iso (<= (compare data-transacao-iso data-fim-iso) 0)
      :else true)))

(defn filtrar-transacoes-por-data [transacoes data-inicio data-fim]
  (if (or (nil? transacoes) (not (sequential? transacoes)))
    []
    (if (and (or (nil? data-inicio) (= data-inicio ""))
             (or (nil? data-fim) (= data-fim "")))
      transacoes
      (filter (fn [transacao]
                (data-no-periodo? (:data transacao) data-inicio data-fim))
              transacoes))))

(defn buscar-extrato-filtrado! []
  (swap! filtro-state assoc :carregando? true)
  (let [data-inicio (:data-inicio @filtro-state)
        data-fim (:data-fim @filtro-state)
        params (cond-> {}
                 (not= data-inicio "") (assoc :data_inicio (converter-data-para-iso data-inicio))
                 (not= data-fim "") (assoc :data_fim (converter-data-para-iso data-fim)))]
    (js/console.log "🔍 Buscando extrato com filtros - Data início:" data-inicio "Data fim:" data-fim)
    (js/console.log "🔍 Parâmetros enviados ao backend:" params)
    (evt/extrato-filtrado! params
      (fn [transacoes]
        (let [transacoes-recebidas (if (or (nil? transacoes) (not (sequential? transacoes)))
                                     []
                                     transacoes)
              transacoes-filtradas (filtrar-transacoes-por-data transacoes-recebidas data-inicio data-fim)]
          (js/console.log "📥 Transações recebidas do backend (total):" (count transacoes-recebidas))
          (js/console.log "📋 Datas das transações recebidas:" (map #(:data %) transacoes-recebidas))
          (js/console.log "✅ Transações após filtro de data no frontend (total):" (count transacoes-filtradas))
          (js/console.log "📋 Datas das transações filtradas:" (map #(:data %) transacoes-filtradas))
          (swap! filtro-state assoc :transacoes transacoes-filtradas :carregando? false)))
      (fn [erro]
        (js/console.error "Erro ao buscar extrato:" erro)
        (swap! filtro-state assoc :transacoes [] :carregando? false)))))

(defn calcular-totais [transacoes]
  (let [transacoes-validas (if (or (nil? transacoes) (not (sequential? transacoes)))
                             []
                             transacoes)
        total-transacoes (count transacoes-validas)
        total-comprado (if (empty? transacoes-validas)
                         0.0
                         (reduce + 0.0 (map #(or (:total %) 0.0) (filter #(eh-compra? (:tipo %)) transacoes-validas))))
        total-vendido (if (empty? transacoes-validas)
                        0.0
                        (reduce + 0.0 (map #(or (:total %) 0.0) (filter #(eh-venda? (:tipo %)) transacoes-validas))))]
    {:total-transacoes total-transacoes
     :total-comprado total-comprado
     :total-vendido total-vendido}))

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
    [:input {:type "date"
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
    [:input {:type "date"
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
    (if (:carregando? @filtro-state) "Filtrando..." "Filtrar")]
   
   [:button {:on-click (fn []
                         (swap! filtro-state assoc :data-inicio "" :data-fim "")
                         (buscar-extrato-filtrado!))
             :disabled (:carregando? @filtro-state)
             :style {:background-color "#6c757d"
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
    [:span "🗑️"]
    "Limpar"]])

(defn tabela-transacoes [transacoes]
  (let [transacoes-validas (if (or (nil? transacoes) (not (sequential? transacoes)))
                             []
                             transacoes)]
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
     
     (if (empty? transacoes-validas)
       [:div {:style {:padding "40px"
                      :text-align "center"
                      :color "#ccc"
                      :font-size "16px"}}
        "📭 Nenhuma transação encontrada no período selecionado."]
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
       (for [[idx transacao] (map-indexed vector transacoes-validas)]
         (let [tipo (:tipo transacao)
               compra? (eh-compra? tipo)
               tipo-str (if compra? "COMPRA" "VENDA")
               cor-tipo (if compra? "#4CAF50" "#f44336")]
           [:tr {:key (str idx "-" (:data transacao) "-" (:ticker transacao) "-" (:quantidade transacao))
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
             (str (:ticker transacao))]
            [:td {:style {:padding "12px 15px"}}
             (str (:quantidade transacao))]
            [:td {:style {:padding "12px 15px"}}
             (if (:preco transacao)
               (str "R$ " (.toLocaleString (js/Number. (:preco transacao)) "pt-BR" {:minimumFractionDigits 2 :maximumFractionDigits 2}))
               "R$ 0,00")]
            [:td {:style {:padding "12px 15px"}}
             (if (:total transacao)
               (str "R$ " (.toLocaleString (js/Number. (:total transacao)) "pt-BR" {:minimumFractionDigits 2 :maximumFractionDigits 2}))
               "R$ 0,00")]]))]])])))

(defn carteira-content []
  (let [transacoes (:transacoes @filtro-state)
        totais (calcular-totais transacoes)]
    
    (r/create-class
     {:component-did-mount
      (fn []
        (buscar-extrato-filtrado!))
      
      :reagent-render
      (fn []
        [:div {:style {:color "white" :padding-top "30px"}}
         [:h1 {:style {:margin-bottom "30px"}}
          "Extrato da Carteira"]
         
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
         
         [filtro-data]
         [tabela-transacoes transacoes]
         
         (when (:erro @evt/app-state)
           [:p {:style {:color "#f44336"
                       :margin-top "20px"
                       :text-align "center"}}
            "❌ Erro: " (:erro @evt/app-state)])])})))

(defn carteira-page []
  [layout/main-layout "Carteira" [carteira-content]])

