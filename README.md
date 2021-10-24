# Product Recommendation System
A product recommendation system implemented with Apache Spark, and Scala. _sbt_ is used for managing builds and dependencies. The data is available in "_data/products.json_" in which every line represents a product. For example:
```
{"sku":"sku-12","attributes":{"att-a":"att-a-10","att-b":"att-b-12","att-c":"att-c-14","att-d":"att-d-1","att-e":"att-e-13","att-f":"att-f-14","att-g":"att-g-9","att-h":"att-h-9","att-i":"att-i-15","att-j":"att-j-12"}}
```
There are 20k products data available in the file. Each product has a list of 10 attributes and an identifier _"sku"_. 

The **objective** is to find the 10 most similar products for a given product and write their _"sku"_ and respective weights to the output file.
The **key criteria** for similarity between products is the number of attributes that they have in common with each other. In the case of a tie, the attributes are valued in the alphabetic order. Meaning that:
```
{"sku":"sku-1","attributes": {"att-a": "a1", "att-b": "b1", "att-c": "c1"}} is more similar to
{"sku":"sku-2","attributes": {"att-a": "a2", "att-b": "b1", "att-c": "c1"}} than to (W=0.67)
{"sku":"sku-3","attributes": {"att-a": "a1", "att-b": "b3", "att-c": "c3"}}         (W=0.33)
```
```
{"sku":"sku-1","attributes":{"att-a": "a1", "att-b": "b1"}} is more similar to
{"sku":"sku-2","attributes":{"att-a": "a1", "att-b": "b2"}} than to (W=0.5)
{"sku":"sku-3","attributes":{"att-a": "a2", "att-b": "b1"}}         (W=0.5)
```
The expected format of the output is similar to the input and can be found in _"expected/recommendations.json"_. This file is also used as a test case.

## How to Run
Make sure you have [*sbt*](https://www.scala-sbt.org/1.x/docs/Setup.html) installed. Navigate to the root of the project (you should see a *build.sbt* in the directory). Then, to run the program:
```bash
$ sbt "runMain Recommendation -p sku-1234"
```
This command will read the data from _"data/products.json"_, and run the recommendation system for the product with the identifier of _"sku-1234"_, then write the output to the stdout and a file _"output/recommendations.json"_. These can be configured with command line arguments. Run the following command to get information about the options:
```bash
$ sbt "runMain Recommendation --help"
```
```
 usage: $ sbt "runMain Recommendation [options]"
        where the options are the following:
        -h | --help  Show this message and quit.
        -i | --in  | --inpath  path   The input file path (default: data/products.json)
        -o | --out | --outpath path   The directory to which the output will be written (default: output)
        -p | --product SKU   The SKU of the product that the app makes recommendations for.
                            "SKU" could be one of:
                            ("sku-1", "sku-2", ... ,"sku-19999", "sku-20000").
        -q | --quiet         Suppress some informational output.
```
## Tests
This project uses the **scalatest** library to test the program. The chosen style of test is the FunSuite. I have tried to test different edge cases regarding I/O and recommendations results.
To run the tests simply run:
```bash
$ sbt test
```
### Possible Further Improvements
 - Add caching for results 
 - Better test cases
 - Wrapping a REST/gRPC API around the engine 
