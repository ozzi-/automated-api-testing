# automated-api-testing
AAT lets you define HTTP API testcases through convenient JSON files.
It supports:
- Loading request bodies from a file
- Sending custom headers
- Checking the response body for a string
- Checking the response code
- Variables (more on this below)

Variables allow you to save values from prior requests and use them in subsequent requests.
They can be defined in the test json (such as declaring the base URL for your tests only at one place) or be set dynamically, by extracting it from the response body (using regular expressions) or the response headers. This allows for complex test flows as well as achieve statefulness during each test case.

# Usage

## Parameters

```
-t testfile       Path to the test file  (required)
-r resultfile     Path to result file    (required)
-v verbose        Verbose Mode
```
Example:
-t D:\ATT\tests.json -v -r D:\ATT\results

## Result files
ATT creates result files, the files are named after each test case and contain useful information:
```
Method - Call:
GET https://oz-web.com/apitesting/api.php?action=get

Body:
null

Result:
2021/02/09 10:11:14 - Success - doGet - 31 ms

Result Body:
{"id" = 5, "created" = "2020-10-23"}
```
Note: Result files are automatically overwritten

## CLI output
ATT provides information via standard & error out:
```
2021/02/09 10:19:21 loading tests
2021/02/09 10:19:21 running test 'doLogin'
2021/02/09 10:19:22 - Success - doLogin - 481 ms
2021/02/09 10:19:22 running test 'doGet'
2021/02/09 10:19:22 - Success - doGet - 54 ms
2021/02/09 10:19:22 running test 'doGetNoAuth'
2021/02/09 10:19:22 - Success - doGetNoAuth - 42 ms
2021/02/09 10:19:21 Finished Testing without failures
```
Note: The exit code equals zero if all tests ran without failures

When verbose mode is enabled, the same test case as used above would output:
```
2021/02/09 10:20:54 loading tests
     Loaded variable 'url'='oz-web.com/apitesting/api.php'
     Loaded variable 'password'='letmein'
     Loaded variable 'contains'='Logged in'
     Loaded extractVariableBody 'secret'='secret=([0-9]*)' in test case 'doLogin'
     Loaded extractVariableHeader 'xSessionHeader'='X-Session' in test case 'doLogin'
     Loaded Custom Header 'X-Custom-Header'='f00bar'
     Loaded Test Case 'doLogin' - https://oz-web.com/apitesting/api.php?action=login - POST - application/x-www-form-urlencoded - 200 - Logged in<DELIMITER>)
     Loaded Custom Header 'X-Session'='%%<xSessionHeader>%%'
     Loaded Test Case 'doGet' - https://oz-web.com/apitesting/api.php?action=get - GET - null - 200 - 2020-10-23<DELIMITER>)
     Loaded Test Case 'doGetNoAuth' - https://oz-web.com/apitesting/api.php?action=get - GET - null - 403 - null)
2021/02/09 10:20:54 running test 'doLogin'
     Doing HTTP POST - https://oz-web.com/apitesting/api.php?action=login with content type application/x-www-form-urlencoded
     Response Code = 200 - Response Body = secret=123456789 - Logged in
2021/02/09 10:20:55 - Success - doLogin - 485 ms
     Extracted Body Variable by Regex secret=([0-9]*) = 123456789
     Extracted Header Variable by Key X-Session = 123456789
     Written Test Result to D:\AAT\doLogin.txt
2021/02/09 10:20:55 running test 'doGet'
     Doing HTTP Get - https://oz-web.com/apitesting/api.php?action=get
     Response Code = 200 - Response Body = {"id" = 5, "created" = "2020-10-23"}
2021/02/09 10:20:55 - Success - doGet - 50 ms
     Written Test Result to D:\AAT\doGet.txt
2021/02/09 10:20:55 running test 'doGetNoAuth'
     Doing HTTP Get - https://oz-web.com/apitesting/api.php?action=get
     Response Code = 403 - Response Body = You are not logged in
2021/02/09 10:20:55 - Success - doGetNoAuth - 36 ms
     Written Test Result to D:\AAT\doGetNoAuth.txt
2021/02/09 10:20:54 Finished Testing without failures
```

