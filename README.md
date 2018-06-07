carmina
====

# What is this?

'carmina' is a small JVM library coded in Groovy, that implements Test Results Repository.


Carmina help you to resolve a file path for a screenshot in the following format:

```
${base directory}/${test suite name}/${test suite timestamp}/${test case name}/${prefix}/${material file name}
```

For example:
```
.Results/TS1/20180809_091047/1/https%3A%2F%2Fwww.google.com%2F.png
```

You can use carmina to save any material files (PNG, JPEG, JSON, XML, PDF, HTML, CSV etc.) which
you obtained during testing with WebDriver in JUnit, Spock and Katalon Studio.

Carmina requires Java8+


