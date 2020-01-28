(def junit-jupiter-version "5.6.0")
(def mockito-version "3.2.4")

(defproject java-after-eight-clj "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :source-paths      ["src/main/clj"]
  :java-source-paths ["src/main/java"]
  :test-paths ["src/test/java" "src/test/clj"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.junit.jupiter/junit-jupiter-api ~junit-jupiter-version]
                                  [org.junit.jupiter/junit-jupiter-params ~junit-jupiter-version]
                                  [org.mockito/mockito-core ~mockito-version]
                                  [org.mockito/mockito-junit-jupiter ~mockito-version]
                                  [org.assertj/assertj-core "3.14.0"]]}})
