;; gorilla-repl.fileformat = 1

;; **
;;; # AISTATS Examples
;;; 
;;; This is the HMM benchmark from the 2014 AISTATS paper, with 16 observations, 3 states and known parameters.
;; **

;; @@
(ns aistats-examples
  (:require [gorilla-plot.core :as plot]
            [clojure.core.matrix :as m])
  (:use clojure.repl
        [anglican core runtime emit [state :only [get-predicts get-log-weight]]]))

(defn expected-value
  "applies f to each sample and computes weighted expectation"
  [f samples]
  (let [vs (map f samples)
        lws (map get-log-weight samples)
        vlws (map vector vs lws)
        max-lw (reduce max lws)]
    (loop [vlws vlws
           sum-wv 0.0
           sum-w 0.0]
      (if-let [[v lw] (first vlws)]
        (let [w (exp (- lw max-lw))]
          (recur (rest vlws)
                 (m/add sum-wv (m/mul w v))
                 (m/add sum-w w)))
        (m/div sum-wv sum-w)))))

(defn index->ind 
  "converts a colleciton of indices to a matrix of indicator vectors"
  [values]
  (let [max-v (reduce max values)
    	zero-vec (into [] (repeat (inc max-v) 0))]
    (m/matrix (map #(assoc zero-vec % 1) values))))

(defn square 
  [x]
  (m/mul x x))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/square</span>","value":"#'aistats-examples/square"}
;; <=

;; **
;;; Define model
;; **

;; @@
(defquery hmm
  [observations init-dist trans-dists obs-dists]
  (predict
    :states
    (reduce 
      (fn [states obs]
        (let [state (sample (get trans-dists
                                 (peek states)))]
          (observe (get obs-dists state) obs)
          (conj states state)))
      [(sample init-dist)]
      observations)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/hmm</span>","value":"#'aistats-examples/hmm"}
;; <=

;; **
;;; Define data and parameters
;; **

;; @@
(def data 
  "observation sequence (16 points)"
  [0.9 0.8 0.7 0.0 
   -0.025 -5.0 -2.0 -0.1 
   0.0 0.13 0.45 6 
   0.2 0.3 -1 -1])

(def init-dist
  "distribution on 0-th state in sequence 
  (before first observation)"
  (discrete [1.0 1.0 1.0]))

(def trans-dists
  "transition distribution for each state"
  {0 (discrete [0.1 0.5 0.4])
   1 (discrete [0.2 0.2 0.6])
   2 (discrete [0.15 0.15 0.7])})

(def obs-dists
  "observation distribution for each state"
  {0 (normal -1 1)
   1 (normal 1 1)
   2 (normal 0 1)})


(def posterior 
  "ground truth postior on states, 
  calculated using forward-backwardi'm "
  [[ 0.3775 0.3092 0.3133]
   [ 0.0416 0.4045 0.5539]
   [ 0.0541 0.2552 0.6907]
   [ 0.0455 0.2301 0.7244]
   [ 0.1062 0.1217 0.7721]
   [ 0.0714 0.1732 0.7554]
   [ 0.9300 0.0001 0.0699]
   [ 0.4577 0.0452 0.4971]
   [ 0.0926 0.2169 0.6905]
   [ 0.1014 0.1359 0.7626]
   [ 0.0985 0.1575 0.744 ]
   [ 0.1781 0.2198 0.6022]
   [ 0.0000 0.9848 0.0152]
   [ 0.1130 0.1674 0.7195]
   [ 0.0557 0.1848 0.7595]
   [ 0.2017 0.0472 0.7511]
   [ 0.2545 0.0611 0.6844]])
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/posterior</span>","value":"#'aistats-examples/posterior"}
;; <=

;; **
;;; Run inference
;; **

;; @@
(def number-of-particles 1000)
(def number-of-samples 100000)

(def samples
  (->> (doquery :smc hmm
                [data 
                 init-dist
                 trans-dists
                 obs-dists]
                :number-of-particles number-of-particles)
       (take number-of-samples)
       doall
       time))
;; @@
;; ->
;;; &quot;Elapsed time: 20617.298 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/samples</span>","value":"#'aistats-examples/samples"}
;; <=

;; **
;;; Calculate L2 error relative to true posterior as a function of number of samples
;; **

;; @@
(def num-sample-range (mapv (partial * number-of-samples)
                            [1e-2 2e-2 5e-2 1e-1 2e-1 5e-1 1]))  
(def L2-errors
   (map (fn [n]
          (->> ;; use first n samples
               (take n samples)
               ;; calculate empirical posterior
               (expected-value (comp index->ind 
                                     :states 
                                     get-predicts))
               ;; calculate L2 error relative to true posterior
               (m/sub posterior)
               square
               (reduce m/add)
               (reduce m/add)))
               ;;(reduce m/add)))
        num-sample-range))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/L2-errors</span>","value":"#'aistats-examples/L2-errors"}
;; <=

;; @@
(plot/list-plot (map vector 
                     (map #(/ (log %) (log 10)) num-sample-range)
                     (map #(/ (log %) (log 10)) L2-errors))
                :joined true
                :color "#05A"
                :x-title "log number of samples"
                :y-title "log L2 error")
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"1618e811-a89f-4316-8e3d-bc9e7d1d7448","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"1618e811-a89f-4316-8e3d-bc9e7d1d7448","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"1618e811-a89f-4316-8e3d-bc9e7d1d7448"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"stroke":{"value":"#05A"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"1618e811-a89f-4316-8e3d-bc9e7d1d7448","values":[{"x":2.9999999999999996,"y":-1.295938840288193},{"x":3.301029995663981,"y":-1.6832808445666025},{"x":3.6989700043360187,"y":-2.09148474979796},{"x":4.0,"y":-2.5261875330880317},{"x":4.30102999566398,"y":-2.541125446126106},{"x":4.698970004336019,"y":-3.0552105323874708},{"x":5.0,"y":-3.2454206731444883}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"1618e811-a89f-4316-8e3d-bc9e7d1d7448\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"1618e811-a89f-4316-8e3d-bc9e7d1d7448\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"1618e811-a89f-4316-8e3d-bc9e7d1d7448\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :stroke {:value \"#05A\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"1618e811-a89f-4316-8e3d-bc9e7d1d7448\", :values ({:x 2.9999999999999996, :y -1.295938840288193} {:x 3.301029995663981, :y -1.6832808445666025} {:x 3.6989700043360187, :y -2.09148474979796} {:x 4.0, :y -2.5261875330880317} {:x 4.30102999566398, :y -2.541125446126106} {:x 4.698970004336019, :y -3.0552105323874708} {:x 5.0, :y -3.2454206731444883})}], :width 400, :height 247.2188, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=
