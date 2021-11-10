# combine-csv

_combine-csv_ is a Java tool to combine CSV-formatted files into a single file. CSV files need not have the same number of columns, column names need not be unique.

## Dependencies

- Java OpenJDK (version 8 or higher)
- Maven (or GNU make)
- Apache Commons CSV (see [https://commons.apache.org/proper/commons-csv/](https://commons.apache.org/proper/commons-csv/))

## Build

Create regular, executable jar:

- `mvn package` (or `make jar`)

Alternatively, create an executable uber-jar (which also contains its runtime dependencies):

- `mvn package assembly:single` (or `make uber-jar`)

Generate the package information and class documentation:

- `mvn site` (for more information, see the [Maven site-plugin](https://maven.apache.org/plugins/maven-site-plugin/usage.html) and the [Maven javadoc-plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/usage.html))

The documentation is located in `./target/site/index.html` in the project directory.

## Usage

`combine-csv-1.0.0.jar` takes as arguments the paths of the CSV files that are to be combined. The result is printed to `stdout`.

Let us assume `combine-csv-1.0.0.jar` is located in the working directory, and the CSV files are located in `./csv-dir`. 

Depending on whether we created a regular or an uber-jar, the commands for merging CSV files may slightly differ.

Using the regular jar, run
```sh
java -cp path/to/commons-csv.jar:combine-csv-1.0.0.jar de.staudtlex.csvtools.CombineCsv csv-dir/*.csv > results.csv
```

With the uber-jar, run
```sh
java -jar combine-csv-1.0.0.jar csv-dir/*.csv > results.csv
``` 

Depending on the size and number of CSV files, this process may take some time. 