(ns bolsa-front.externals
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]))

(defonce app-state (r/atom {:acoes []         
                            :saldo-por-ativo {}         
                            :total-investido 0.0
                            :lucro-prejuizo 0.0
                            :patrimonio 0.0
                            :cotacao-atual nil
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


(defn saldo-por-ativo! [] 
   (swap! app-state assoc :carregando? true)

    (GET (str api-url "/carteira/saldo") 
       {:handler (fn [resposta]
                   (js/console.log "Saldo por ativo (Dashboard):" resposta)
                   (swap! app-state assoc :saldo-por-ativo (if (map? resposta) resposta {}) :carregando? false))
        :error-handler (fn [erro]
                         (js/console.error "Erro no saldo por ativo:" erro)
                         (swap! app-state assoc :erro "Erro no saldo por ativo" :carregando? false))
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

(defn gerar-data-iso []
  (let [data (js/Date.)
        ano (.getFullYear data)
        mes (-> (.getMonth data) inc)
        dia (.getDate data)
        horas (.getHours data)
        minutos (.getMinutes data)
        segundos (.getSeconds data)
        pad-zero (fn [n] (if (< n 10) (str "0" n) (str n)))]
    (str ano "-"
         (pad-zero mes) "-"
         (pad-zero dia) "T"
         (pad-zero horas) ":"
         (pad-zero minutos) ":"
         (pad-zero segundos))))

(defn comprar-acao! [ticker quantidade]
  (swap! app-state assoc :carregando? true :erro nil)
  
  (let [data-iso (gerar-data-iso)
        quantidade-num (js/parseInt quantidade)
        params {:ticker (str ticker)
                :quantidade quantidade-num
                :data data-iso}]
    (js/console.log "Enviando compra - Parâmetros:" params)
    (js/console.log "Data gerada:" data-iso)
    (POST (str api-url "/transacoes/compra")
          {:params params
           :format :json
           :response-format :json
           
           :handler (fn [resposta]
                      (js/console.log "Compra realizada:" resposta) 
                      (js/alert "Compra realizada com sucesso!")
                      (swap! app-state assoc :carregando? false)
                      (extrato!)
                      (saldo-por-ativo!)
                      (valor-investido)
                      (patrimonio!))
           
           :error-handler (fn [erro]
                            (js/console.error "Erro completo na Compra:" erro)
                            (let [response-data (-> erro :response)
                                  erro-msg (or (:erro response-data)
                                              (:detalhe response-data)
                                              (-> erro :status-text)
                                              "Erro desconhecido")]
                              (swap! app-state assoc :erro erro-msg :carregando? false)
                              (js/alert (str "Erro na compra: " erro-msg))))})))

(defn vender-acao! [ticker quantidade]
  (swap! app-state assoc :carregando? true :erro nil)
  
  (let [data-iso (gerar-data-iso)
        quantidade-num (js/parseInt quantidade)
        params {:ticker (str ticker)
                :quantidade quantidade-num
                :data data-iso}]
    (js/console.log "Enviando venda - Parâmetros:" params)
    (js/console.log "Data gerada:" data-iso)
    (POST (str api-url "/transacoes/venda")
          {:params params
           :format :json 
           :response-format :json
           
           :handler (fn [resposta]
                      (js/console.log "Venda realizada:" resposta)
                      (js/alert "Venda realizada com sucesso!")
                      (swap! app-state assoc :carregando? false)
                      (extrato!)
                      (saldo-por-ativo!)
                      (valor-investido)
                      (patrimonio!))
           
           :error-handler (fn [erro]
                            (js/console.error "Erro completo na Venda:" erro)
                            (let [response-data (-> erro :response)
                                  erro-msg (or (:erro response-data)
                                              (:detalhe response-data)
                                              (-> erro :status-text)
                                              "Erro desconhecido")]
                              (swap! app-state assoc :erro erro-msg :carregando? false)
                              (js/alert (str "Erro na venda: " erro-msg))))})))

(defn buscar-acao! [ticker]
  (swap! app-state assoc :carregando? true :erro nil :cotacao-busca nil)

  (GET (str api-url "/cotacao/" ticker)
    {:handler (fn [resposta]
                (js/console.log "🔍 Resultado da busca:" resposta)
                (swap! app-state assoc :cotacao-busca resposta :carregando? false))
     
     :error-handler (fn [erro]
                      (js/console.error "Erro na busca:" erro)
                      (let [response-data (:response erro)
                            msg (or (:erro response-data) 
                                    (:message response-data) 
                                    (str "Erro: " (:status-text erro)))]
                        (swap! app-state assoc :erro msg :carregando? false)))
     
     :response-format :json
     :keywords? true}))

(defn atualizar-tudo! []
  (extrato!)
  (saldo-por-ativo!)
  (valor-investido)
  (patrimonio!)
  )
