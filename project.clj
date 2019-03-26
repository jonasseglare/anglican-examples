(defproject anglican-examples "0.2.1-SNAPSHOT"
  :description "Anglican program examples expressed as Gorilla repl instances"
  :url "http://www.robots.ox.ac.uk/~fwood/anglican/"
  :license {:name "GNU General Public License Version 3; Other commercial licenses available."
            :url "http://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [http-kit "2.4.0-alpha3"]
                 [org.clojure/data.csv "0.1.3"]
                 [clj-auto-diff "0.1.3"]
                 [anglican "1.0.0"]]
  :plugins [;;[dtolpin/lein-gorilla "0.4.1-SNAPSHOT"]
            [org.clojars.gscacco/lein-gorilla "0.5.3"]
            [javax.xml.bind/jaxb-api "2.3.0"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
