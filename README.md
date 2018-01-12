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

