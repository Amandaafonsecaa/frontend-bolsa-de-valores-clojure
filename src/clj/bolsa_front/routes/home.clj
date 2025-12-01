(ns bolsa-front.routes.home
  (:require
   [bolsa-front.layout :as layout]
   [clojure.java.io :as io]
   [bolsa-front.middleware :as middleware]
   [ring.util.http-response :as response]
   [ring.util.response :refer [resource-response content-type]])) 

(defn home-page [request]
  (-> (resource-response "html/home.html")
      (content-type "text/html")))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]

   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]])