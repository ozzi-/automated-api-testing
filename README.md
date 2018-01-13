# automated-api-testing
Set up API / web test cases with ease!

# Usage
## Defining test cases
tests.json is the main file defining the test cases. 
There are two ways of adding a test case:
- Inline , adding the necessary fields
```
{
  . . . 
  "tests": [
    {
      "name": "createIdentity",
      "call": "%%<w_base>%%createIdentity/",
      "method": "POST",
      "responsecode": 200,
      "body": "createIdentity.json",
      "contenttype": "application/json",
      "responsecontains": [
      "initialPassword","IdNumber"
    }
  ]
  . . . 
}
```
- Include , including the necessary fields by referencing another json file
```
{
  . . . 
  "tests": [
    {
      "include": "createIdentity.json",
      "includeName": "createIdentity",
    }
  ]
  . . . 
}
```
Note: the base path for includes is "src/resources/"

## Checking the result
The result (as in the response body) can be checked by the response code and by defining strings that need to be contained in the response. 
The following test case will only succeed if a response code of 200 (OK) is returned and the body contains "initialPassword=testpwd"  and "<a span="id">12345</span>"
```
{
	"name": "createIdentity",
	"call": "%%<w_base>%%createIdentity/",
	"method": "POST",
	"responsecode": 200,
	"body": "createIdentity.json",
	"contenttype": "application/json",
	"responsecontains": [
		"initialPassword=testpwd","<a span=\"id\">12345</span>"
	]
}
```

## Variables
In order to help automate the testing, you can define variables

### Global variables
Global variables can be defined in tests.json, they are available in all test cases.
However they still may be overwritten.
```
{
  . . .
  "variables": [
  		{
  			"w_base": "http://192.168.100.100:8080/rest/",
  		 	"n_base": "http://idm.example.com/admin/"
      }
   ],
   . . . 
}
```
### Static variables
Static variables are valid from the test case where they are defined until the end of the testing.
They also may be overwritten later on in the testing.

Static variables can be defined as such in a include test case assignment:
```
"staticVariables" : [{
  "c_createIdentity_LoginID": "testl002"
},{
  "c_createIdentity_Email": "erika.mueller2@example.com"
}]
```
Static variaibles can also be defined in the test case itself as such:
```
{
	"name": "createIdentity",
	"call": "%%<w_base>%%createIdentity/",
  . . . 
	"variables" : [{
    "c_createIdentity_LoginID": "%%static%%testl002"
  },{
    "c_createIdentity_Email": "%%static%%erika.mueller2@example.com"
	}]
}
```
Note: The keyword %%static%% is needed here.

### Dynamic variables
Dynamic variables are set depending on the response body using regular expressions. 
This allows complex testing where a subsequent test case is depending on a result of a prior test case.
This example will get the json value of "idNumber" and "initialPassword" from the response body:
```
{
	"name": "createIdentity",
	"call": "%%<w_base>%%createIdentity/",
	"method": "POST",
	"responsecode": 200,
  	. . .
 	"variables" : [{
		"c_IdNumber": "\"IdNumber\":\"(.*?)\"" 
	},{
		"c_InitialPassword": "\"initialPassword\":\"(.*?)\"" 
	}]
}
```
### Variable usage
Variables may be used in all fields available, in the test case definitions as well as the defined bodies.
The syntax is as following: %%<VARIABLE_NAME>%%
Example in the body:
```

{
 	"loginId": "%%<c_createIdentity_LoginID>%%",
	"email": "%%<c_createIdentity_Email>%%",
	"language": "DE",
	"name": "Erich",
	"birthdate": "01.11.1992",
	"sex": "m",
	"country": "CH"
}
```
Example in the test case:
```
{
	"name": "createIdentity",
	"call": "%%<w_base>%%createIdentity/%%<c_createIdentity_LoginID>%%",
	. . .
}
```
Note: When trying to access an undefined variable, an according error message is displayed.

## Result reporting
Next to the direct feedback over terminal output, a result file is created.
The call including a success / error message (with reason), the body and the response will be written into files in "/results/TEST_CASE_NAME.txt".
Example:
```
Method - Call:
POST http://192.168.100.100:8080/rest/createIdentity/

Body:
{
	"login": "testl001",
	. . .
	"more": "json"
}

Result:
2018/01/12 14:50:41 - Failure: Does not contain 'initialPassword' - createIdentity - 381 ms

Result Body:
<!DOCTYPE html><html><head><title>Apache Tomcat - Error report</title>
. . .
<h1>HTTP Status 503 - </h1><div class="line"></div><p><b>type</b> Status report</p><p><b>message</b> <u></u></p><p><b>description</b> <u>The requested service is not currently available.
. . .
</html>

```

