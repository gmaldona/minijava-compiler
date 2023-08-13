# MiniJava Compiler

## Building The MiniJava Compiler

---
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
    cd scripts && chmod +x build_venv.sh compile_grammar.sh
    ````
   ```shell
   ./build_venv.sh 
    ```
   ```shell
   ./compile_grammar.sh 
    ```
2. Ensure Antlr4 Tools generated the correct files needed for the MiniJava Compiler. A validation script
can be found within the `scripts/` directory.
   ```shell
    cd scripts && chmod +x validate.sh 
    ```
   ```shell
    ./validate.sh
   ```

### Build Using Docker
