/**
 * Created by haffo on 10/20/15.
 */
angular.module('tcl').directive('compile', function ($compile) {
    return function(scope, element, attrs) {
        scope.$watch(
            function(scope) {
                // watch the 'compile' expression for changes
                return scope.$eval(attrs.compile);
            },
            function(value) {
                // when the 'compile' expression changes
                // assign it into the current DOM
                element.html(value);

                // compile the new DOM and link it to the current
                // scope.
                // NOTE: we only compile .childNodes so that
                // we don't get into infinite loop compiling ourselves
                $compile(element.contents())(scope);
            }
        );
    };
});

angular.module('tcl').directive('dynamic', function ($compile) {
    return {
      restrict: 'A',
      replace: true,
      scope: { dynamic: '=dynamic'},
      link: function postLink(scope, element, attrs) {
        scope.$watch( 'dynamic' , function(html){
          element.html(html);
          $compile(element.contents())(scope);
        });
      }
    };
  });