(ns bolsa-front.core
  (:require [reagent.core :as r]
            [reagent.dom :as d]
            [bolsa-front.externals :as evt]
            [bolsa-front.state :as state]        
            [bolsa-front.pages.dashboard :as dashboard]
            [bolsa-front.pages.carteira :as carteira]
            [bolsa-front.pages.buysell :as buysell]))

(defn get-page-from-hash []
  (let [hash (-> js/window .-location .-hash)]
    (case hash
      "#/carteira" :carteira
      "#carteira" :carteira
      "#/dashboard" :home
      "#dashboard" :home
      "#/transacoes" :buysell
      "#/cotacao" :cotacao
      "" :home
      "#" :home
      :home)))

(defn current-page-component []
  (let [current @state/current-page]
    (case current
      :carteira [carteira/carteira-page]
      :home [dashboard/dashboard-page]
      :buysell [buysell/buysell-page]
      :cotacao [dashboard/dashboard-page]
      [dashboard/dashboard-page])))

(defn on-hash-change []
  (reset! state/current-page (get-page-from-hash)))

(defn mount-root [] 
  (let [el (.getElementById js/document "app")]
    (d/render [current-page-component] el))) 

(defn ^:export init []
  (js/console.log "Iniciando sistema...")
  (.addEventListener js/window "hashchange" on-hash-change)
  (reset! state/current-page (get-page-from-hash))
  (when (= @state/current-page :home)
    (evt/atualizar-tudo!))
  (mount-root))        
