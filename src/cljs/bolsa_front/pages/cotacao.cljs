(ns bolsa-front.pages.cotacao
  (:require [reagent.core :as r]
            [bolsa-front.externals :as evt]
            [bolsa-front.layout :as layout]))

            
(defn cotacao-content []
    
    [:div {:style {:display "flex"
                    :justify-content "center"
                    :align-items "center"
                   :flex-direction "column"
                   :padding "29px"
                   :gap "20px"}}
        
        [:div
            [:h2 {:style {:margin "0" 
                        :color "white"
                        :font-weight "800"
                        :font-size "30px"}} "Cotação de Ações"]
        ]])  


(defn cotacao-page []
  [layout/main-layout "Dashboard" [cotacao-content]])