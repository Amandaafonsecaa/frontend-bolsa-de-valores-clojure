(ns bolsa-front.core
  (:require [reagent.dom :as d]
            [bolsa-front.externals :as evt]        ;; O Cérebro (Lógica)
            [bolsa-front.pages.home :as home]))    ;; A Tela (Visual)


(defn mount-root []
  ;; O Porteiro só diz: "Renderize a Home Page dentro da div 'app'"
  ;; Ele pega 'home-page' de dentro do arquivo 'home' que importamos acima
  (let [el (.getElementById js/document "app")]
    (d/render [home/home-page] el)))

(defn ^:export init []
  (js/console.log "Iniciando sistema...")
  (evt/atualizar-tudo!) ;; Busca os dados do backend assim que abre
  (mount-root))         ;; Desenha a tela