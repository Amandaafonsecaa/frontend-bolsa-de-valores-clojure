(ns bolsa-front.core
  (:require [reagent.dom :as d]
            [bolsa-front.externals :as evt]        ;; O Cérebro (Estado Global)
            
            ;; IMPORTANTE: Seus imports de páginas
            [bolsa-front.pages.buysell :as buysell]    
            [bolsa-front.pages.dashboard :as dashboard]
            [bolsa-front.pages.carteira :as carteira]))

;; --- 1. TRADUTOR (Lê a URL e devolve a chave interna) ---
(defn get-page-from-hash []
  (let [hash (-> js/window .-location .-hash)]
    (case hash
      "#/carteira"   :carteira
      "#/dashboard"  :home
      "#/buysell"    :buysell   ;; URL direta
      "#/cotacao"    :buysell   ;; URL do botão (Redireciona para buysell)
      :home)))                  ;; Padrão

;; --- 2. O ROTEADOR (Escolhe o componente) ---
(defn current-page-component []
  (let [pagina (:pagina-atual @evt/app-state)]
    (case pagina
      :home     [dashboard/dashboard-page]
      :carteira [carteira/carteira-page]
      :buysell  [buysell/buysell-page] ;; <--- Chama a sua página nova!
      
      ;; Default
      [dashboard/dashboard-page])))

;; --- 3. OUVINTE (Monitora mudanças na URL) ---
(defn on-hash-change []
  (let [nova-pagina (get-page-from-hash)]
    (swap! evt/app-state assoc :pagina-atual nova-pagina)))

;; --- INICIALIZAÇÃO ---
(defn mount-root [] 
  (let [el (.getElementById js/document "app")]
    (d/render [current-page-component] el))) 

(defn ^:export init []
  (js/console.log "Iniciando sistema...")
  ;; Liga o ouvido para navegação
  (.addEventListener js/window "hashchange" on-hash-change)
  ;; Verifica onde estamos agora
  (on-hash-change)
  ;; Carrega dados
  (evt/atualizar-tudo!)
  ;; Desenha
  (mount-root))