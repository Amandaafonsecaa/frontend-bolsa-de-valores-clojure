(ns bolsa-front.externals
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))

(defonce app-state (r/atom {:acoes []         
                            :saldo 0.0         
                            :total-investido 0.0
                            :lucro-prejuizo 0.0
                            :patrimonio 0.0
                            :carregando? false
                            :erro nil}))

(def api-url "http://localhost:3000")

(defn extrair-numero [nome-campo resposta chaves-tentativa]
  (js/console.log (str "🔍 DEBUG " nome-campo ":") resposta)
  
  (cond
    (number? resposta) resposta
    
    (string? resposta) (js/parseFloat resposta)
    
    (map? resposta) (let [valor-pela-chave (some #(get resposta %) chaves-tentativa)
                          valor-bruto (-> resposta vals first)]
                      
                      (cond
                        (number? valor-pela-chave) valor-pela-chave
                        (number? valor-bruto) valor-bruto
                        :else 0.0))
    
    (vector? resposta) (let [primeiro (first resposta)]
                         (if (map? primeiro)
                           (or (some #(get primeiro %) chaves-tentativa) 0.0)
                           0.0))
                           
    :else 0.0))

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

;; busca o extrato filtrado por data
(defn extrato-filtrado! [params success-callback error-callback]
  (let [query-string (->> params
                         (filter (fn [[_ v]] (some? v)))
                         (map (fn [[k v]] (str (name k) "=" v)))
                         (clojure.string/join "&"))
        url (if (empty? query-string)
              (str api-url "/carteira/extrato")
              (str api-url "/carteira/extrato?" query-string))]
    (GET url
      {:handler (fn [resposta]
                  (js/console.log "Extrato filtrado:" resposta)
                  (success-callback resposta))
       :error-handler (fn [erro]
                        (js/console.error "Erro ao buscar extrato filtrado:" erro)
                        (if error-callback
                          (error-callback erro)
                          (swap! app-state assoc :erro "Erro ao buscar extrato filtrado" :carregando? false)))
       :response-format :json
       :keywords? true})))


(defn patrimonio! []
  (GET (str api-url "/carteira/patrimonio")
    {:handler (fn[resposta]
                (let [valor (extrair-numero "PATRIMONIO" resposta [:patrimonio_liquido :patrimonio :valor])]
                  (swap! app-state assoc :patrimonio valor :carregando? false)))
     :error-handler (fn [erro] (swap! app-state assoc :erro "Erro patrimonio" :carregando? false))
     :response-format :json :keywords? true}))


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
                    (let [valor (extrair-numero "VALOR_INVESTIDO" resposta [:valor_total_investido :valor :total])]
                      (swap! app-state assoc :total-investido valor :carregando? false)))
        :error-handler (fn [erro]
                         (swap! app-state assoc :erro "Erro no Valor investido" :carregando? false))
        :response-format :json
        :keywords? true
        }
    )
)

#_ (defn lucro[]
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

;; transacoes 

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
  (valor-investido)
  (patrimonio!)
  )
