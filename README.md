# combine-csv

_combine-csv_ is a Java tool to combine CSV-formatted files into a single file. CSV files need not have the same number of columns, column names need not be unique.

## Dependencies

- Java SDK (Java 8 or higher)
- Maven
- Apache Commons CSV (see [https://commons.apache.org/proper/commons-csv/](https://commons.apache.org/proper/commons-csv/))

## Build

Create a self-contained executable jar (contains compile and runtime dependencies):

- `mvn package assembly:single`

Generate the package information and class documentation:

- `mvn site` (for more information, see the [Maven site-plugin](https://maven.apache.org/plugins/maven-site-plugin/usage.html) and the [Maven javadoc-plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/usage.html))

The documentation is located in `./target/site/index.html` in the project directory.

## Usage

`combine-csv-x.x.jar` takes as arguments the paths of the CSV files that are to be combined. The result is printed to `stdout`.

Assuming `combine-csv-x.x.jar` is located in the working directory, and the CSV files are located in `./csv-dir`, you can merge the data and write the results to `results.csv` as follows: 
```sh
java -jar combine-csv-1.0.jar csv-dir/*.csv > results.csv
``` 

Depending on the size and number of CSV files, this process may take some time. 