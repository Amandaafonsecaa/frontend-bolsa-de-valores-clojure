(ns bolsa-front.core
  (:require [reagent.core :as r]
            [reagent.dom :as d]
            [bolsa-front.externals :as evt]
            [bolsa-front.state :as state]        
            [bolsa-front.pages.dashboard :as dashboard]
            [bolsa-front.pages.carteira :as carteira]))

(defn get-page-from-hash []
  (let [hash (-> js/window .-location .-hash)]
    (cond
      (or (= hash "") (= hash "#")) "Dashboard"
      (= hash "#carteira") "Carteira"
      (= hash "#dashboard") "Dashboard"
      :else "Dashboard")))


(defn current-page-component []
  (case @state/current-page
    "Carteira" [carteira/carteira-page]
    "Dashboard" [dashboard/dashboard-page]
    [dashboard/dashboard-page]))

(defn on-hash-change []
  (reset! state/current-page (get-page-from-hash)))

(defn mount-root [] 
  (let [el (.getElementById js/document "app")]
    (d/render [current-page-component] el))) 

(defn ^:export init []
  (js/console.log "Iniciando sistema...")
  (.addEventListener js/window "hashchange" on-hash-change)
  (reset! state/current-page (get-page-from-hash))
  (when (= @state/current-page "Dashboard")
    (evt/atualizar-tudo!))
  (mount-root))        
