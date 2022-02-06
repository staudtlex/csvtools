/*
 * Copyright (C) 2021 Alexander Staudt
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.staudtlex.csvtools;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * Provides methods to read and merge CSV files into a single file.
 * <p>
 * CombineCsv automatically creates unique column names from the CSV header. All
 * CSV files need not have the same number of columns.
 * <p>
 * Note: This class expects a semicolon ({@code ;}) as column delimiter and
 * quoted fields to be enclosed in double quotes ({@code ""}).
 */
public class CombineCsv {

  /**
   * Returns a list of files found in {@code dirOrFilePath}.
   * 
   * @param dirOrFilePath the directory or file path pointing to the files to be
   *                        listed
   * @return the list of files
   * @throws IOException if an I/O error occurs
   */
  public static List<File> findFiles(final String dirOrFilePath)
      throws IOException {
    final String filePath = new File(dirOrFilePath).isDirectory()
        ? dirOrFilePath + "/*"
        : dirOrFilePath;
    final Path path = Paths.get(filePath).toAbsolutePath();
    final Path baseDir = path.getParent();
    final String glob = path.getFileName().toString();
    final List<File> fileList = new ArrayList<File>();
    try (
        DirectoryStream<Path> paths = Files.newDirectoryStream(baseDir, glob)) {
      for (final Path e : paths) {
        if (e.toFile().isFile()) {
          fileList.add(e.toFile());
        }
      }
    }
    return fileList;
  }

  /**
   * Disambiguates duplicated list entries.
   * <p>
   * For example, given a string list {@code ["a","a","b"]} and suffix
   * {@code "dupl"}, returns sorted set containing {@code ["a","a_dupl_1","b"]}.
   * 
   * @param stringList the list of (possibly duplicated) strings
   * @param suffix     the suffix to append to duplicated strings
   * @return the sorted set of disambiguated strings
   * @see #getDistinct(List)
   */
  public static LinkedHashSet<String> makeDistinct(
      final List<String> stringList, final String suffix) {
    if (stringList.size() < 2) {
      return new LinkedHashSet<String>(stringList);
    }
    final int n = stringList.size();
    final LinkedHashSet<String> uniqueStringSet = new LinkedHashSet<>(n);
    final HashMap<String, Integer> duplicateStrings = new HashMap<String, Integer>(
        n);
    for (final String str : stringList) {
      final Integer lookup = duplicateStrings.get(str);
      if (lookup == null) {
        // if str is not contained in duplicateStrings, add str to
        // uniqueStringSet
        uniqueStringSet.add(str);
        duplicateStrings.put(str, 1);
      } else {
        // else (lookup != null, duplicateStrings contains str), append suffix
        // and value of lookup to str to make it unique (e.g. x, x_1, x_2,
        // x_number-of-duplicates)
        uniqueStringSet.add(str + suffix + lookup);
        duplicateStrings.put(str, lookup + 1);
      }
    }
    return uniqueStringSet;
  }

  /**
   * Creates a set of distinct strings, removing any duplicated list entries.
   * 
   * @param stringList list of (possibly duplicated) strings
   * @return set of distinct strings. Set elements appear in the same order as
   *         in the input list (save for removed duplicates)
   * @see #makeDistinct(List, String)
   */
  public static LinkedHashSet<String> getDistinct(
      final List<String> stringList) {
    if (stringList == null) {
      return null;
    }
    final int n = stringList.size();
    final LinkedHashSet<String> uniqueStringSet = new LinkedHashSet<>(n / 10);
    for (final String str : stringList) {
      uniqueStringSet.add(str);
    }
    return uniqueStringSet;
  }

  /**
   * Create a set of alphabetially ordered strings.
   * 
   * @param set list of unique strings
   * @return an alphabetially sorted set of distinct strings
   */
  public static LinkedHashSet<String> sort(final LinkedHashSet<String> set) {
    final ArrayList<String> sortList = new ArrayList<>(set);
    Collections.sort(sortList);
    return new LinkedHashSet<String>(sortList);
  }

