angular.module('mc.core.ui.bs.modalPromptRegister', ['mc.util.messages', 'ngCookies']).config ['messagesProvider', (messagesProvider)->
  factory = [ '$modal', '$q', 'messages', 'security', ($modal, $q, messages, security) ->
    ->
      dialog = $modal.open {
        windowClass: 'register-modal-prompt'
        template: '''
         <div class="modal-header">
            <h4>Register</h4>
        </div>
        <div class="modal-body">
            <messages-panel messages="messages"></messages-panel>
            <form role="form" ng-submit="change()">


                  <div class="form-group">
                    <label for="firstName">First Name*</label>
                    <input type="text" class="form-control" id="firstName" placeholder="FirstName" ng-model="user.firstName">
                  </div>



                  <div class="form-group">
                    <label for="lastName">LastName*</label>
                    <input type="text" class="form-control" id="lastName" placeholder="LastName" ng-model="user.lastName">
                  </div>




              <div class="form-group">
                <label for="username">Username*</label>
                <input type="text" class="form-control" id="username" placeholder="Username" ng-model="user.username">
              </div>


              <div class="form-group">
                <label for="email">Email*</label>
                <input type="text" class="form-control" id="email" placeholder="Email" ng-model="user.email">
              </div>


              <div class="form-group">
                <label for="password">Password*</label>
                <input type="password" class="form-control" id="password" placeholder="Password" ng-model="user.password">
              </div>


              <div class="form-group">
                <label for="password2">Password(again)*</label>
                <input type="password" class="form-control" id="password2" placeholder="Password2" ng-model="user.password2">
              </div>



              <button type="submit" class="hide" ng-click="change()"></button>
            </form>
        </div>
        <div class="modal-footer">
            <button type="submit" class="btn btn-success" ng-click="change()" ng-disabled="!user.password || !user.password2 || !user.username || !user.email || !user.firstName || !user.lastName"><span class="glyphicon glyphicon-ok"></span> Save</button>
            <button class="btn btn-warning" ng-click="$dismiss()">Cancel</button>
        </div>
        '''
        controller: ['$scope', '$cookies', 'messages', 'security', '$modalInstance',
          ($scope, $cookies, messages, security, $modalInstance) ->
            $scope.user = {}
            $scope.messages = messages.createNewMessages()

            $scope.change = ->
              $scope.messages.clearAllMessages()
              security.register($scope.user).then (success)->
                if success.data.error
                  $scope.messages.error success.data.error
                else if success.data.errors
                  for error in success.data.errors
                    $scope.messages.error error
                else
                  $modalInstance.close success
                  messages.success('Your account registration email was sent - check your mail!','')
        ]

      }

      dialog.result
  ]

  messagesProvider.setPromptFactory 'register', factory
]