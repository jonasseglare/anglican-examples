(defproject anglican-examples "0.2.1-SNAPSHOT"
  :description "Anglican program examples expressed as Gorilla repl instances"
  :url "http://www.robots.ox.ac.uk/~fwood/anglican/"
  :license {:name "GNU General Public License Version 3; Other commercial licenses available."
            :url "http://www.gnu.org/licenses/gpl.html"}

  ;; https://github.com/bhauman/lein-figwheel/issues/612
  ;;:jvm-opts ["--add-modules" "java.xml.bind"]
  
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [clj-auto-diff "0.1.3"]
                 [http-kit "2.4.0-alpha3"]
                 [anglican "1.0.0"]
                 

                 ;; https://stackoverflow.com/questions/43574426/how-to-resolve-java-lang-noclassdeffounderror-javax-xml-bind-jaxbexception-in-j

                 [
                  javax.xml.bind/jaxb-api
                 "2.2.11"
                 ]
                 [
                 com.sun.xml.bind/jaxb-core
                 "2.2.11"
                 ]
                 [
                 com.sun.xml.bind/jaxb-impl
                 "2.2.11"
                 ]
                 [
                 javax.activation/activation
                 "1.1.1"
                 ]]
  :plugins [
            ;;[org.clojars.benfb/lein-gorilla "0.5.0"]
            [org.clojars.gscacco/lein-gorilla "0.5.3"]
            [javax.xml.bind/jaxb-api "2.3.0"]
            ;;[dtolpin/lein-gorilla "0.4.1-SNAPSHOT"]
            ]
  ;;:java-cmd "/home/jonas/local/jdk8/bin/java"
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
