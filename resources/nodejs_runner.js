var path = require("path"),
  files = process.argv.slice(2);

files.forEach(function (f) {
    var file = path.join(process.cwd(), f);
    try {
        require(file);
    } catch (e) {
        console.log("Error in file: " + file);
        console.log(e);
    }
});

var success = (function() {
    var results = cemerick.cljs.test.run_all_tests();
    return cemerick.cljs.test.successful_QMARK_(results);
})();

process.exit(success ? 0 : 1);
