# combine-csv

_combine-csv_ is a Java tool to combine CSV-formatted files into a single file. CSV files need not have the same number of columns, column names need not be unique.

## Dependencies

- OpenJDK (version 8 or higher)
- Maven (or GNU make)
- Apache Commons CSV (see [https://commons.apache.org/proper/commons-csv/](https://commons.apache.org/proper/commons-csv/))
- Apache Commons CLI (see [https://commons.apache.org/proper/commons-cli/](https://commons.apache.org/proper/commons-cli/))
- JUnit ConsoleLauncher (required for testing only; see [https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher](https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher))

## Build and test

Create regular, executable jar:

- `mvn package` (or `make jar`)

Alternatively, create an executable uber-jar (which also contains its runtime dependencies):

- `mvn package assembly:single` (or `make uber-jar`)

Test the package:

- `mvn test` (or `make test`)

Generate the package information and class documentation:

- `mvn site` (for more information, see the [Maven site-plugin](https://maven.apache.org/plugins/maven-site-plugin/usage.html) and the [Maven javadoc-plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/usage.html))

The documentation is located in `./target/site/index.html` in the project directory.

## Usage

```sh
usage: combineCsv [-h] [-r <custom-order>] <file-1 file-2 ...>

Options:
 -h,--help            Display this help message
 -r,--reorder <arg>   Reorder columns according to comma-separated list of
                      column names
```

`combine-csv-1.2.0.jar` takes as arguments at least two CSV files that are to be combined. The result is printed to `stdout`. Users may optionally provide a comma-separated string to reorder the column names via the `-r` option (also see [column ordering](#column-ordering)).

Let us assume `combine-csv-1.2.0.jar` is located in the working directory, and the CSV files are located in `./csv-dir`. 

Depending on whether we created a regular or an uber-jar, the commands for merging CSV files may slightly differ.

Using the regular jar, run
```sh
java -cp path/to/commons-csv.jar:path/to/commons-cli.jar/combine-csv-1.2.0.jar de.staudtlex.csvtools.CombineCsv csv-dir/*.csv > results.csv
```

With the uber-jar, run
```sh
java -jar combine-csv-1.2.0.jar csv-dir/*.csv > results.csv
``` 

Depending on the size and number of CSV files, this process may take some time. 

## Column ordering
By default, _combine-csv_ orders column names according to their order of appearance in the first file in which they present:

- Given two files with column names `a,b` and `b,a`, the resulting order will be `a,b`

- Given three files with column names `a,b`, `y,x`, and `d,e,x,y`, the resulting order will be `a,b,y,x,d,e`

Assuming we want the resulting CSV to have columns to be ordered alphabetically, call _combine-csv_ with the `-r` option and provide the required column ordering

```sh
java -jar combine-csv-1.2.0.jar -r a,b,d,e,x,y csv-dir/*.csv > results.csv
``` 