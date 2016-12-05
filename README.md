# fuzzy_set_operations

## Compilation
The desktop application is built upon Java 8 and uses Maven as the build management tool.

First, you should install Java 8. After that download an install Maven as described in the below sections.
After you have successfully installed Maven, you can cd into the project's root directory and run Maven compilation.

```
$ cd path/to/fuzzy_set_operations
$ mvn compile
```

In order to build the project on your own and get an executable file, run Maven build.
```
$ mvn package
```

If you would run into error upon building the project, you may try to clean and recompile the project before building.
```
$ mvn clean
$ mvn compile
$ mvn package
```

The executable produced by the build and the compiled jar files should be found in the `target/` directory of the project.

### Installation

#### Mac OS X
Assuming that you have homebrew installed:
```
$ brew install maven
```

#### Ubuntu
To get all the available Maven packages and install Maven, run the following commands in your terminal:
```
$ apt-cache search maven
$ sudo apt-get install maven
```

#### Windows
To install Maven on Windows follow the [simple steps](https://www.mkyong.com/maven/how-to-install-maven-in-windows/) provided by Mkyong.

### Installation verification
After installing Maven, don't forget to verify your efforts.
```
$ mvn -version
```

## Usage

### Universe Definition
There are three ways to define the fuzzy sets' universe:

1. By enumeration: **1, 2, 3.14, 42**
2. By a range:

  - Either by a closed range: **1...3.14**
  - Or by a left-closed, right-open range: **1..10**
3. By a list of ranges: **10...1, 15...20, 25...18**

The ranges can be specified either in an increasing or a decreasing order. It does not even matter, if the ranges have common intervals. The application merges such ranges, i.e. **1..3, 2...4** would become **1...4**.

### Name of the sets
You can leave it blank, the name of the sets have a default value. However, if you decide to name your sets, there is only one rule to follow. The names of the respective set must differ.

### Membership functions
These field expect you to input a mathematical expression which is parsed by the tool [Parsii](https://github.com/scireum/parsii/). The project does not provide examples on how the expressions should look like. However, you can have a look at their [test files](https://github.com/scireum/parsii/blob/master/src/test/java/parsii/ParserTest.java) to read some of the expressions being tested.

The application expects you to provide exactly one variable in the member function, which can be represented by any string (except the reserved keywords such as *pi*, *euler*, *undefined*).

#### Examples of possible membership functions

- 2*x
- cos(x)
- abs(sin(x))
- if(x < 5, sin(x), cos(x))
- if(x < 5 || x > 8, pi, euler)
- tan(sqrt(euler ^ (pi * x)))
- min(0.7, x)
- max(0.3, x)

As you can see above, some functions can reach values greater than 1. Since the membership degree of the fuzzy elements should be in the range 0...1, the application normalizes the membership degrees in these cases.

A list of evaluable mathematical functions can be found at the [Function.java](https://github.com/scireum/parsii/blob/master/src/main/java/parsii/eval/Functions.java) file in the Parsii repository.

## A full example of the the application
Let the universe be **1...90** representing the age of a person.
Further on, let the fuzzy set A represent the **'young'** people.
And finally, define what being young really means **if(x <= 25, 1, 1/(1 + ((x - 25)/5)^3))**.

Hit the *Complement Button* and the result should look like the one in the following picture:

![Alt text](/doc/img/readme-example.png?raw=true "Full Example: What does it mean to be young in a context of fuzzy sets?")
