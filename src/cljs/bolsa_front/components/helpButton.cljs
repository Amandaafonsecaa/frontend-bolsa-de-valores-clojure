(ns bolsa-front.components.helpButton
  (:require [reagent.core :as r]))

  (defn helpButton [titulo explicacao]
    (let [modal-aberto (r/atom false)]
        (fn []
            [:div {:style {:position "relative"
                            :display "inline-block"
                            :cursor "pointer"
                            }

                    }
                [:button 
                    {:on-click #(swap! modal-aberto not)
                    :style {:background "none"
                            :border "none"
                            :color "#007bff"
                            :font-weight "bold"
                            :font-size "16px"
                            :margin-left "8px"
                            :padding "0"
                            :cursor "pointer"
                             }       
                    }
                    "ⓘ"
                ]

                (when @modal-aberto
                    [:div
                        {:on-click #(reset! modal-aberto false)
                        :style {
                            :position "absolute"
                            :top "100%"
                            :left "50%"
                            :transform "translateX(-50%)"
                            :z-index "100"
                            :width "300px"
                            :background-color "#f0f8ff" 
                            :border "1px solid #007bff"
                            :border-radius "8px"
                            :padding "15px"
                            :box-shadow "0 4px 12px rgba(0, 0, 0, 0.5)"
                            :color "#2e2e2e"
                            :text-align "left"
                            :font-size "14px"
                        }
                        }

                        [:div (:style {:font-weight "bold"
                                        :margin-top "5px"
                                        :color "#007bff"
                                        }
                                )
                                titulo
                        ]

                        [:p explicacao]
                        
                        [:button
                        {:on-click #(reset! modal-aberto false)
                            :style {:position "absolute" :top "5px" :right "5px"
                                    :background "none" :border "none" :font-weight "bold"
                                    :cursor "pointer" :color "#555"}}
                        "X"]
                    ]
                )
            ])
    )
  )