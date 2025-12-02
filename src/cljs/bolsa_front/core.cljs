(ns bolsa-front.core
  (:require [reagent.dom :as d]
            [bolsa-front.externals :as evt]        
            [bolsa-front.pages.dashboard :as dashboard])) 


(defn mount-root [] 
  (let [el (.getElementById js/document "app")]
    (d/render [dashboard/dashboard-page] el))) 

(defn ^:export init []
  (js/console.log "Iniciando sistema...")
  (evt/atualizar-tudo!) 
  (mount-root))        
