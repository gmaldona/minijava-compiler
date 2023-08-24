# <span> A compiler for MiniJava <img src="https://img.icons8.com/color/48/000000/java-coffee-cup-logo--v1.png"/></span>
MiniJava is a subset of the Java language

A few examples of the MiniJava language can be found <a href="https://www.cambridge.org/resources/052182060X/">here</a>. <br>
The grammar used in this project comes from the grammar found on <a href="http://www.cs.tufts.edu/~sguyer/classes/comp181-2006/minijava.html">here</a>. <br>

## Building The minijava.lang.MiniJava Compiler

### Build by Installing Python Tool Chain
- The Antlr 4 Tools can be installed using python pip install. The following PyPi packages include the necessary tools to
compile a *.g4 file and generate the required Java classes:
    * antlr4-python3-runtime version 4.12.0
    * antlr4-tools version 0.1

Convenience scripts for building a virtual environment and downloading the necessary PyPi packages can
be found within the `scripts/` directory.

####  Steps:
1. Navigate into the scripts directory and run the following commands
    ```shell 
    cd scripts && chmod +x build_env.python.sh compile_grammar.python.sh
    ````
   ```shell
   ./build_env.python.sh 
    ```
   ```shell
   ./compile_grammar.python.sh 
    ```
2. Ensure Antlr4 Tools generated the correct files needed for the minijava.lang.MiniJava Compiler. A validation script
can be found within the `scripts/` directory.
   ```shell
    cd scripts && chmod +x validate.sh 
    ```
   ```shell
    ./validate.sh
   ```

### Build Using Docker

---
#### Historical Note: This is a post-graduation attempt of re-writing the MiniJava Compiler.
The original implementation was writen in Scala and can be found on [GitHub](https://github.com/gmaldona/minijava).
This newer and improved compiler implementation started as a side project for learning new Java 17 techniques.