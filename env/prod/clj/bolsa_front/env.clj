(ns bolsa-front.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[bolsa-front started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[bolsa-front has shut down successfully]=-"))
   :middleware identity})
