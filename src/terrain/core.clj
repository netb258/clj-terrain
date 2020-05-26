(ns terrain.core
  (:require [quil.core :as q]
            [quil.middleware :as m])
  (:gen-class))

(def STATE
  {:width 800
   :height 800
   :scale 20
   :position 0.0})

(defn setup []
  (q/frame-rate 30)
  ;; Here we return the initial state that the other function(update-state, draw-state!, etc...) will work with.
  STATE)

(defn set-colors! []
  ;; The background is black.
  (q/background 0 0 0)
  ;; We're drawing with white lines.
  (q/stroke 255 255 255)
  (q/no-fill))

;; NOTE: We need to rotate the whole field or it will look like a flat 2d board (like a chess board).
(defn rotate-field! [state]
  (q/translate (/ (:width state) 2) (/ (+ (:height state) 110) 2))
  (q/rotate-x (/ (Math/PI) 3))
  (q/translate (/ (- (:width state)) 2) (/ (- (:height state)) 2)))

(defn draw-state! [state]
  (set-colors!)
  (rotate-field! state)
  (let [scale (:scale state)
        cols (/ (:width state) scale)
        rows (/ (:height state) scale)
        z-values (mapv #(into [] %) ;; The z axis values should be in a vector of vectors.
                   (partition rows
                              (for [y (range rows)
                                    x (range cols)]
                                ;; NOTE: The noise function will return random floats that are close to each other.
                                (q/map-range (q/noise (- (* x 0.1) (:position state)) (* y 0.1)) 0 1 -120 120))))]
    ;; Draw a mesh of triangles that will be the basis of our terrain.
    (dotimes [y (dec rows)]
      (q/begin-shape :triangle-strip)
      (dotimes [x cols]
        (q/vertex (* x scale) (* y scale) (get-in z-values [x y]))
        (q/vertex (* x scale) (* (inc y) scale) (get-in z-values [x (inc y)])))
      (q/end-shape))))

(defn update-state [state]
  ;; Move foreward every frame.
  (update-in state [:position] #(+ % 0.1)))

(defn -main []
  (q/defsketch terrain
    :title "Terrain"
    :renderer :opengl
    :features [:exit-on-close]
    :setup setup
    :draw draw-state!
    :update update-state
    :size [(:width STATE) (:height STATE)]
    :middleware [m/fun-mode]))
