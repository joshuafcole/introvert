(defproject introvert "0.1.5"
  :description "A small utility belt for introspecting into clojurescript."
  :url "http://github.com/joshuafcole/introvert"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2127"]]
  :plugins [[lein-cljsbuild "1.0.1"]
            [com.cemerick/clojurescript.test "0.2.1"]]

  :hooks [leiningen.cljsbuild]

  :cljsbuild {:builds [{:source-paths ["src" "test"]
                        :compiler {:optimizations :whitespace
                                   :source-map "target/introvert.js.map"
                                   :output-to "target/introvert.js"
                                   :output-dir "target/"
                                   :pretty-print true}}]

              :test-commands {"unit-tests" ["phantomjs" :runner
                                          "window.literal_js_was_evaluated=true"
                                          "target/introvert.js"]}
              })