  /**
   * Reads the header of a CSV file.
   * 
   * @param path to the CSV file
   * @return list of CSV record keys
   * @throws RuntimeException if an I/O error occurs opening the file under
   *                            {@code path}.
   * @throws RuntimeException if there is a problem reading the header.
   */
  public static List<String> readHeader(final String path) {
    try {
      final Reader csvFile = Files.newBufferedReader(Paths.get(path),
          StandardCharsets.UTF_8);

      final CSVFormat RawCsvFormat = CSVFormat.Builder.create().setHeader()
          .setSkipHeaderRecord(true).setDelimiter(';')
          .setTrailingDelimiter(true).setAllowMissingColumnNames(false)
          .setAllowDuplicateHeaderNames(true).setTrim(true).setQuote('"')
          .build();

      final CSVParser rawCsvParser = new CSVParser(csvFile, RawCsvFormat);
      final List<String> recordKeys = rawCsvParser.getHeaderNames();
      rawCsvParser.close();

      return recordKeys;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parses the contents of a CSV file.
   * 
   * @param path to the CSV file
   * @param keys the keys of the CSV records
   * @return list of CSV
   * @throws RuntimeException if an I/O error occurs opening the file under
   *                            {@code path}.
   * @throws RuntimeException if there is a problem reading the header (keys).
   */
  public static List<CSVRecord> parseCsv(final String path,
      final LinkedHashSet<String> keys) {
    try {
      final Reader csvFile1 = Files.newBufferedReader(Paths.get(path),
          StandardCharsets.UTF_8);

      final CSVFormat myCsvFormat = CSVFormat.Builder.create()
          .setHeader(keys.toArray(new String[0])).setSkipHeaderRecord(true)
          .setDelimiter(';').setTrailingDelimiter(true)
          .setAllowMissingColumnNames(false).setAllowDuplicateHeaderNames(true)
          .setTrim(true).setQuote('"').build();

      final CSVParser csvParser = new CSVParser(csvFile1, myCsvFormat);
      final List<CSVRecord> records = csvParser.getRecords();
      csvParser.close();

      return records;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Rearranges a record based on a set of unique strings.
   * 
   * @param record the record to be rearranged
   * @param keys   the set of unique strings from which to construct the
   *                 rearranged record
   * @return the rearranged record as {@code Map<String, String>}
   */
  public static LinkedHashMap<String, String> rearrangeMap(
      final Map<String, String> record, final LinkedHashSet<String> keys) {
    final LinkedHashMap<String, String> rearrangedMap = keys.parallelStream()
        .collect(Collectors.toMap(e -> e, e -> "", (o1, o2) -> o1,
            LinkedHashMap::new));
    record.keySet().stream().forEach(e -> rearrangedMap.put(e, record.get(e)));
    return rearrangedMap;
  }

  /**
   * Rearranges all records of an ImportedCsvData-object based on a set of
   * unique strings.
   * 
   * @param csvData the {@link ImportedCsvData} whose records are to be
   *                  rearranged
   * @param keys    the list of unique strings from which to construct the
   *                  rearranged record
   * @return the {@link CsvData} containing the rearranged records
   * @see #rearrangeMap(Map, LinkedHashSet)
   */
  public static CsvData rearrange(final ImportedCsvData csvData,
      final LinkedHashSet<String> keys) {
    final List<LinkedHashMap<String, String>> rearrangedRecords = csvData
        .getRecords().parallelStream().map(e -> rearrangeMap(e, keys))
        .collect(Collectors.toList());
    return new CsvData(rearrangedRecords);
  }

  /**
   * Merge records of a list of CsvData objects into a single instance of
   * CsvData.
   * 
   * @param csvDataList the list of {@link CsvData} whose records are to be
   *                      merged
   * @return the CsvData containing the merged records
   */
  public static CsvData merge(final List<CsvData> csvDataList) {
    final List<LinkedHashMap<String, String>> records = csvDataList.stream()
        .flatMap(e -> e.getRecords().stream()).collect(Collectors.toList());
    return new CsvData(records);
  }

  /**
   * A data object which contains records and record keys (column names).
   * Provides a method to format and print the records to {@code stdout}.
   */
  public static class CsvData {
    private final List<String> keys;
    private final List<LinkedHashMap<String, String>> records;

    /**
     * Creates a CsvData instance from the specified records.
     * 
     * @param records the list of records (as
     *                  {@code List<LinkedHashMap<String, String>>}) from which
     *                  to create the CsvData instance
     * @throws IllegalArgumentException when the list passed to the constructor
     *                                    is empty
     */
    public CsvData(final List<LinkedHashMap<String, String>> records)
        throws IllegalArgumentException {
      this.records = records;
      if (records.isEmpty()) {
        throw new IllegalArgumentException("Record is empty");
      }
      this.keys = new ArrayList<>(records.get(0).keySet());
    }

    /**
     * @return the records' keys
     */
    public List<String> getKeys() {
      return keys;
    }

    /**
     * @return the CsvData instance's records
     */
    public List<LinkedHashMap<String, String>> getRecords() {
      return records;
    }

    /**
     * Formats records as semicolon-delimited text with newlines separating each
     * record. Returns a string suitable for printing to stdout.
     * 
     * @return the records' string-representation
     * @throws RuntimeException when an error occurs while formatting the CSV
     *                            records
     */
    public String formatRecords() {
      final StringBuilder formattedRecords = new StringBuilder();

      final CSVFormat outformat = CSVFormat.Builder.create()
          .setHeader(keys.toArray(new String[0])).setDelimiter(';')
          .setAllowMissingColumnNames(false).setTrim(true).setQuote('"')
          .build();

      try {
        final CSVPrinter printer = new CSVPrinter(formattedRecords, outformat);
        printer.printRecords(
            records.stream().map(e -> e.values()).collect(Collectors.toList()));
        printer.close();
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
      return formattedRecords.toString();
    }

  }

  /**
   * A data object which contains records and records keys (column names).
   * Instances of {@link ImportedCsvData} directly parse CSV files upon
   * instantiation.
   */
  public static class ImportedCsvData {
    private final File file;
    private final String filePath;
    private final String fileName;
    private final LinkedHashSet<String> keys;
    private final List<Map<String, String>> records;

    /**
     * Creates an ImportedCsvData instance from a CSV file.
     * 
     * @param f the {@link File} from which to read the CSV records
     */
    public ImportedCsvData(final File f) {
      this.file = f;
      this.filePath = f.getAbsolutePath();
      this.fileName = f.getName();
      this.keys = makeDistinct(readHeader(this.filePath), "__duplicated_");
      this.records = parseCsv(this.filePath, this.keys).stream()
          .map(CSVRecord::toMap).collect(Collectors.toList());
    }

    /**
     * @return the {@code File} used for instantiation
     */
    public final File getFile() {
      return file;
    }

    /**
     * @return the CSV source's absolute file path
     */
    public final String getFilePath() {
      return filePath;
    }

    /**
     * @return the CSV source's file name, that is the last element of the
     *         absolute file path
     */
    public final String getFileName() {
      return fileName;
    }

    /**
     * @return the records' keys
     */
    public final LinkedHashSet<String> getKeys() {
      return keys;
    }

    /**
     * @return the CsvData instance's records
     */
    public final List<Map<String, String>> getRecords() {
      return records;
    }

  }

  /**
   * Merges CSV files into a single file, when necessary disambiguating
   * duplicated column names and extending the number of columns. Results are
   * printed to {@code stdout}.
   * 
   * @param args the paths to the files that are to be merged as well as options
   *               and option arguments. When used from the command line, globs
   *               are automatically expanded to an array of strings.
   */
  public static void main(String[] args) {
    // Define options
    final Options options = new Options();
    options.addOption("r", "reorder", true,
        "Reorder columns according to a comma-separated list of column names. Duplicated column names as well as column names not present in the input files will be ignored");
    options.addOption("h", "help", false, "Display this help message");

    // Define help
    final HelpFormatter formatter = new HelpFormatter();
    final String cmdLineSyntax = "combineCsv [-h] [-r <custom-order>] <file-1 file-2 ...>";
    final String header = "\nOptions:";
    final String footer = "";

    // Parse named options and remaining positional arguments
    final CommandLineParser parser = new DefaultParser();
    String customOrder = "";
    Boolean providesCustomOrder = false;
    Boolean requiresHelp = false;
    try {
      final CommandLine cmd = parser.parse(options, args);
      providesCustomOrder = cmd.hasOption("r");
      requiresHelp = cmd.hasOption("h");
      if (requiresHelp) {
        formatter.printHelp(cmdLineSyntax, header, options, footer);
        System.exit(0);
      } else if (providesCustomOrder) {
        customOrder = cmd.getOptionValue("r");
      }
      args = cmd.getArgs();
    } catch (final ParseException e) {
      System.err.println(
          "Unable to parse command line options: " + e.getMessage() + "\n");
      formatter.printHelp(cmdLineSyntax, header, options, footer);
      System.exit(1);
    }
    if (args.length < 1) {
      System.err.println("Command requires at least one CSV file\n");
      formatter.printHelp(cmdLineSyntax, header, options, footer);
      System.exit(1);
    }

    // (1) Get array of CSV file names that are to be merged. Exit and check if
    // specified files exist
    final File[] csvFileList = Stream.of(args)
        .map(e -> Paths.get(e).toAbsolutePath().toFile())
        .collect(Collectors.toList()).toArray(new File[0]);

    final File[] missingFiles = Stream.of(csvFileList)
        .filter(e -> !Files.exists(e.toPath())).collect(Collectors.toList())
        .toArray(new File[0]);
    if (missingFiles.length > 0) {
      final String message = "The following files were not found: ";
      System.err.println(message + Stream.of(missingFiles).map(e -> e.getName())
          .reduce("", (e1, e2) -> e1 + e2) + "\n");
      formatter.printHelp(cmdLineSyntax, header, options, footer);
      System.exit(1);
    }

    // (2) Import CSV data from files
    final List<ImportedCsvData> csvData = Stream.of(csvFileList).parallel()
        .map(ImportedCsvData::new).collect(Collectors.toList());

    // (3) Merge data from each CSV file
    final List<String> keys = csvData.stream().map(e -> e.getKeys())
        .flatMap(e -> e.stream()).collect(Collectors.toList());
    final LinkedHashSet<String> distinctKeys = getDistinct(keys);

    // - rearrange CSV records according to distinct keys
    final LinkedHashSet<String> keyOrderSet;
    if (providesCustomOrder) {
      keyOrderSet = Stream.of(customOrder.split(",", -1))
          .filter(e -> distinctKeys.contains(e)) // ignore possibly non-existing
                                                 // columns passed in with
                                                 // option -r
          .collect(Collectors.toCollection(LinkedHashSet::new));
      keyOrderSet.addAll(distinctKeys);
    } else {
      keyOrderSet = distinctKeys;
    }

    final List<CsvData> rearrangedCsvData = csvData.stream()
        .map(e -> rearrange(e, keyOrderSet)).collect(Collectors.toList());

    // - collect rearranged CSV records in single CsvData
    final CsvData mergedCsvData = merge(rearrangedCsvData);

    // rearrangedCsvData.forEach(e -> System.out.println(e.getKeys()));
    // System.out.println();
    // System.out.println(mergedCsvData.getKeys());

    // (4) Print merged CSV records to stdout
    final String formattedResults = mergedCsvData.formatRecords();
    System.out.print(formattedResults);

  }

}
