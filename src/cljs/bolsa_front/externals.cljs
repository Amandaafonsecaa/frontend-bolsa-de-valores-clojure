(ns bolsa-front.externals
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))

(defonce app-state (r/atom {:acoes []         
                            :saldo 0.0         
                            :total-investido 0.0
                            :lucro-prejuizo 0.0
                            :carregando? false
                            :erro nil}))

(def api-url "http://localhost:3000")

;; carteira
(defn extrato! []
  (swap! app-state assoc :carregando? true :erro nil)

  (GET (str api-url "/carteira/extrato")
    {:handler (fn[resposta]
                (js/console.log "Extrato:" resposta)
                (swap! app-state assoc :acoes resposta :carregando? false))
                :error-handler (fn [erro]
                         (swap! app-state assoc :erro "Erro no extrato" :carregando? false))
                :response-format :json
                :keywords? true}
  )

)

(defn ver-saldo [] 
   (swap! app-state assoc :carregando? true)

    (GET (str api-url "/carteira/saldo") 
       {:handler (fn [resposta]
                   (js/console.log "Saldo:" resposta) 
                   (swap! app-state assoc :saldo (js/parseFloat resposta) :carregando? false))
        :error-handler (fn [erro]
                         (swap! app-state assoc :erro "Erro no saldo" :carregando? false))
        :response-format :json
        :keywords? true})
)

(defn valor-investido[]
    (swap! app-state assoc :carregando? true)

    (GET (str api-url "/carteira/investido")
        {:handler (fn[resposta]
                    (js/console.log "Valor investido:" resposta) 
                    (swap! app-state assoc :total-investido (js/parseFloat resposta) :carregando? false))
        :error-handler (fn [erro]
                         (swap! app-state assoc :erro "Erro no Valor investido" :carregando? false))
        :response-format :json
        :keywords? true
        }
    )
)

(defn lucro[]
    (swap! app-state assoc :carregando? true)

    (GET (str api-url "/carteira/lucro")
        {:handler (fn[resposta]
                    (js/console.log "Lucro:" resposta) 
                    (swap! app-state assoc :lucro-prejuizo (js/parseFloat resposta) :carregando? false))
        :error-handler (fn [erro]
                         (swap! app-state assoc :erro "Erro no lucro" :carregando? false))
        :response-format :json
        :keywords? true
        }
    )
)

;; Transacoes 

(defn comprar-acao! [ticker quantidade]
  (swap! app-state assoc :carregando? true)
  
  (POST (str api-url "/transacoes/compra")
        {:params {:ticker ticker 
                  :quantidade (js/parseInt quantidade)}
         :format :json
         :response-format :json
         
         :handler (fn [resposta]
                    (js/console.log "Compra realizada:" resposta) 
                    (js/alert "Compra realizada com sucesso!")
                    (extrato!))
         
         :error-handler (fn [erro]
                          (js/console.error "Erro na Compra:" erro)
                          (swap! app-state assoc :erro "Falha na compra." :carregando? false))}))

(defn vender-acao! [ticker quantidade]
  (swap! app-state assoc :carregando? true)
  
  (POST (str api-url "/transacoes/venda")
        {:params {:ticker ticker 
                  :quantidade (js/parseInt quantidade)}
         :format :json 
         :response-format :json
         
         :handler (fn [resposta]
                    (js/console.log "Venda realizada:" resposta)
                    (js/alert "Venda realizada com sucesso!")
                    (extrato!))
         
         :error-handler (fn [erro]
                          (js/console.error "Erro na Venda:" erro)
                          (swap! app-state assoc :erro "Falha na Venda." :carregando? false))}))
(defn atualizar-tudo! []
  (extrato!)
  (ver-saldo)
  (valor-investido)
  (lucro))
