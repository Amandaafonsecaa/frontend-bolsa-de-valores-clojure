(ns bolsa-front.layout
  (:require [reagent.core :as r]
            [bolsa-front.state :as state]))

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