## Defining test cases

### Minimal test case
A minimal test case requires the following values:
```json
{
   "tests":[
      {
         "name":"check200",
         "call":"https://zgheb.com",
         "method":"GET",
         "responseCode":200
      }
   ]
}
```
This test cases performs a HTTP GET on zgheb.com and checks if the response code is 200.


### Response contains
The following test case addtionally checks if the response body contains a string:
```json
{
   "tests":[
      {
         "name":"check200",
         "call":"https://zgheb.com",
         "method":"GET",
         "responseCode":200,
	 "responseContains": "automating"
      }
   ]
}
```
Note: You may also provide multiple contain keywords 
```json
         . . .
	 "responseContains": [
	     "automating", "security"
	 ]
	 . . .
```
### Request body
Request bodies are externalized in files, you may define them as following:
```json
      {
         "name":"doLogin",
         "call":"https://zgheb.com",
         "method":"POST",
         "responseCode":200,
         "timeout":500,
         "body":"bodies/loginbody",     <<<
	 . . . 
```
Note: The path defined in "body" is always relative to the location of the test json file.


### Headers
Setting headers is done as following - while setting the content type header has its own shortcut:
```json
         . . . 
         "headers":[
            {
               "X-Custom-Header":"f00bar"
            }
         ],
	 . . .
	 "contentType":"application/x-www-form-urlencoded",
	 . . . 
```

### Static variables
You may define a variable outside of the test cases as following:
```json
{
   "tests":[
      {
         "name":"check200",
         "call":"https://zgheb.com/%%<staticvar>%%",    <<<
         "method":"GET",
         "responseCode":200,
	 "responseContains": "automating"
      }
   ],
   "variables":[
      {
         "staticvar":"42"        <<<
      }
   ]
}
```
	
You may also declare variables for each test case:
```json
{
   "tests":[
      {
	 "call":"https://zgheb.com/%%<staticvar>%%",
	 . . . 
         "variables":[
            {
               "staticvar":"42"   <<<
            }
         ]
      },
        {
         "call":"https://zgheb.com/%%<staticvar>%%",
	 . . . 
         "variables":[
            {
               "staticvar":"43"   <<<
            }
         ]
      }
   ]
}
```
### Dynamic variables
You may dynamically set variables, meaning their value is extracted from a response.
Either from the variable body, using a regexp or from a response header.
This is extremely usefull to create test cases that need to be stateful.
```json
{
   "tests":[
      {
         "name":"doGet",
         "call":"https://zgheb.com",
         "method":"GET",
         "responseCode":200,
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
      }
   ]
}
```
### Using variables
Variables can be used under the following elements:
- call (url)
- request body file
- header
- responseContains

They can be injected by using %%<variable name>%%.


### Proxy
Configuring a proxy for all requests is as simple as:
```json
{
   "tests":[
      {
         "name":"doGet",
         "call":"https://zgheb.com",
         "method":"GET",
         "responseCode":200
      }
   ],
   "proxy":{
      "address": "127.0.0.1",
      "port": 5016
   }
}
```
### Timeout
When doing test cases, you may want to tweak the default timeout from 5000 ms to a specific value as such:
```json
      {
         "name":"longRunning",
         "call":"https://zgheb.com/longtask",
         "method":"GET",
         "responseCode":200,
         "timeout":10000,
         . . . 
```
	
### Externalizing tests
Being able to externalize tests into files is great for reoccuring tests.
```json
{
    "tests":[
        {
            "include":"post.json",
            "includeName":"doPost"
        },
	. . . 
``` 
  

## Mockup API
A mockup API is hosted under https://oz-web.com/apitesting/api.php which allows you to get acquianted with AAT.
The PHP code used is the following:
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

The following test json shows all features in a practical scenario using the mock API:
```json
{
   "tests":[
      {
         "name":"doLogin",
         "call":"https://%%<url>%%?action=login",
         "method":"POST",
         "responseCode":200,
         "timeout":500,
         "body":"af/loginbody",
         "contentType":"application/x-www-form-urlencoded",
         "responseContains":[
            "%%<contains>%%"
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
   ]
}
```
