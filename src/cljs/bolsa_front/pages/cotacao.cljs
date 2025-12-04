(ns bolsa-front.pages.cotacao
  (:require [reagent.core :as r]
            [bolsa-front.externals :as evt]
            [bolsa-front.layout :as layout]
            [clojure.string :as str]))

(defn format-money [value]
  (if (number? value)
    (str "R$ " (.toLocaleString (js/Number. value) "pt-BR" {:minimumFractionDigits 2 :maximumFractionDigits 2}))
    "R$ 0,00"))

(defn search-input [local-term]
  [:form {:on-submit (fn [e]
                       (.preventDefault e)
                       (when-not (str/blank? @local-term)
                         (evt/buscar-acao! @local-term)))
          :style {:position "relative"
                  :width "100%"
                  :max-width "600px"
                  :margin-bottom "30px"}}
   
   ;; Ícone Lupa (SVG Absolute)
   [:div {:style {:position "absolute"
                  :left "15px"
                  :top "26%"
                  :transform "translateY(-50%)"
                  :color "#ccc"}}
    [:svg {:width "20" :height "20" :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
     [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" 
             :d "M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"}]]]

   ;; Input
   [:input {:type "text"
            :placeholder "Digite o código (ex: PETR4, VALE3)..."
            :value @local-term
            :on-change #(reset! local-term (-> % .-target .-value))
            :style {:width "100%"
                    :padding "15px 15px 15px 50px" ;; Espaço para a lupa
                    :font-size "16px"
                    :background-color "#2e2e2e"
                    :border "1px solid #3a3a3a"
                    :border-radius "8px"
                    :color "white"
                    :outline "none"
                    :box-shadow "0 4px 8px rgba(0, 0, 0, 0.2)"}}]

   [:button {:type "submit"
             :style {:margin-top "15px"
                     :width "100%"
                     :background-color "#007bff"
                     :color "white"
                     :padding "12px 20px"
                     :border "none"
                     :border-radius "4px"
                     :cursor "pointer"
                     :font-weight "bold"
                     :font-size "14px"}}
    "CONSULTAR"]])

(defn result-card [data]
  (let [preco (:ultimo-preco data)
        abertura (:preco-abertura data)
        maximo (:preco-maximo data)
        minimo (:preco-minimo data)
        fechamento (:preco-fechamento data)
        hora (:hora data)
        variacao (if (and preco fechamento (not= fechamento 0))
                   (* (/ (- preco fechamento) fechamento) 100)
                   0.0)
        positivo? (>= variacao 0)
        cor-variacao (if positivo? "#4CAF50" "#ef4444")] ;; Verde Dashboard ou Vermelho

    [:div {:style {:background-color "#2e2e2e"
                   :padding "30px"
                   :border-radius "8px"
                   :box-shadow "0 4px 8px rgba(0, 0, 0, 0.2)"
                   :color "white"
                   :margin-top "20px"
                   :max-width "700px"
                   :width "100%"
                   :text-align "center"}}

     ;; Ticker Header
     [:div {:style {:font-size "36px"
                    :font-weight "bold"
                    :margin-bottom "5px"
                    :color "white"}}
      (:nome-curto data)]

     ;; Subtitulo (Nome Longo)
     [:div {:style {:font-size "14px"
                    :color "#ccc"
                    :margin-bottom "20px"}}
      (:nome data)]

     ;; Divisor
     [:div {:style {:border-bottom "1px solid #3a3a3a"
                    :margin-bottom "20px"}}]

     ;; Preço Principal
     [:div {:style {:font-size "48px"
                    :font-weight "bold"
                    :margin-bottom "10px"}}
      (format-money preco)]

     ;; Badge de Variação
     [:div {:style {:display "inline-block"
                    :padding "8px 16px"
                    :border-radius "4px"
                    :background-color (if positivo? "rgba(76, 175, 80, 0.1)"
                                          "rgba(239, 68, 68, 0.1)")
                    :color cor-variacao
                    :font-weight "bold"
                    :font-size "18px"
                    :margin-bottom "20px"}}
      (str (if positivo? "▲ " "▼ ")
           (.toFixed (js/Math.abs variacao) 2)
           "%")]

     ;; Linha de detalhes (Abertura, Máximo, Mínimo, Fechamento, Hora)
     [:div {:style {:display "grid"
                    :grid-template-columns "repeat(auto-fit, minmax(120px, 1fr))"
                    :gap "15px"
                    :margin-top "10px"
                    :text-align "left"}}

      ;; Abertura
      [:div
       [:div {:style {:font-size "12px"
                      :color "#aaa"
                      :text-transform "uppercase"
                      :margin-bottom "4px"}} "Abertura"]
       [:div {:style {:font-size "16px"
                      :font-weight "bold"}}
        (format-money abertura)]]

      ;; Máximo
      [:div
       [:div {:style {:font-size "12px"
                      :color "#aaa"
                      :text-transform "uppercase"
                      :margin-bottom "4px"}} "Máximo"]
       [:div {:style {:font-size "16px"
                      :font-weight "bold"}}
        (format-money maximo)]]

      ;; Mínimo
      [:div
       [:div {:style {:font-size "12px"
                      :color "#aaa"
                      :text-transform "uppercase"
                      :margin-bottom "4px"}} "Mínimo"]
       [:div {:style {:font-size "16px"
                      :font-weight "bold"}}
        (format-money minimo)]]

      ;; Fechamento
      [:div
       [:div {:style {:font-size "12px"
                      :color "#aaa"
                      :text-transform "uppercase"
                      :margin-bottom "4px"}} "Fechamento"]
       [:div {:style {:font-size "16px"
                      :font-weight "bold"}}
        (format-money fechamento)]]

      ;; Hora
      [:div
       [:div {:style {:font-size "12px"
                      :color "#aaa"
                      :text-transform "uppercase"
                      :margin-bottom "4px"}} "Hora"]
       [:div {:style {:font-size "16px"
                      :font-weight "bold"}}
        (or hora "-")]]]]))

(defn cotacao-content []
  (let [termo-local (r/atom "")]
    (fn []
      (let [{:keys [cotacao-busca carregando? erro]} @evt/app-state]
        [:div {:style {:display "flex"
                       :flex-direction "column"
                       :align-items "center"
                       :padding-top "20px"}}
         
         [:h1 {:style {:color "white" :font-weight "800"
                     :font-size "24px" :margin "28px" :margin-bottom "30px"}} 
          "Cotação de Ativos"]

         ;; Input Component
         [search-input termo-local]

         ;; Estados de Carregamento/Erro
         (when carregando?
           [:div {:style {:color "#007bff" :margin-top "20px" :font-weight "bold"}}
            "Buscando dados na B3..."])

         (when erro
           [:div {:style {:color "#ef4444" 
                          :margin-top "20px" 
                          :padding "15px"
                          :background-color "rgba(239, 68, 68, 0.1)"
                          :border-radius "4px"}}
            (str "❌" erro)])

         ;; Card de Resultado
         (when cotacao-busca
           [result-card cotacao-busca])]))))

(defn cotacao-page []
  [layout/main-layout "Cotação" [cotacao-content]])