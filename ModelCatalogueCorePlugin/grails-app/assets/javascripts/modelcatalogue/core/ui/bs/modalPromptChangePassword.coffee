angular.module('mc.core.ui.bs.modalPromptChangePassword', ['mc.util.messages', 'ngCookies']).config ['messagesProvider', (messagesProvider)->
  factory = [ '$modal', '$q', 'messages', 'security', ($modal, $q, messages, security) ->
    ->
      dialog = $modal.open {
        windowClass: 'change-password-modal-prompt'
        template: '''
         <div class="modal-header">
            <h4>Change Password</h4>
        </div>
        <div class="modal-body">
            <messages-panel messages="messages"></messages-panel>
            <form role="form" ng-submit="change()">
              <div class="form-group">
                <label for="password">Password</label>
                <input type="password" class="form-control" id="password" placeholder="Password" ng-model="user.password">
              </div>
              <div class="form-group">
                <label for="password2">Confirm Password</label>
                <input type="password" class="form-control" id="password2" placeholder="Confirm Password" ng-model="user.password2">
              </div>
              <button type="submit" class="hide" ng-click="change()"></button>
            </form>
        </div>
        <div class="modal-footer">
            <button type="submit" class="btn btn-success" ng-click="change()" ng-disabled="!user.password || !user.password2"><span class="glyphicon glyphicon-ok"></span> Save</button>
            <button class="btn btn-warning" ng-click="$dismiss()">Cancel</button>
        </div>
        '''
        controller: ['$scope', '$cookies', 'messages', 'security', '$modalInstance',
          ($scope, $cookies, messages, security, $modalInstance) ->
            $scope.user = {}
            $scope.messages = messages.createNewMessages()

            $scope.change = ->
              $scope.messages.clearAllMessages()
              security.changePassword($scope.user.password, $scope.user.password2).then (success)->
                if success.data.error
                  $scope.messages.error success.data.error
                else if success.data.errors
                  for error in success.data.errors
                    $scope.messages.error error
                else
                  $modalInstance.close success
        ]

      }

      dialog.result
  ]

  messagesProvider.setPromptFactory 'change-password', factory
]