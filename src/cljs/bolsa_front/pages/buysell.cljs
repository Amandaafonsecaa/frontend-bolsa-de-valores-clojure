(ns bolsa-front.pages.buysell
  (:require [reagent.core :as r]
            [bolsa-front.layout :as layout]
            [bolsa-front.externals :as evt]
            [clojure.string]))

(defn obter-data-hoje []
  (let [data (js/Date.)
        ano (.getFullYear data)
        mes (-> (.getMonth data) inc)
        dia (.getDate data)
        pad-zero (fn [n] (if (< n 10) (str "0" n) (str n)))]
    (str ano "-" (pad-zero mes) "-" (pad-zero dia))))

(defn validar-data [data-str]
  (if (clojure.string/blank? data-str)
    {:valida? false :mensagem "Data é obrigatória"}
    (let [hoje-str (obter-data-hoje)
          hoje-parts (clojure.string/split hoje-str #"-")
          data-parts (clojure.string/split data-str #"-")
          hoje-ano (js/parseInt (nth hoje-parts 0))
          hoje-mes (js/parseInt (nth hoje-parts 1))
          hoje-dia (js/parseInt (nth hoje-parts 2))
          data-ano (js/parseInt (nth data-parts 0))
          data-mes (js/parseInt (nth data-parts 1))
          data-dia (js/parseInt (nth data-parts 2))
          hoje-date (js/Date. hoje-ano (dec hoje-mes) hoje-dia)
          data-date (js/Date. data-ano (dec data-mes) data-dia)
          diff-dias (/ (- (.getTime data-date) (.getTime hoje-date)) (* 1000 60 60 24))]
      (if (> diff-dias 0)
        {:valida? false :mensagem "A data não pode ser no futuro"}
        {:valida? true :mensagem nil}))))

(defn card [tipo]
     (let [state (r/atom {:ticker "" :quantidade "" :data (obter-data-hoje)})
        eh-compra? (= tipo "compra")
        titulo (if eh-compra? "Comprar Ação" "Vender Ação")
        cor-tema (if eh-compra? "#4CAF50" "#f44336")
        texto-botao (if eh-compra? "Confirmar Compra" "Confirmar Venda")
        erro-data (r/atom nil)]

    (fn [tipo] 
      [:div {:style {:background-color "#2e2e2e"
                     :padding "30px"
                     :border-radius "16px"
                     :border-top (str "4px solid " cor-tema)
                     :box-shadow "0 10px 25px -5px rgba(0, 0, 0, 0.5)"
                     :color "white"
                     :flex "1"
                     :min-width "300px"
                     :max-width "400px"
                     :display "flex"
                     :flex-direction "column"
                     :gap "20px"}}
       [:h2 {:style {:margin "0" 
                     :color cor-tema 
                     :font-weight "800"
                     :font-size "24px"}} 
        titulo]
       [:div
        [:label {:style {:display "block" 
                         :font-size "13px" 
                         :font-weight "600"
                         :color "#aaa"
                         :margin-bottom "8px"}} 
         "TICKER DO ATIVO"]
        [:input {:type "text"
                 :placeholder "Ex: PETR4"
                 :value (:ticker @state)
                 :on-change #(swap! state assoc :ticker (-> % .-target .-value .toUpperCase))
                 :style {:width "100%"
                         :padding "12px 16px"
                         :background-color "#181818"
                         :border "1px solid #444"
                         :border-radius "8px"
                         :color "white"
                         :font-size "16px"
                         :font-weight "bold"
                         :outline "none"
                         :text-transform "uppercase"}}]]
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
       [:div
        [:label {:style {:display "block" 
                         :font-size "13px" 
                         :font-weight "600"
                         :color "#aaa" 
                         :margin-bottom "8px"}} 
         "DATA"]
        [:input {:type "date"
                 :value (:data @state)
                 :max (obter-data-hoje)
                 :on-change (fn [e]
                             (let [nova-data (-> e .-target .-value)
                                   validacao (validar-data nova-data)]
                               (swap! state assoc :data nova-data)
                               (reset! erro-data (when (not (:valida? validacao))
                                                   (:mensagem validacao)))))
                 :style {:width "100%"
                         :padding "12px 16px"
                         :background-color "#181818"
                         :border (if @erro-data "1px solid #f44336" "1px solid #444")
                         :border-radius "8px"
                         :color "white"
                         :font-size "16px"
                         :font-weight "bold"
                         :outline "none"}}]
        (when @erro-data
          [:div {:style {:color "#f44336"
                        :font-size "12px"
                        :margin-top "5px"}}
           @erro-data])]
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
                             (let [dados @state
                                   ticker (:ticker dados)
                                   quantidade (:quantidade dados)
                                   data (:data dados)
                                   validacao-data (validar-data data)]
                               (if (not (:valida? validacao-data))
                                 (do
                                   (reset! erro-data (:mensagem validacao-data))
                                   (js/alert (:mensagem validacao-data)))
                                 (if (and ticker (not (clojure.string/blank? ticker)) 
                                          quantidade (not (clojure.string/blank? quantidade))
                                          data (not (clojure.string/blank? data)))
                                   (do 
                                     (reset! erro-data nil)
                                     (if eh-compra?
                                       (evt/comprar-acao! ticker quantidade data) 
                                       (evt/vender-acao! ticker quantidade data)) 
                                     (reset! state {:ticker "" :quantidade "" :data (obter-data-hoje)})) 
                                   (js/alert "Por favor, preencha todos os campos: Ticker, Quantidade e Data.")))))
                 :on-mouse-down #(-> % .-target .-style .-transform (set! "scale(0.98)"))
                 :on-mouse-up   #(-> % .-target .-style .-transform (set! "scale(1)"))}
        texto-botao]])))

(defn buysell-content []
  [:div {:style {:color "white" 
                 :padding "40px 20px" 
                 :text-align "center"
                 :min-height "100%"}}
   [:h1 {:style {:margin-bottom "10px"
                 :font-size "30px"
                 :font-weight "900"
                 :letter-spacing "-2px"
                 :background "white"
                 :-webkit-background-clip "text"
                 :-webkit-text-fill-color "transparent"}} 
    "Negociações"]
   [:p {:style {:color "#aaa" 
                :margin-bottom "50px" 
                :font-size "18px"}} 
    "Gerencie sua carteira comprando e vendendo ativos em tempo real."]
   [:div {:style {:display "flex" 
                  :flex-wrap "wrap" 
                  :gap "40px" 
                  :justify-content "center"
                  :align-items "flex-start"}}
    [card "compra"]
    [card "venda"]]])

(defn buysell-page []
  [layout/main-layout "Negociações" [buysell-content]])