(ns bolsa-front.core
  (:require [reagent.dom :as d]
            [bolsa-front.externals :as evt]))

;; --- Componente: Card de Ação ---
(defn card-acao [acao]
  ;; Se não vierem dados corretos, evita quebrar a tela
  (when acao
    [:div {:key (:simbolo acao)
           :class "bg-white p-6 rounded shadow border border-gray-200"}
     [:div.flex.justify-between
      [:h3.text-xl.font-bold (:simbolo acao)]
      ;; Verifica se variação existe antes de testar se é positiva
      (if (:variacao acao)
        [:span {:class (if (pos? (:variacao acao)) "text-green-600" "text-red-600")}
         (str (:variacao acao) "%")]
        [:span.text-gray-400 "-"])]
     [:p.text-2xl.mt-2 (str "R$ " (:preco acao))]]))

;; --- Componente: Página Principal ---
(defn home-page []
  (let [estado @evt/app-state]
    [:div.p-10.bg-gray-50.min-h-screen
     [:h1.text-3xl.font-bold.mb-6 "Bolsa de Valores"]
     
     ;; Botão de Teste
     [:button {:on-click evt/carregar-dados!
               :class "bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700 mb-6 transition"}
      (if (:carregando? estado) "Buscando..." "Atualizar Dados")]

     ;; Tratamento de Erro Visual
     (when (:erro estado)
       [:div.bg-red-100.text-red-700.p-4.mb-4.rounded.border.border-red-200 
        [:strong "Erro: "] (:erro estado)])

     ;; Lista de Cards
     (if (empty? (:acoes estado))
       [:p.text-gray-500 "Nenhuma ação para mostrar. Clique em Atualizar."]
       [:div.grid.grid-cols-1.md:grid-cols-3.gap-4
        (for [acao (:acoes estado)]
          [card-acao acao])])]))

;; --- Inicialização Segura ---
(defn mount-root []
  ;; Procura a div "app" que criamos no HTML
  (let [el (.getElementById js/document "app")]
    (if el
      (d/render [home-page] el)
      (js/console.error "ERRO FATAL: Não achei a div id='app'. Verifique o home.html!"))))

(defn ^:export init! []
  (mount-root))