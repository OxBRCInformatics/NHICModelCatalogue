angular.module('mc.core.ui.bs.propertiesPane', ['mc.core.ui.propertiesPane', 'ngSanitize']).run [ '$templateCache', ($templateCache) ->
    $templateCache.put 'modelcatalogue/core/ui/propertiesPane.html', '''
      <table class="pp-table table">
        <thead>
          <th class="col-md-4">{{title}}</th>
          <th class="col-md-6">{{valueTitle}}</th>
        </thead>
        <tbody>
          <tr class="pp-table-property-row" ng-repeat="property in properties" ng-switch="displayType(property.value, item)" ng-init="theValue = evaluateValue(property.value, item)">
            <th class="pp-table-property-label col-md-4">{{property.label}}</th>
            <td class="pp-table-property-value-no-wrap col-md-8 pp-table-property-element-value" data-value-for="{{property.label}}"  ng-switch-when="element"><span ng-class="theValue.getIcon()" class="text-muted"></span> <a ng-click="theValue.show()">{{theValue.name}}</a> <span class="fa fa-fw" ng-class="{'fa-plus-square-o': (!theValue._expanded &amp;&amp; !theValue._expanding), 'fa-refresh': theValue._expanding, 'fa-minus-square-o':(theValue._expanded &amp;&amp; !theValue._expanding)}" ng-click="expandOrCollapse(theValue)"></span><div collapse="!theValue._expanded">
              <blockquote>{{theValue.description ? theValue.description : 'No Description' }}</blockquote>
              <div ng-show="theValue.enumerations" class="preserve-new-lines"><strong>Enumerations</strong>
                {{getEnumerations(theValue)}}
              </div>
              <div ng-if="theValue.dataType"><span class="text-muted"><catalogue-element-icon type="'dataType'"></catalogue-element-icon></span> <a ng-href="{{theValue.dataType.href()}}">{{theValue.dataType.name}}</a></div>
              <div ng-show="theValue.dataType.enumerations" class="preserve-new-lines"><strong>Enumerations</strong>
                {{getEnumerations(theValue.dataType)}}
              </div>
            </div></td>
            <td class="pp-table-property-value col-md-8 pp-table-property-element-value" data-value-for="{{property.label}}" ng-switch-when="date">{{theValue | date:'short'}}</td>
            <td class="pp-table-property-value col-md-8 pp-table-property-element-value" data-value-for="{{property.label}}" ng-switch-when="elementArray"><span ng-repeat="element in theValue"><a class="label with-pointer" ng-class="{'label-warning': element.status == 'DRAFT', 'label-info': element.status == 'PENDING', 'label-primary': element.status == 'FINALIZED', 'label-danger': element.status == 'DEPRECATED'}" ng-href="{{element.href()}}"><span ng-class="element.getIcon()"></span> {{element.name}}</a> </span></td>
            <td class="pp-table-property-value col-md-8 pp-table-property-element-value" data-value-for="{{property.label}}" ng-switch-when="array"><span ng-repeat="element in theValue">{{element}}<span ng-hide="$last">, </span></span></td>
            <td class="pp-table-property-value col-md-8 pp-table-property-element-value" data-value-for="{{property.label}}" ng-switch-default ng-bind-html="theValue == null ? '' : ('' + theValue) | linky:'_blank'"></td>
          </tr>
        </tbody>
      </table>
    '''
  ]