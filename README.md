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
         "name":"doGet",
         "call":"https://%%<url>%%",
         "method":"GET",
         "responseCode":200,
         "responseContains":[
            "GET",
            "secret"
         ],
         "extractVariableBody":[
            {
               "secret":"secret=([0-9]*)"
            }
         ],
         "extractVariableHeader":[
            {
               "xampleHeader":"X-Example"
            }
         ]
      },
      {
         "include":"post.json",
         "includeName":"doPost"
      }
   ],
   "variables":[
      {
         "url":"oz-web.com/apitesting/api.php"
      }
   ]
}
```
Include
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
Body
```
postbody! %%<secret>%% %%<xampleHeader>%%
```
