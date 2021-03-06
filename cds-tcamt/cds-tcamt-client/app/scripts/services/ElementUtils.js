/**
 * Created by haffo on 3/9/16.
 */
'use strict';
angular.module('tcl').factory('ElementUtils',
    ['$rootScope', 'ViewSettings', function ($rootScope, ViewSettings) {
        var ElementUtils = {
            filterConstraints: function (node, constraints) {
                if (constraints) {
                    return $filter('filter')(constraints, {constraintTarget: node.position + '[1]'}, true);
                }
                return null;
            },
            isRelevant: function (node, predicates) {
                if (predicates && predicates != null && predicates.length > 0) {
                    return  predicates[0].trueUsage === "R" || predicates[0].trueUsage === "RE" || predicates[0].falseUsage === "R" || predicates[0].falseUsage === "RE";
                } else {
                    return node.usage == null || !node.usage || node.usage === "R" || node.usage === "RE" || node.usage === "C";
                }
            },
            setUsage: function (node) {
                if( node.usage && node.min) {
                    if( node.usage === "R" && node.min == 0){
                        node.min = 1;
                    }
                    if( node.usage === "0"){
                        node.min = 0;
                    }
                }
            }
        };
        return ElementUtils;
    }]);
