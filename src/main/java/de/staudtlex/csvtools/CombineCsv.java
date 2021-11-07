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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  private static List<File> findFiles(String dirOrFilePath) throws IOException {
    String filePath = new File(dirOrFilePath).isDirectory()
        ? dirOrFilePath + "/*"
        : dirOrFilePath;
    Path path = Paths.get(filePath).toAbsolutePath();
    Path baseDir = path.getParent();
    String glob = path.getFileName().toString();
    List<File> fileList = new ArrayList<File>();
    try (
        DirectoryStream<Path> paths = Files.newDirectoryStream(baseDir, glob)) {
      for (Path e : paths) {
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
   * {@code "dupl"}, returns {@code ["a","a_dupl_1","b"]}.
   * 
   * @param stringList the list of (possibly duplicated) strings
   * @param suffix     the suffix to append to duplicated strings
   * @return the list of disambiguated strings
   * @see #getDistinct(List)
   */
  public static List<String> makeDistinct(List<String> stringList,
      String suffix) {
    if (stringList.size() < 2) {
      return stringList;
    }
    int n = stringList.size();
    List<String> uniqueStringList = new ArrayList<String>(n);
    HashMap<String, Integer> duplicateStrings = new HashMap<String, Integer>(n);
    for (String str : stringList) {
      Integer lookup = duplicateStrings.get(str);
      if (lookup == null) {
        // falls in duplicateStrings string str nicht enthalten ist, füge str zu
        // uniqueStringList hinzu
        uniqueStringList.add(str);
        duplicateStrings.put(str, 1);
      } else {
        // ansonsten (lookup != null, str in duplicateStrings enthalten),
        // füge str einen Unterstrich und den Wert von lookup hinzu (bspw. x,
        // x_1, x_2, x_anzahl-der-Duplikate)
        uniqueStringList.add(str + suffix + lookup);
        duplicateStrings.put(str, lookup + 1);
      }
    }
    return uniqueStringList;
  }

  /**
   * Creates a list of distinct strings by removing any duplicated list entries.
   * 
   * @param stringList list of (possibly duplicated) strings
   * @return list of distinct strings
   * @see #makeDistinct(List, String)
   */
  public static List<String> getDistinct(List<String> stringList) {
    if (stringList == null) {
      return null;
    }
    int n = stringList.size();
    List<String> uniqueStringList = new ArrayList<String>(n / 10);
    HashMap<String, Integer> duplicateStrings = new HashMap<String, Integer>(
        n / 10);
    for (String str : stringList) {
      Integer lookup = duplicateStrings.get(str);
      if (lookup == null) {
        // falls in duplicateStrings string str nicht enthalten ist, füge str zu
        // uniqueStringList hinzu
        uniqueStringList.add(str);
        duplicateStrings.put(str, 1);
      }
    }
    return uniqueStringList;
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
  public static List<String> readHeader(String path) {
    try {
      Reader csvFile = Files.newBufferedReader(Paths.get(path),
          StandardCharsets.UTF_8);

      final CSVFormat RawCsvFormat = CSVFormat.Builder.create().setHeader()
          .setSkipHeaderRecord(true).setDelimiter(';')
          .setTrailingDelimiter(true).setAllowMissingColumnNames(false)
          .setAllowDuplicateHeaderNames(true).setTrim(true).setQuote('"')
          .build();

      CSVParser rawCsvParser = new CSVParser(csvFile, RawCsvFormat);
      List<String> recordKeys = rawCsvParser.getHeaderNames();
      rawCsvParser.close();

      return recordKeys;
    } catch (IOException e) {
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
  public static List<CSVRecord> parseCsv(String path, List<String> keys) {
    try {
      Reader csvFile1 = Files.newBufferedReader(Paths.get(path),
          StandardCharsets.UTF_8);

      final CSVFormat myCsvFormat = CSVFormat.Builder.create()
          .setHeader(keys.toArray(new String[0])).setSkipHeaderRecord(true)
          .setDelimiter(';').setTrailingDelimiter(true)
          .setAllowMissingColumnNames(false).setAllowDuplicateHeaderNames(true)
          .setTrim(true).setQuote('"').build();

      CSVParser csvParser = new CSVParser(csvFile1, myCsvFormat);
      List<CSVRecord> records = csvParser.getRecords();
      csvParser.close();

      return records;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Rearranges a record based on a list of unique strings.
   * 
   * @param record the record to be rearranged
   * @param keys   the list of unique strings from which to construct the
   *                 rearranged record
   * @return the rearranged record as {@code Map<String, String>}
   */
  public static Map<String, String> rearrangeMap(Map<String, String> record,
      List<String> keys) {
    Map<String, String> rearrangedMap = keys.parallelStream()
        .collect(Collectors.toMap(e -> e, e -> ""));
    record.keySet().stream().forEach(e -> rearrangedMap.put(e, record.get(e)));
    return rearrangedMap;
  }

  /**
   * Rearranges all records of an ImportedCsvData-object based on a list of
   * unique strings.
   * 
   * @param csvData the {@link ImportedCsvData} whose records are to be
   *                  rearranged
   * @param keys    the list of unique strings from which to construct the
   *                  rearranged record
   * @return the {@link CsvData} containing the rearranged records
   * @see #rearrangeMap(Map, List)
   */
  public static CsvData rearrange(ImportedCsvData csvData, List<String> keys) {
    List<Map<String, String>> rearrangedRecords = csvData.getRecords()
        .parallelStream().map(e -> rearrangeMap(e, keys))
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
  public static CsvData merge(List<CsvData> csvDataList) {
    List<Map<String, String>> records = csvDataList.stream()
        .flatMap(e -> e.getRecords().stream()).collect(Collectors.toList());
    return new CsvData(records);
  }

  /**
   * A data object which contains records and record keys (column names).
   * Provides a method to format and print the records to {@code stdout}.
   */
  public static class CsvData {
    private List<String> keys;
    private List<Map<String, String>> records;

    /**
     * Creates a CsvData instance from the specified records.
     * 
     * @param records the list of records (as {@code List<Map<String, String>>})
     *                  from which to create the CsvData instance
     */
    public CsvData(List<Map<String, String>> records) {
      this.records = records;
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
    public List<Map<String, String>> getRecords() {
      return records;
    }

    /**
     * Formats records as semicolon-delimited text with newlines separating each
     * record. Returns a string suitable for printing to stdout.
     * 
     * @return the records' string-representation
     * @throws RuntimeException
     */
    public String formatRecords() {
      StringBuilder formattedRecords = new StringBuilder();

      final CSVFormat outformat = CSVFormat.Builder.create()
          .setHeader(keys.toArray(new String[0])).setDelimiter(';')
          .setAllowMissingColumnNames(false).setTrim(true).setQuote('"')
          .build();

      try {
        CSVPrinter printer = new CSVPrinter(formattedRecords, outformat);
        printer.printRecords(
            records.stream().map(e -> e.values()).collect(Collectors.toList()));
        printer.close();
      } catch (IOException e) {
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
    private File file;
    private String filePath;
    private String fileName;
    private List<String> keys;
    private List<Map<String, String>> records;

    /**
     * Creates an ImportedCsvData instance from a CSV file.
     * 
     * @param f the {@link File} from which to read the CSV records
     */
    public ImportedCsvData(File f) {
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
    public File getFile() {
      return file;
    }

    /**
     * @return the CSV source's absolute file path
     */
    public String getFilePath() {
      return filePath;
    }

    /**
     * @return the CSV source's file name, that is the last element of the
     *         absolute file path
     */
    public String getFileName() {
      return fileName;
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
    public List<Map<String, String>> getRecords() {
      return records;
    }

  }

  /**
   * Merges CSV files into a single file, when necessary disambiguating
   * duplicated column names and extending the number of columns. Results are
   * printed to {@code stdout}.
   * 
   * @param args the paths to the files that are to be merged. When used from
   *               the command line, globs are automatically expanded to an
   *               array of strings.
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Command requires at least one CSV file");
      return;
    }

    // (1) Get array of CSV files that are to be merged
    final File[] csvFileList = Stream.of(args)
        .map(e -> Paths.get(e).toAbsolutePath().toFile())
        .collect(Collectors.toList()).toArray(new File[0]);

    // Todo: check if specified files exist
    // Stream.of(csvFileList).filter(e -> Files.exists(e.toPath()));

    // (2) Import CSV data from files
    final List<ImportedCsvData> csvData = Stream.of(csvFileList).parallel()
        .map(ImportedCsvData::new).collect(Collectors.toList());

    // (3) Merge data from each CSV file
    final List<String> keys = csvData.stream().map(e -> e.getKeys())
        .flatMap(e -> e.stream()).collect(Collectors.toList());
    final List<String> distinctKeys = getDistinct(keys);

    // - rearrange CSV records according to distinct keys
    final List<CsvData> rearrangedCsvData = csvData.stream()
        .map(e -> rearrange(e, distinctKeys)).collect(Collectors.toList());

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
