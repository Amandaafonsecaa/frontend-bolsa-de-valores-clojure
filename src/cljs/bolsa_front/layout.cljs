(ns bolsa-front.layout
  (:require [reagent.core :as r]))

(defn nav-item [label current-page]
  [:a {:href "#"
       :style {:color (if (= label current-page) "#007bff" "#ccc")
               :margin-right "25px"
               :padding-bottom "10px"
               :border-bottom (if (= label current-page) "2px solid #007bff" "none")
               :text-decoration "none"
               :font-weight "500"
               :cursor "pointer"}}
   label])

(defn nav-bar [current-page]
  [:nav {:style {:background-color "#1e1e1e"
                 :padding "20px 40px"
                 :border-bottom "1px solid #3a3a3a"
                 :display "flex"
                 :align-items "center"}}

   ;; Logo/Título
   [:div {:style {:flex-grow 1
                  :display "flex"
                  :align-items "center"}}
    [:h2 {:style {:font-size "24px" :color "white" :margin "0 10px 0 0"}} "Bolsa Front"]
    [:p {:style {:color "#ccc" :font-size "14px" :margin 0}} "Portfolio Manager"]]

   ;; Links de Navegação
   [:div {:style {:display "flex"}}
    (nav-item "Dashboard" current-page)
    (nav-item "Quote Lookup" current-page)
    (nav-item "Transactions" current-page)
    (nav-item "Wallet Extract" current-page)]])

;; Layout Principal que envolve qualquer página
(defn main-layout [current-page content]
  [:div {:style {:min-height "100vh"
                 :background-color "#1e1e1e"
                 :font-family "sans-serif"}}
   [nav-bar current-page] 
   [:div {:style {:max-width "1200px"
                  :margin "0 auto"
                  :padding "0 20px"}}
    content]])
