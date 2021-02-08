# automated-api-testing
Set up API / web test cases with ease!

https://oz-web.com/apitesting/api.php

work in progress . . .

# Usage
## Defining test cases
```
{
   "tests":[
      {
         "name":"doLogin",
         "call":"https://%%<url>%%?action=login",
         "method":"POST",
         "responseCode":200,
         "timeout":500,
         "body":"loginbody",
         "contentType":"application/x-www-form-urlencoded",
         "responseContains":[
            "%%<contains>%%"
         ],
         "headers":[
            {
               "X-Custom-Header":"f00bar"
            }
         ],
         "extractVariableBody":[
            {
               "secret":"secret=([0-9]*)"
            }
         ],
         "extractVariableHeader":[
            {
               "xSessionHeader":"X-Session"
            }
         ]
      },{
         "name":"doGet",
         "call":"https://%%<url>%%?action=get",
         "method":"GET",
         "responseCode":200,
         "timeout":500,
         "responseContains":[
            "2020-10-23"
         ],
         "headers":[
            {
               "X-Session":"%%<xSessionHeader>%%"
            }
         ]
      },{
         "name":"doGetNoAuth",
         "call":"https://%%<url>%%?action=get",
         "method":"GET",
         "responseCode":403,
         "timeout":500
      }
   ],
   "variables":[
      {
         "url":"oz-web.com/apitesting/api.php"
      },
      {
         "password":"letmein"
      },
      {
         "contains":"Logged in"
      }
   ],
   "proxy":{
      "address": "127.0.0.1",
      "port": 5016
   }
}
```
Externalizing Test Cases into files by

      {
         "include":"post.json",
         "includeName":"doPost"
      }
      
      
```
{
   "name":"doPost",
   "call":"https://%%<url>%%",
   "method":"POST",
   "responseCode":200,
   "body":"body",
   "contentType":"application/json",
   "responseContains":[
      "postbody!",
      "1234",
      "abcd"
   ],
   "variables":[
      {
         "example_static_var":"f00bar"
      }
   ]
}
```
Login Body
```
user=user&password=%%<password>%%
```


# API PHP Mockup Code
```php
<?php
	if (isset($_GET['action'])){
		if($_GET['action']=="login" && $_SERVER['REQUEST_METHOD'] == 'POST'){
			if(isset($_POST["user"]) && isset($_POST["password"])){
				if($_POST["password"]==="letmein" && $_POST["user"]==="user"){
					echo("secret=123456789 - Logged in");
					header("X-Session: 123456789");
				}else{
					$user = htmlspecialchars($_POST["user"], ENT_QUOTES, 'UTF-8');
					$pw = htmlspecialchars($_POST["password"], ENT_QUOTES, 'UTF-8');
					echo("Username '".$user."' or password '".$pw."' wrong!");
					http_response_code(403);
				}
			}else{
				echo("Missing parameters user and password");
			}
		}else{
			$headers = getRequestHeaders();

			if(isset($headers["X-Session"])){
				if($headers["X-Session"]=="123456789"){
					if($_GET['action']=="logout" && $_SERVER['REQUEST_METHOD'] == 'POST'){
						echo("Good bye!");
					}else if($_GET['action']=="get" && $_SERVER['REQUEST_METHOD'] == 'GET'){
						echo('{"id" = 5, "created" = "2020-10-23"}');
					}else if($_GET['action']=="create" && $_SERVER['REQUEST_METHOD'] == 'POST'){
						echo('{"id" = 5}');
					}
				}else{
					echo "Invalid Session ID";
					http_response_code(401);
				}
			}else{
				echo "You are not logged in";
				http_response_code(403);
			}
		}
	}else{
		echo("No action defined");
	}


	function getRequestHeaders() {
		$headers = array();
		foreach($_SERVER as $key => $value) {
			if (substr($key, 0, 5) <> 'HTTP_') {
				continue;
			}
			$header = str_replace(' ', '-', ucwords(str_replace('_', ' ', strtolower(substr($key, 5)))));
			$headers[$header] = $value;
		}
		return $headers;
	}
?>
```
