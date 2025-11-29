(ns bolsa-front.externals
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))

(defonce app-state (r/atom {:acoes []          ;; Lista do extrato
                            :saldo 0.0         ;; Saldo em caixa
                            :total-investido 0.0
                            :lucro-prejuizo 0.0
                            :carregando? false
                            :erro nil}))

(def api-url "http://localhost:3000")

;; Carteira 
(defn carregar-dados! []
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

  ;; MUDANÇA 1: Usando (str api-url ...) em vez de digitar tudo
)

(defn ver-saldo [] 
   (swap! app-state assoc :carregando? true)

    (GET (str api-url "/carteira/saldo") 
       {:handler (fn [resposta]
                   (js/console.log "Saldo:" resposta)
                   (swap! app-state assoc :saldo resposta :carregando? false))
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
                    (swap! app-state assoc :total-investido resposta :carregando? false))
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
                    (swap! app-state assoc :lucro-prejuizo resposta :carregando? false))
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
                  ;; MUDANÇA 2: Convertendo string "10" para número 10
                  :quantidade (js/parseInt quantidade)}
         :format :json
         :response-format :json
         
         :handler (fn [resposta]
                    (js/console.log "Compra realizada:" resposta)
                    ;; MUDANÇA 3: Feedback visual pro usuário
                    (js/alert "Compra realizada com sucesso!")
                    (carregar-dados!))
         
         :error-handler (fn [erro]
                          (js/console.error "Erro na Compra:" erro)
                          (swap! app-state assoc :erro "Falha na compra." :carregando? false))}))

(defn vender-acao! [ticker quantidade]
  (swap! app-state assoc :carregando? true)
  
  (POST (str api-url "/transacoes/venda")
        {:params {:ticker ticker
                  ;; MUDANÇA 2: Convertendo string para número aqui também
                  :quantidade (js/parseInt quantidade)}
         :format :json 
         :response-format :json
         
         :handler (fn [resposta]
                    (js/console.log "Venda realizada:" resposta)
                    (js/alert "Venda realizada com sucesso!")
                    (carregar-dados!))
         
         :error-handler (fn [erro]
                          (js/console.error "Erro na Venda:" erro)
                          (swap! app-state assoc :erro "Falha na Venda." :carregando? false))}))