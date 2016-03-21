;; gorilla-repl.fileformat = 1

;; **
;;; # Gaussian with Unknown Mean (AISTATS)
;;; 
;;; This is the Gaussian with unknown mean  benchmark from the 2014 AISTATS paper, with analytical posterior.
;; **

;; @@
(ns aistats-examples
  (:require [gorilla-plot.core :as plot]
            [clojure.core.matrix :as m]
            [anglican.stat :as s])
  (:use clojure.repl
        [anglican 
          core runtime emit 
          [state :only [get-predicts get-log-weight]]
          [inference :only [collect-by]]]))

(defn- square [x] (m/mul x x))

(defn kl-normal 
  "calculates the kl divergence beween two 
  normal distributions from parameters"
  [p-mean p-sigma q-mean q-sigma]
  (+ (- (log q-sigma)
     	(log p-sigma))
     (/ (+ (square p-sigma)
           (square (- p-mean
                      q-mean)))
        (* 2 (square q-sigma)))
     (/ -1 2)))           
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/kl-normal</span>","value":"#'aistats-examples/kl-normal"}
;; <=

;; **
;;; ## Define model
;; **

;; @@
(defquery gaussian
    "Returns the posterior distribution on the mean of a Gaussian, 
    conditioned on observations"
    [observations sigma mu0 sigma0]
    (let [mu (sample (normal mu0 sigma0))
          likelihood (normal mu sigma)]
      (reduce (fn [_ obs]
                (observe likelihood obs))
              nil
              observations)
      (predict :mu mu)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/gaussian</span>","value":"#'aistats-examples/gaussian"}
;; <=

;; **
;;; ## True Posterior
;; **

;; @@
(def posterior (normal 7.25 (sqrt (/ 1.0 1.2))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/posterior</span>","value":"#'aistats-examples/posterior"}
;; <=

;; **
;;; ## Run inference
;; **

;; @@
(def number-of-samples 1000000)

(def samples
  (->> (doquery :lmh
                gaussian 
                [[9.0 8.0] (sqrt 2.0) 1.0 (sqrt 5.0)])
       (take number-of-samples)
       doall
       time))
;; @@
;; ->
;;; &quot;Elapsed time: 52659.563 msecs&quot;
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/samples</span>","value":"#'aistats-examples/samples"}
;; <=

;; **
;;; ## Plot Empirical Distribution vs True Posterior
;; **

;; @@
(def mu-values 
  (map (comp :mu get-predicts) samples))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/mu-values</span>","value":"#'aistats-examples/mu-values"}
;; <=

;; @@
(plot/histogram mu-values)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"507140b7-6954-4ac2-b284-e8f27e05daa1","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"507140b7-6954-4ac2-b284-e8f27e05daa1","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"507140b7-6954-4ac2-b284-e8f27e05daa1"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"507140b7-6954-4ac2-b284-e8f27e05daa1","values":[{"x":2.375642527360281,"y":0},{"x":2.791840087412413,"y":4.0},{"x":3.208037647464545,"y":6.0},{"x":3.6242352075166773,"y":66.0},{"x":4.040432767568809,"y":196.0},{"x":4.456630327620942,"y":953.0},{"x":4.872827887673074,"y":3430.0},{"x":5.289025447725206,"y":10471.0},{"x":5.705223007777338,"y":32026.0},{"x":6.12142056782947,"y":61742.0},{"x":6.537618127881602,"y":112043.0},{"x":6.9538156879337345,"y":149848.0},{"x":7.370013247985867,"y":189959.0},{"x":7.786210808037999,"y":174603.0},{"x":8.202408368090131,"y":124498.0},{"x":8.618605928142262,"y":80758.0},{"x":9.034803488194393,"y":40208.0},{"x":9.451001048246525,"y":11616.0},{"x":9.867198608298656,"y":5547.0},{"x":10.283396168350787,"y":1530.0},{"x":10.699593728402919,"y":469.0},{"x":11.11579128845505,"y":0.0},{"x":11.531988848507181,"y":27.0},{"x":11.948186408559312,"y":0}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"507140b7-6954-4ac2-b284-e8f27e05daa1\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"507140b7-6954-4ac2-b284-e8f27e05daa1\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"507140b7-6954-4ac2-b284-e8f27e05daa1\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"507140b7-6954-4ac2-b284-e8f27e05daa1\", :values ({:x 2.375642527360281, :y 0} {:x 2.791840087412413, :y 4.0} {:x 3.208037647464545, :y 6.0} {:x 3.6242352075166773, :y 66.0} {:x 4.040432767568809, :y 196.0} {:x 4.456630327620942, :y 953.0} {:x 4.872827887673074, :y 3430.0} {:x 5.289025447725206, :y 10471.0} {:x 5.705223007777338, :y 32026.0} {:x 6.12142056782947, :y 61742.0} {:x 6.537618127881602, :y 112043.0} {:x 6.9538156879337345, :y 149848.0} {:x 7.370013247985867, :y 189959.0} {:x 7.786210808037999, :y 174603.0} {:x 8.202408368090131, :y 124498.0} {:x 8.618605928142262, :y 80758.0} {:x 9.034803488194393, :y 40208.0} {:x 9.451001048246525, :y 11616.0} {:x 9.867198608298656, :y 5547.0} {:x 10.283396168350787, :y 1530.0} {:x 10.699593728402919, :y 469.0} {:x 11.11579128845505, :y 0.0} {:x 11.531988848507181, :y 27.0} {:x 11.948186408559312, :y 0})}], :width 400, :height 247.2188, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; @@
(let [mu-min (reduce min mu-values)
      mu-max (reduce max mu-values)]
  (plot/plot #(exp (observe posterior %)) [mu-min mu-max]))
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"357b02d9-9ee6-4515-b41e-1697ce3c59fc","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"357b02d9-9ee6-4515-b41e-1697ce3c59fc","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"357b02d9-9ee6-4515-b41e-1697ce3c59fc"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"stroke":{"value":"#FF29D2"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"357b02d9-9ee6-4515-b41e-1697ce3c59fc","values":[{"x":2.375642527360281,"y":2.814265864677348E-7},{"x":2.463044014293512,"y":4.670888594129433E-7},{"x":2.5504455012267435,"y":7.681619551172633E-7},{"x":2.637846988159975,"y":1.2517712651019644E-6},{"x":2.7252484750932062,"y":2.021231513650351E-6},{"x":2.8126499620264376,"y":3.2338959519256866E-6},{"x":2.900051448959669,"y":5.126900908025777E-6},{"x":2.9874529358929003,"y":8.053834544273894E-6},{"x":3.0748544228261316,"y":1.2536300162395232E-5},{"x":3.162255909759363,"y":1.9335480088576434E-5},{"x":3.2496573966925943,"y":2.9550132768528315E-5},{"x":3.3370588836258257,"y":4.4748946163726434E-5},{"x":3.424460370559057,"y":6.714676387327186E-5},{"x":3.5118618574922884,"y":9.983579034042826E-5},{"x":3.5992633444255198,"y":1.4708430505201253E-4},{"x":3.686664831358751,"y":2.1471644317718402E-4},{"x":3.7740663182919825,"y":3.1058692919044996E-4},{"x":3.861467805225214,"y":4.451639058670973E-4},{"x":3.948869292158445,"y":6.322307466708316E-4},{"x":4.0362707790916765,"y":8.897134992659179E-4},{"x":4.123672266024908,"y":0.0012406339066623623},{"x":4.211073752958139,"y":0.001714178373302116},{"x":4.298475239891371,"y":0.002346860502185738},{"x":4.385876726824602,"y":0.003183738864840915},{"x":4.473278213757833,"y":0.00427963273896677},{"x":4.560679700691065,"y":0.005700257337831434},{"x":4.648081187624296,"y":0.007523177739591856},{"x":4.735482674557527,"y":0.009838459030450707},{"x":4.822884161490759,"y":0.012748871374623796},{"x":4.91028564842399,"y":0.016369495559820802},{"x":4.997687135357221,"y":0.020826570097175784},{"x":5.085088622290453,"y":0.026255428303484183},{"x":5.172490109223684,"y":0.03279739581685168},{"x":5.2598915961569155,"y":0.04059555787785668},{"x":5.347293083090147,"y":0.04978936251913001},{"x":5.434694570023378,"y":0.06050810008109676},{"x":5.5220960569566095,"y":0.07286338885300692},{"x":5.609497543889841,"y":0.08694089666133255},{"x":5.696899030823072,"y":0.10279163230527294},{"x":5.784300517756304,"y":0.12042324039990572},{"x":5.871702004689535,"y":0.13979181859100412},{"x":5.959103491622766,"y":0.16079483683111565},{"x":6.046504978555998,"y":0.18326576447038173},{"x":6.133906465489229,"y":0.20697099393074747},{"x":6.22130795242246,"y":0.23160958409024993},{"x":6.308709439355692,"y":0.25681623043307056},{"x":6.396110926288923,"y":0.2821677053656881},{"x":6.483512413222154,"y":0.3071928086827653},{"x":6.570913900155386,"y":0.3313856376276672},{"x":6.658315387088617,"y":0.35422174505223203},{"x":6.7457168740218485,"y":0.3751765223524159},{"x":6.83311836095508,"y":0.3937449417037951},{"x":6.920519847888311,"y":0.4094616392226226},{"x":7.007921334821543,"y":0.4219202335280232},{"x":7.095322821754774,"y":0.4307907642683839},{"x":7.182724308688005,"y":0.43583420749321977},{"x":7.270125795621237,"y":0.4369131769399241},{"x":7.357527282554468,"y":0.43399814256118674},{"x":7.444928769487699,"y":0.4271687734296827},{"x":7.532330256420931,"y":0.416610319781884},{"x":7.619731743354162,"y":0.4026052635231478},{"x":7.707133230287393,"y":0.38552076244464956},{"x":7.794534717220625,"y":0.3657926669769423},{"x":7.881936204153856,"y":0.34390707996418396},{"x":7.9693376910870874,"y":0.3203805461234182},{"x":8.056739178020319,"y":0.2957399921384503},{"x":8.14414066495355,"y":0.2705034918133791},{"x":8.231542151886782,"y":0.24516281151065558},{"x":8.318943638820013,"y":0.22016851323335968},{"x":8.406345125753244,"y":0.19591817435121392},{"x":8.493746612686476,"y":0.17274804440941466},{"x":8.581148099619707,"y":0.15092822101825717},{"x":8.668549586552938,"y":0.13066120691932814},{"x":8.75595107348617,"y":0.11208352392963618},{"x":8.843352560419401,"y":0.0952699170559465},{"x":8.930754047352632,"y":0.08023958916466177},{"x":9.018155534285864,"y":0.06696386383322837},{"x":9.105557021219095,"y":0.05537467774124407},{"x":9.192958508152326,"y":0.04537334715264339},{"x":9.280359995085558,"y":0.0368391264234759},{"x":9.367761482018789,"y":0.02963716972004265},{"x":9.45516296895202,"y":0.023625609982039216},{"x":9.542564455885252,"y":0.01866157232279942},{"x":9.629965942818483,"y":0.014606034873195783},{"x":9.717367429751715,"y":0.011327532924282235},{"x":9.804768916684946,"y":0.008704768627296062},{"x":9.892170403618177,"y":0.006628237018579093},{"x":9.979571890551409,"y":0.00500101004364161},{"x":10.06697337748464,"y":0.003738835197878752},{"x":10.154374864417871,"y":0.0027697069277266475},{"x":10.241776351351103,"y":0.0020330600712111383},{"x":10.329177838284334,"y":0.0014787184911235279},{"x":10.416579325217565,"y":0.0010657116057413082},{"x":10.503980812150797,"y":7.610492914046107E-4},{"x":10.591382299084028,"y":5.385236435666489E-4},{"x":10.67878378601726,"y":3.775858093534219E-4},{"x":10.76618527295049,"y":2.623284852427392E-4},{"x":10.853586759883722,"y":1.8059018295343265E-4},{"x":10.940988246816953,"y":1.2318610495369925E-4},{"x":11.028389733750185,"y":8.326226248173526E-5},{"x":11.115791220683416,"y":5.5763957960537286E-5}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"357b02d9-9ee6-4515-b41e-1697ce3c59fc\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"357b02d9-9ee6-4515-b41e-1697ce3c59fc\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"357b02d9-9ee6-4515-b41e-1697ce3c59fc\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :stroke {:value \"#FF29D2\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"357b02d9-9ee6-4515-b41e-1697ce3c59fc\", :values ({:x 2.375642527360281, :y 2.814265864677348E-7} {:x 2.463044014293512, :y 4.670888594129433E-7} {:x 2.5504455012267435, :y 7.681619551172633E-7} {:x 2.637846988159975, :y 1.2517712651019644E-6} {:x 2.7252484750932062, :y 2.021231513650351E-6} {:x 2.8126499620264376, :y 3.2338959519256866E-6} {:x 2.900051448959669, :y 5.126900908025777E-6} {:x 2.9874529358929003, :y 8.053834544273894E-6} {:x 3.0748544228261316, :y 1.2536300162395232E-5} {:x 3.162255909759363, :y 1.9335480088576434E-5} {:x 3.2496573966925943, :y 2.9550132768528315E-5} {:x 3.3370588836258257, :y 4.4748946163726434E-5} {:x 3.424460370559057, :y 6.714676387327186E-5} {:x 3.5118618574922884, :y 9.983579034042826E-5} {:x 3.5992633444255198, :y 1.4708430505201253E-4} {:x 3.686664831358751, :y 2.1471644317718402E-4} {:x 3.7740663182919825, :y 3.1058692919044996E-4} {:x 3.861467805225214, :y 4.451639058670973E-4} {:x 3.948869292158445, :y 6.322307466708316E-4} {:x 4.0362707790916765, :y 8.897134992659179E-4} {:x 4.123672266024908, :y 0.0012406339066623623} {:x 4.211073752958139, :y 0.001714178373302116} {:x 4.298475239891371, :y 0.002346860502185738} {:x 4.385876726824602, :y 0.003183738864840915} {:x 4.473278213757833, :y 0.00427963273896677} {:x 4.560679700691065, :y 0.005700257337831434} {:x 4.648081187624296, :y 0.007523177739591856} {:x 4.735482674557527, :y 0.009838459030450707} {:x 4.822884161490759, :y 0.012748871374623796} {:x 4.91028564842399, :y 0.016369495559820802} {:x 4.997687135357221, :y 0.020826570097175784} {:x 5.085088622290453, :y 0.026255428303484183} {:x 5.172490109223684, :y 0.03279739581685168} {:x 5.2598915961569155, :y 0.04059555787785668} {:x 5.347293083090147, :y 0.04978936251913001} {:x 5.434694570023378, :y 0.06050810008109676} {:x 5.5220960569566095, :y 0.07286338885300692} {:x 5.609497543889841, :y 0.08694089666133255} {:x 5.696899030823072, :y 0.10279163230527294} {:x 5.784300517756304, :y 0.12042324039990572} {:x 5.871702004689535, :y 0.13979181859100412} {:x 5.959103491622766, :y 0.16079483683111565} {:x 6.046504978555998, :y 0.18326576447038173} {:x 6.133906465489229, :y 0.20697099393074747} {:x 6.22130795242246, :y 0.23160958409024993} {:x 6.308709439355692, :y 0.25681623043307056} {:x 6.396110926288923, :y 0.2821677053656881} {:x 6.483512413222154, :y 0.3071928086827653} {:x 6.570913900155386, :y 0.3313856376276672} {:x 6.658315387088617, :y 0.35422174505223203} {:x 6.7457168740218485, :y 0.3751765223524159} {:x 6.83311836095508, :y 0.3937449417037951} {:x 6.920519847888311, :y 0.4094616392226226} {:x 7.007921334821543, :y 0.4219202335280232} {:x 7.095322821754774, :y 0.4307907642683839} {:x 7.182724308688005, :y 0.43583420749321977} {:x 7.270125795621237, :y 0.4369131769399241} {:x 7.357527282554468, :y 0.43399814256118674} {:x 7.444928769487699, :y 0.4271687734296827} {:x 7.532330256420931, :y 0.416610319781884} {:x 7.619731743354162, :y 0.4026052635231478} {:x 7.707133230287393, :y 0.38552076244464956} {:x 7.794534717220625, :y 0.3657926669769423} {:x 7.881936204153856, :y 0.34390707996418396} {:x 7.9693376910870874, :y 0.3203805461234182} {:x 8.056739178020319, :y 0.2957399921384503} {:x 8.14414066495355, :y 0.2705034918133791} {:x 8.231542151886782, :y 0.24516281151065558} {:x 8.318943638820013, :y 0.22016851323335968} {:x 8.406345125753244, :y 0.19591817435121392} {:x 8.493746612686476, :y 0.17274804440941466} {:x 8.581148099619707, :y 0.15092822101825717} {:x 8.668549586552938, :y 0.13066120691932814} {:x 8.75595107348617, :y 0.11208352392963618} {:x 8.843352560419401, :y 0.0952699170559465} {:x 8.930754047352632, :y 0.08023958916466177} {:x 9.018155534285864, :y 0.06696386383322837} {:x 9.105557021219095, :y 0.05537467774124407} {:x 9.192958508152326, :y 0.04537334715264339} {:x 9.280359995085558, :y 0.0368391264234759} {:x 9.367761482018789, :y 0.02963716972004265} {:x 9.45516296895202, :y 0.023625609982039216} {:x 9.542564455885252, :y 0.01866157232279942} {:x 9.629965942818483, :y 0.014606034873195783} {:x 9.717367429751715, :y 0.011327532924282235} {:x 9.804768916684946, :y 0.008704768627296062} {:x 9.892170403618177, :y 0.006628237018579093} {:x 9.979571890551409, :y 0.00500101004364161} {:x 10.06697337748464, :y 0.003738835197878752} {:x 10.154374864417871, :y 0.0027697069277266475} {:x 10.241776351351103, :y 0.0020330600712111383} {:x 10.329177838284334, :y 0.0014787184911235279} {:x 10.416579325217565, :y 0.0010657116057413082} {:x 10.503980812150797, :y 7.610492914046107E-4} {:x 10.591382299084028, :y 5.385236435666489E-4} {:x 10.67878378601726, :y 3.775858093534219E-4} {:x 10.76618527295049, :y 2.623284852427392E-4} {:x 10.853586759883722, :y 1.8059018295343265E-4} {:x 10.940988246816953, :y 1.2318610495369925E-4} {:x 11.028389733750185, :y 8.326226248173526E-5} {:x 11.115791220683416, :y 5.5763957960537286E-5})}], :width 400, :height 247.2188, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; **
;;; ## Plot KL error as a function of number of samples
;; **

;; @@
(def num-sample-range (mapv (comp int (partial * number-of-samples))
                            [1e-3 2e-3 5e-3 1e-2 2e-2 5e-2 1e-1 2e-1 5e-1 1]))
  
(def KL-errors
  (map (fn [n]
         (let [mus (collect-by :mu (take n samples))
			   mean (s/empirical-mean mus)
               sd (s/empirical-std mus)]
			(kl-normal mean sd (:mean posterior) (:sd posterior))))
       num-sample-range))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;aistats-examples/KL-errors</span>","value":"#'aistats-examples/KL-errors"}
;; <=

;; @@
(plot/list-plot (map vector 
                     (map #(/ (log %) 
                              (log 10)) 
                          num-sample-range)
                     (map #(/ (log %) 
                              (log 10)) 
                          KL-errors))
                :joined true
                :color "#05A"
                :x-title "log number of samples"
                :y-title "log KL divergence")
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"120986b0-9142-4251-ba0b-51f18aac7855","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"120986b0-9142-4251-ba0b-51f18aac7855","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"120986b0-9142-4251-ba0b-51f18aac7855"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"stroke":{"value":"#05A"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"120986b0-9142-4251-ba0b-51f18aac7855","values":[{"x":2.9999999999999996,"y":-2.5129909315552106},{"x":3.301029995663981,"y":-0.9231773588694007},{"x":3.6989700043360187,"y":-1.1291571287355335},{"x":4.0,"y":-1.5954189198696342},{"x":4.30102999566398,"y":-1.6574444932772843},{"x":4.698970004336019,"y":-1.6674263679686838},{"x":5.0,"y":-2.126884693783321},{"x":5.301029995663981,"y":-2.769256531020848},{"x":5.698970004336018,"y":-2.9579110658880667},{"x":5.999999999999999,"y":-3.161614551843416}]}],"width":400,"height":247.2187957763672,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"120986b0-9142-4251-ba0b-51f18aac7855\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"120986b0-9142-4251-ba0b-51f18aac7855\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"120986b0-9142-4251-ba0b-51f18aac7855\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :stroke {:value \"#05A\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"120986b0-9142-4251-ba0b-51f18aac7855\", :values ({:x 2.9999999999999996, :y -2.5129909315552106} {:x 3.301029995663981, :y -0.9231773588694007} {:x 3.6989700043360187, :y -1.1291571287355335} {:x 4.0, :y -1.5954189198696342} {:x 4.30102999566398, :y -1.6574444932772843} {:x 4.698970004336019, :y -1.6674263679686838} {:x 5.0, :y -2.126884693783321} {:x 5.301029995663981, :y -2.769256531020848} {:x 5.698970004336018, :y -2.9579110658880667} {:x 5.999999999999999, :y -3.161614551843416})}], :width 400, :height 247.2188, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=