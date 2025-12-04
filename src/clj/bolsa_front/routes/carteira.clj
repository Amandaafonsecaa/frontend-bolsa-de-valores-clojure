(ns bolsa-front.routes.carteira
  (:require [ring.util.http-response :as response]))

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
  (response/ok (:saldo carteira-data)))

(defn investido-handler [_]
  (response/ok (:total-investido carteira-data)))

(defn lucro-handler [_]
  (response/ok (:lucro-prejuizo carteira-data)))

(defn carteira-routes []
  ["/carteira"
   ;; rotas
   ["/extrato" {:get extrato-handler}]
   ["/saldo" {:get saldo-handler}]
   ["/investido" {:get investido-handler}]
   ["/lucro" {:get lucro-handler}]])
