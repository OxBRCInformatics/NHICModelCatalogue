<%@ page import="grails.util.Environment; org.modelcatalogue.core.util.CDN" %>
<html>
<head>
    <title><g:message code='spring.security.ui.resetPassword.title'/></title>
    %{--<asset:stylesheet href="modelcatalogue.css"/>--}%
    %{--<asset:javascript src="angular/angular.js"/>--}%


    <g:if test="${CDN.preferred}">
        <g:set var="minSuffix" value="${Environment.current == Environment.TEST ? '' : '.min'}"/>
        <!-- CDNs -->
        <link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.2.0/css/bootstrap${minSuffix}.css">
        <link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.2.0/css/font-awesome${minSuffix}.css">

        <script type="application/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.1/jquery${minSuffix}.js"></script>
        <script type="application/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.4/jquery-ui${minSuffix}.js"></script>
        <script type="application/javascript" src="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.2.0/js/bootstrap${minSuffix}.js"></script>
        <script type="application/javascript" src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.3.15/angular${minSuffix}.js"></script>
        <script type="application/javascript" src="//cdnjs.cloudflare.com/ajax/libs/angular-ui-bootstrap/0.11.2/ui-bootstrap-tpls${minSuffix}.js"></script>

        <!-- i18n 1.3.15 not present but hopefuly it's the same -->
        <script type="application/javascript" src="//cdnjs.cloudflare.com/ajax/libs/angular-i18n/1.2.15/angular-locale_en-gb.js"></script>

        <script type="application/javascript" src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.3.15/angular-animate${minSuffix}.js"></script>
        <script type="application/javascript" src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.3.15/angular-sanitize${minSuffix}.js"></script>
        <script type="application/javascript" src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.3.15/angular-cookies${minSuffix}.js"></script>

        <!-- code -->
        <asset:stylesheet href="modelcatalogue.css"/>
        <asset:javascript src="modelcatalogue/modelcatalogue.js"/>
    </g:if>
    <g:else>
        <asset:stylesheet href="bootstrap/dist/css/bootstrap.css"/>
        <asset:stylesheet href="font-awesome/css/font-awesome"/>
        <asset:stylesheet href="modelcatalogue.css"/>

        <asset:javascript src="jquery/dist/jquery.js"/>
        <asset:javascript src="jquery-ui/jquery-ui.js"/>
        <asset:javascript src="bootstrap/dist/js/bootstrap.js"/>
        <asset:javascript src="angular/angular.js"/>
        <asset:javascript src="angular-animate/angular-animate.js"/>
        <asset:javascript src="angular-bootstrap/ui-bootstrap-tpls.js"/>
        <asset:javascript src="angular-cookies/angular-cookies.js"/>
        <asset:javascript src="angular-sanitize/angular-sanitize.js"/>
        <asset:javascript src="angular-animate/angular-animate.js"/>
        <asset:javascript src="modelcatalogue/modelcatalogue.js"/>
    </g:else>





</head>

<body>
<div id="wrap">
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
        <div class="container">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="${createLink(uri: '/')}">Model Catalogue</a>
            </div>
        </div>
    </div>


    <div class="container" ng-app="resetPasswordApp" ng-controller="resetPasswordCtrl">
        <div id="info" class="row">
            <div class="col-sm-4 col-sm-offset-4">
                <div class="panel panel-default">
                    <!-- Default panel contents -->
                    <div class="panel-heading">
                        <strong> <g:message code='spring.security.ui.resetPassword.title'/></strong>
                    </div>

                    <div class="panel-body">

                        <g:if test='${flash.error}'>
                            <div class="alert alert-danger" role="alert">
                                ${flash.error}
                            </div>
                        </g:if>

                        <form action='resetPassword' method='POST' id='loginForm' class='cssform' autocomplete='off'
                              name='resetPasswordForm'>
                            <g:hiddenField name='t' value='${token}'/>

                            <div class="sign-in">

                                <div class="form-group">
                                    <label for="password">Password</label>
                                    <input type="password" class="form-control" id="password" name='password'
                                           placeholder="Password" value="${command?.password}" ng-model="password">
                                </div>

                                <div class="form-group">
                                    <label for="password2">Confirm Password</label>
                                    <input type="password" class="form-control" id="password2" name='password2'
                                           placeholder="Confirm Password" value="${command?.password2}"
                                           ng-model="password2">
                                </div>

                                <button type='submit' class="btn btn-success" id="submit"
                                        ng-disabled="!password || !password2"><span
                                        class="glyphicon glyphicon-ok"></span> Update my password</button>
                            </div>
                        </form>

                    </div>
                </div>
            </div>
        </div>
    </div>

</div>

<div id="footer">
    <div class="container">
        <p class="muted credit">

        <div class="row">
            <div class="col-sm-4 nav-left">
                <div show-if-logged-in>
                    <p class="feedback text-muted" id="feedback"></p>
                </div>
            </div>

            <div class="col-sm-4"><p
                    class="text-muted">Model Catalogue &copy; ${new Date()[Calendar.YEAR]} &nbsp&nbspv<g:meta
                        name="app.version"/></p></div>

            <div class="col-sm-4"></div>
        </div>
    </p>
    </div>
</div>

<script>
    $(document).ready(function () {
        $('#password').focus();
    });

</script>


<script type="text/javascript">
    var demoConfig = angular.module('resetPasswordApp', [])
    demoConfig.controller('resetPasswordCtrl', ["$scope", function ($scope) {
    }])
</script>

</body>
</html>