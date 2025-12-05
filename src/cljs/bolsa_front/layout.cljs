(ns bolsa-front.layout
  (:require [reagent.core :as r]
            [bolsa-front.state :as state]))


(defn help-button [titulo explicacao]
  (let [modal-aberto (r/atom false)]
    (fn []
      [:div {:style {:position "relative"
                     :display "inline-block"
                     :cursor "pointer"}}
       
       ;; Botão "?" que abre/fecha
       [:button
        {:on-click #(swap! modal-aberto not)
         :style {:background "none"
                 :border "none"
                 :color "#007bff"
                 :font-weight "bold"
                 :font-size "16px"
                 :margin-left "8px"
                 :padding "0"
                 :cursor "pointer"}}
        "ⓘ"]

       (when @modal-aberto
         [:div
          {:on-click #(reset! modal-aberto false)
           :style {:position "absolute"
                   :top "100%"
                   :left "50%"
                   :transform "translateX(-50%)"
                   :z-index "100"
                   :width "300px"
                   :background-color "#f0f8ff"
                   :border "1px solid #007bff"
                   :border-radius "8px"
                   :padding "15px"
                   :box-shadow "0 4px 12px rgba(0, 0, 0, 0.5)"
                   :color "#2e2e2e"
                   :text-align "left"
                   :font-size "14px"}}

          ;; Título da Explicação
          [:div {:style {:font-weight "bold"
                         :margin-top "5px"
                         :color "#007bff"}}
           titulo]

          [:p explicacao]

          ;; Botão de Fechar
          [:button
           {:on-click #(reset! modal-aberto false)
            :style {:position "absolute" :top "5px" :right "5px"
                    :background "none" :border "none" :font-weight "bold"
                    :cursor "pointer" :color "#555"}}
           "X"]
          ])])))

(defn nav-item [label page-key hash]
  (let [current @state/current-page]
    [:a {:href (str "#/" hash)
         :style {:color (if (= page-key current) "#007bff" "#ccc")
                 :margin-right "25px"
                 :padding-bottom "10px"
                 :border-bottom (if (= page-key current) "2px solid #007bff" "none")
                 :text-decoration "none"
                 :font-weight "500"
                 :cursor "pointer"}}
     label]))

(defn nav-bar []
  [:nav {:style {:background-color "#1e1e1e"
                 :padding "20px 40px"
                 :border-bottom "1px solid #3a3a3a"
                 :display "flex"
                 :align-items "center"}}

   [:div {:style {:flex-grow 1 :display "flex" :align-items "center"}}
    [:h2 {:style {:font-size "24px" :color "white" :margin "0 10px 0 0"}} "BolsaFácil"]]

   [:div {:style {:display "flex"}}
    [nav-item "Dashboard" :home "dashboard"]
    [nav-item "Transações" :buysell "transacoes"]
    [nav-item "Cotação" :cotacao "cotacao"]
    [nav-item "Carteira" :carteira "carteira"]]])

(defn main-layout [titulo content]
  [:div {:style {:min-height "100vh"
                 :background-color "#1e1e1e"
                 :font-family "sans-serif"}}
   [nav-bar] 
   [:div {:style {:max-width "1200px"
                  :margin "0 auto"
                  :padding "0 20px"}}
    content]])
