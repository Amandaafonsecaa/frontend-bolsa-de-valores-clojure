(ns bolsa-front.pages.buysell
  (:require [reagent.core :as r]
            [bolsa-front.layout :as layout]
            [bolsa-front.externals :as evt]))

;; --- COMPONENTE: Card de Negociação ---
(defn card [tipo]
  (let [state (r/atom {:ticker "" :quantidade ""})
        eh-compra? (= tipo "compra")
        titulo (if eh-compra? "Comprar Ação" "Vender Ação")
        
        ;; Cores Fixas (Sem depender de variáveis CSS para não bugar)
        cor-tema (if eh-compra? "#4CAF50" "#f44336") ;; Verde ou Vermelho
        
        texto-botao (if eh-compra? "Confirmar Compra" "Confirmar Venda")]

    (fn [tipo] 
      [:div {:style {:background-color "#2e2e2e" ;; Fundo Cinza Escuro (Igual Dashboard)
                     :padding "30px"
                     :border-radius "16px"
                     :border-top (str "4px solid " cor-tema)
                     :box-shadow "0 10px 25px -5px rgba(0, 0, 0, 0.5)" ;; Sombra bonita
                     :color "white"
                     :flex "1"
                     :min-width "300px"
                     :max-width "400px"
                     :display "flex"
                     :flex-direction "column"
                     :gap "20px"}}
       
       ;; Título do Card
       [:h2 {:style {:margin "0" 
                     :color cor-tema 
                     :font-weight "800"
                     :font-size "24px"}} 
        titulo]

       ;; --- Input: Ticker ---
       [:div
        [:label {:style {:display "block" 
                         :font-size "13px" 
                         :font-weight "600"
                         :color "#aaa"  ;; Cinza claro
                         :margin-bottom "8px"}} 
         "TICKER DO ATIVO"]
        
        [:input {:type "text"
                 :placeholder "Ex: PETR4"
                 :value (:ticker @state)
                 :on-change #(swap! state assoc :ticker (-> % .-target .-value .toUpperCase))
                 :style {:width "100%"
                         :padding "12px 16px"
                         :background-color "#181818" ;; Fundo do input bem escuro
                         :border "1px solid #444"    ;; Borda sutil
                         :border-radius "8px"
                         :color "white"              ;; Texto branco ao digitar
                         :font-size "16px"
                         :font-weight "bold"
                         :outline "none"
                         :text-transform "uppercase"}}]]

       ;; --- Input: Quantidade ---
       [:div
        [:label {:style {:display "block" 
                         :font-size "13px" 
                         :font-weight "600"
                         :color "#aaa" 
                         :margin-bottom "8px"}} 
         "QUANTIDADE"]
        
        [:input {:type "number"
                 :placeholder "Ex: 100"
                 :value (:quantidade @state)
                 :on-change #(swap! state assoc :quantidade (-> % .-target .-value))
                 :style {:width "100%"
                         :padding "12px 16px"
                         :background-color "#181818"
                         :border "1px solid #444"
                         :border-radius "8px"
                         :color "white"
                         :font-size "16px"
                         :font-weight "bold"
                         :outline "none"}}]]

       ;; --- Botão de Ação ---
       [:button {:style {:margin-top "10px"
                         :background-color cor-tema
                         :color "white"
                         :padding "14px"
                         :border "none"
                         :border-radius "8px"
                         :font-size "16px"
                         :font-weight "800"
                         :letter-spacing "0.5px"
                         :cursor "pointer"
                         :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.3)"
                         :transition "transform 0.1s"}
                 :on-click (fn []
                             (let [dados @state]
                               (if eh-compra?
                                 (evt/comprar-acao! (:ticker dados) (:quantidade dados))
                                 (evt/vender-acao! (:ticker dados) (:quantidade dados)))
                               (reset! state {:ticker "" :quantidade ""})))
                 :on-mouse-down #(-> % .-target .-style .-transform (set! "scale(0.98)"))
                 :on-mouse-up   #(-> % .-target .-style .-transform (set! "scale(1)"))}
        texto-botao]])))

;; --- Conteúdo da Página ---
(defn buysell-content []
  [:div {:style {:color "white" 
                 :padding "40px 20px" 
                 :text-align "center"
                 ;; TIREI O BACKGROUND COLOR AQUI PARA PEGAR O DO LAYOUT
                 :min-height "100%"}}
   
   ;; TÍTULO COM GRADIENTE
   [:h1 {:style {:margin-bottom "10px"
                 :font-size "30px"
                 :font-weight "900"
                 :letter-spacing "-2px"
                 ;; Efeito gradiente no texto (Azul -> Verde)
                 :background "white"
                 :-webkit-background-clip "text"
                 :-webkit-text-fill-color "transparent"}} 
    "Negociações"]
   
   [:p {:style {:color "#aaa" 
                :margin-bottom "50px" 
                :font-size "18px"}} 
    "Gerencie sua carteira comprando e vendendo ativos em tempo real."]

   ;; Container Flex
   [:div {:style {:display "flex" 
                  :flex-wrap "wrap" 
                  :gap "40px" 
                  :justify-content "center"
                  :align-items "flex-start"}}
    
    ;; Card de Compra
    [card "compra"]
    
    ;; Card de Venda
    [card "venda"]]])

(defn buysell-page []
  [layout/main-layout "Cotação" [buysell-content]])