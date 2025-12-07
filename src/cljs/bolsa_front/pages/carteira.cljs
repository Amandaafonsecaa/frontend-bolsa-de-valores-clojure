(ns bolsa-front.pages.carteira
  (:require [reagent.core :as r]
            [bolsa-front.layout :as layout]
            [bolsa-front.externals :as evt]
            [clojure.string]))

(defonce filtro-state (r/atom {:data-inicio ""
                               :data-fim ""
                               :pagina 1
                               :limite 5
                               :total-paginas 1
                               :total-itens 0
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

(defn normalizar-data-para-iso [data-str]
  (when (and data-str (not= data-str ""))
    (try
      (cond
        (re-find #"^\d{4}-\d{2}-\d{2}$" data-str)
        data-str

        (re-find #"^\d{2}/\d{2}/\d{4}$" data-str)
        (let [partes (clojure.string/split data-str #"/")
              dia (nth partes 0)
              mes (nth partes 1)
              ano (nth partes 2)]
          (str ano "-" mes "-" dia))

        :else
        data-str)
      (catch js/Error e
        data-str))))

(defn converter-data-para-iso [data-str]
  (normalizar-data-para-iso data-str))

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

(defn data-no-periodo?
  [data-transacao data-inicio data-fim]
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

(defn buscar-extrato-filtrado! []
  (swap! filtro-state assoc :carregando? true)
  (let [data-inicio-str (:data-inicio @filtro-state)
        data-fim-str (:data-fim @filtro-state)
        pagina (:pagina @filtro-state) 
        limite (:limite @filtro-state) 
        data-inicio-iso (normalizar-data-para-iso data-inicio-str)
        data-fim-iso (normalizar-data-para-iso data-fim-str)
        inicio-localdatetime (when data-inicio-iso
                               (str data-inicio-iso "T00:00:00"))
        fim-localdatetime (when data-fim-iso
                            (str data-fim-iso "T23:59:59"))
        params (cond-> {}
                 inicio-localdatetime (assoc :data_inicio inicio-localdatetime)
                 fim-localdatetime (assoc :data_fim fim-localdatetime)
                 true (assoc :pagina pagina)
                 true (assoc :limite limite))]
    (evt/extrato-filtrado!
      params
      (fn [resposta]
        (let [transacoes-lista (get resposta :transacoes)
              total-paginas (get resposta :total-paginas 1)
              total-itens (get resposta :total-itens 0)
              transacoes-recebidas (if (or (nil? transacoes-lista) (not (sequential? transacoes-lista)))
                                     []
                                     transacoes-lista)]
          (swap! filtro-state assoc 
                 :transacoes transacoes-recebidas
                 :total-paginas total-paginas
                 :total-itens total-itens
                 :carregando? false)))
      (fn [erro]
        (js/console.error "Erro ao buscar extrato:" erro)
        (swap! filtro-state assoc :transacoes [] :carregando? false)))))

(defn atualizar-carteira!
  []
  (buscar-extrato-filtrado!))

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
   
   [:button {:on-click (fn [] 
                         (swap! filtro-state assoc :pagina 1)
                         (buscar-extrato-filtrado!))
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
                         (swap! filtro-state assoc :data-inicio "" :data-fim "" :pagina 1)
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

(defn controles-paginacao []
  (let [{:keys [pagina total-paginas carregando?]} @filtro-state
        pode-anterior? (> pagina 1)
        pode-proximo? (< pagina total-paginas)]
    
    [:div {:style {:display "flex"
                   :justify-content "center"
                   :align-items "center"
                   :gap "10px"
                   :margin-top "20px"
                   :padding "15px"
                   :background-color "#1e1e1e"
                   :border-radius "8px"}}
     
     ;; Botão Anterior
     [:button {:on-click (fn []
                          (when pode-anterior?
                            (swap! filtro-state update :pagina dec)
                            (buscar-extrato-filtrado!)))
               :disabled (or carregando? (not pode-anterior?))
               :style {:background-color (if (and pode-anterior? (not carregando?))
                                           "#007bff"
                                           "#3a3a3a")
                       :color (if (and pode-anterior? (not carregando?))
                               "white"
                               "#666")
                       :padding "8px 16px"
                       :border "none"
                       :border-radius "4px"
                       :cursor (if (and pode-anterior? (not carregando?))
                                "pointer"
                                "not-allowed")
                       :font-weight "500"
                       :transition "all 0.2s"}}
      "← Anterior"]
     
     ;; Números das páginas
     [:div {:style {:display "flex"
                    :gap "5px"
                    :align-items "center"}}
      (for [num-pagina (range 1 (inc total-paginas))]
        [:button {:key num-pagina
                  :on-click (fn []
                             (when (and (not carregando?) (not= num-pagina pagina))
                               (swap! filtro-state assoc :pagina num-pagina)
                               (buscar-extrato-filtrado!)))
                  :disabled carregando?
                  :style {:background-color (if (= num-pagina pagina)
                                              "#007bff"
                                              "#2e2e2e")
                          :color "white"
                          :padding "8px 12px"
                          :border (if (= num-pagina pagina)
                                   "2px solid #0056b3"
                                   "1px solid #3a3a3a")
                          :border-radius "4px"
                          :cursor (if carregando? "not-allowed" "pointer")
                          :font-weight (if (= num-pagina pagina) "bold" "normal")
                          :min-width "40px"
                          :transition "all 0.2s"}}
         num-pagina])]
     
     ;; Botão Próximo
     [:button {:on-click (fn []
                          (when pode-proximo?
                            (swap! filtro-state update :pagina inc)
                            (buscar-extrato-filtrado!)))
               :disabled (or carregando? (not pode-proximo?))
               :style {:background-color (if (and pode-proximo? (not carregando?))
                                           "#007bff"
                                           "#3a3a3a")
                       :color (if (and pode-proximo? (not carregando?))
                               "white"
                               "#666")
                       :padding "8px 16px"
                       :border "none"
                       :border-radius "4px"
                       :cursor (if (and pode-proximo? (not carregando?))
                                "pointer"
                                "not-allowed")
                       :font-weight "500"
                       :transition "all 0.2s"}}
      "Próximo →"]
     
     ;; Informação da página atual
     [:div {:style {:color "#ccc"
                    :font-size "14px"
                    :margin-left "10px"
                    :padding "8px 12px"
                    :background-color "#2e2e2e"
                    :border-radius "4px"}}
      (str "Página " pagina " de " total-paginas)]]))

(defn tabela-transacoes [transacoes]
  (let [transacoes-validas (if (or (nil? transacoes) (not (sequential? transacoes)))
                             []
                             transacoes)
        {:keys [total-paginas carregando?]} @filtro-state]
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
        "🔭 Nenhuma transação encontrada no período selecionado."]
       [:div
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
                  "R$ 0,00")]]))]]
        
        (when (and (> total-paginas 1) (not carregando?))
          [controles-paginacao])])]))

(defn carteira-content []
  (r/create-class
   {:component-did-mount
    (fn []
      (swap! evt/app-state assoc :atualizar-carteira-fn buscar-extrato-filtrado!)
      (buscar-extrato-filtrado!))
    
    :component-will-unmount
    (fn []
      (swap! evt/app-state assoc :atualizar-carteira-fn nil))
    
    :reagent-render
    (fn []
      (let [{:keys [transacoes carregando?]} @filtro-state
            totais (calcular-totais transacoes)]
        [:div {:style {:color "white" :padding-top "30px" :display "flex"
                       :flex-direction "column"}}
         [:h1 {:style {:margin "20px" :font-weight "800"
                       :font-size "24px" :text-align "center"}}
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
            "❌ Erro: " (:erro @evt/app-state)])]))}))

(defn carteira-page []
  [layout/main-layout "Carteira" [carteira-content]])