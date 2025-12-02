(ns bolsa-front.routes.carteira
  (:require [ring.util.http-response :as response]))

;; Dados simulados da carteira para teste
(def carteira-data
  {:saldo 25450.75
   :total-investido 100000.00
   :lucro-prejuizo 25450.75
   :extrato [{:ticker "PETR4" :quantidade 500}
             {:ticker "VALE3" :quantidade 300}
             {:ticker "ITUB4" :quantidade 800}
             {:ticker "BBDC4" :quantidade 1200}
             {:ticker "MGLU3" :quantidade 2000}]})

(defn extrato-handler [_]
  (response/ok (:extrato carteira-data)))

(defn saldo-handler [_]
  ;; O AJAX no frontend espera um valor simples (número), não um mapa
  (response/ok (:saldo carteira-data)))

(defn investido-handler [_]
  ;; O AJAX no frontend espera um valor simples (número)
  (response/ok (:total-investido carteira-data)))

(defn lucro-handler [_]
  ;; O AJAX no frontend espera um valor simples (número)
  (response/ok (:lucro-prejuizo carteira-data)))

(defn carteira-routes []
  ["/carteira"
   ;; Rotas da carteira (API)
   ["/extrato" {:get extrato-handler}]
   ["/saldo" {:get saldo-handler}]
   ["/investido" {:get investido-handler}]
   ["/lucro" {:get lucro-handler}]])
