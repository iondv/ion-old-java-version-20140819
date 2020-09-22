// Karma configuration
// Generated on Wed May 06 2015 19:03:49 GMT+1000 (VLAT)

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',


    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine'],


    // list of files / patterns to load in the browser
    files: [ 
        'src/main/webapp/resources/ionweb-spa/js/lib/jquery.min.js',
        // 'src/main/webapp/resources/ionweb-spa/js/lib/jquery.cookie.js',
        // 'src/main/webapp/resources/ionweb-spa/js/lib/jquery-ui.min.js',
        // src/main/webapp/resources/ionweb-spa/js/lib/jquery.ui.datepicker-ru.js,
        // src/main/webapp/resources/ionweb-spa/js/jquery.maskedinput.js,
        // src/main/webapp/resources/ionweb-spa/js/lib/select2.js,
        'src/main/webapp/resources/ionweb-spa/js/lib/angular/angular.min.js',
        'src/main/webapp/resources/ionweb-spa/js/lib/angular/angular-mocks.js',
        'src/main/webapp/resources/ionweb-spa/js/lib/angular/angular-route.min.js',
        'src/main/webapp/resources/ionweb-spa/js/lib/angular/angular-messages.min.js',
        'src/main/webapp/resources/ionweb-spa/js/lib/angular/multi-transclude.js',
        'src/main/webapp/resources/ionweb-spa/js/lib/angular/select.min.js',
        'src/main/webapp/resources/ionweb-spa/js/lib/angular/angular-sanitize.min.js',
        'src/main/webapp/resources/ionweb-spa/js/lib/angular/date.js',
        'src/main/webapp/resources/ionweb-spa/js/lib/modernizr-respond-html5.min.js',
        'src/main/webapp/resources/ionweb-spa/js/ion-ng/*.js',
        // src/main/webapp/resources/ionweb-spa/js/ion-ng/ion-ng-create.js,
        // src/main/webapp/resources/ionweb-spa/js/ion-ng/ion-ng-listfield.js,
        // 'src/main/webapp/resources/ionweb-spa/js/ion-ng/ion-ng-list.js',
        // src/main/webapp/resources/ionweb-spa/js/ion-ng/ion-ng-mtpl.js,
        // src/main/webapp/resources/ionweb-spa/js/ion-ng/ion-ng-sidebar.js,
        // src/main/webapp/resources/ionweb-spa/js/ion-ng/ion-ng-formfield.js,
     // 'src/main/webapp/resources/ionweb-spa/js/lib/*.js',
     // 'src/main/webapp/resources/ionweb-spa/js/lib/angular/*.js',
      'src/main/webapp/resources/ionweb-spa/js/ion-ng/ng-test/*.js'
    ],


    // list of files to exclude
    exclude: [
    ],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
    },


    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['progress'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['Chrome'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: false
  });
};
