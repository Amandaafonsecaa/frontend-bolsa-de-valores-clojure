(ns bolsa-front.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [bolsa-front.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[bolsa-front started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[bolsa-front has shut down successfully]=-"))
   :middleware wrap-dev})
