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
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * Use of the tests defined below with the JUnit library is explicitly
 * permitted.
 */
package de.staudtlex.csvtools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CombineCsvTest {
  final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  final PrintStream originalOut = System.out;
  final PrintStream originalErr = System.err;

  @BeforeEach
  private void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @AfterEach
  private void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  void testGetDistinct() {
    // strings
    final String[] withoutDupl = {
        "Neque", "porro", "quisquam", "est,", "qui", "dolorem", "ipsum,",
        "quia", "dolor", "sit,", "amet,", "consectetur", "adipisci",
        "velit [...]."
    };
    final String[] withDupl = {
        "Neque", "porro", "quisquam", "Neque", "quisquam", "porro", "est,",
        "qui", "qui", "dolorem", "ipsum,", "quia", "dolor", "sit,", "amet,",
        "consectetur", "adipisci", "quisquam", "velit [...].", "ipsum,"
    };

    // inputs lists
    final List<String> listWithoutDupl = Arrays.asList(withoutDupl);
    final List<String> listWithDupl = Arrays.asList(withDupl);
    final List<String> singletonList = Arrays.asList("Neque");
    final List<String> emptyList = new ArrayList<String>();

    // expected results
    final LinkedHashSet<String> refListWithoutDupl = new LinkedHashSet<>(
        Arrays.asList(withoutDupl));
    final LinkedHashSet<String> refListWithDuplMadeUnique = new LinkedHashSet<>(
        Arrays.asList(withoutDupl));
    final LinkedHashSet<String> refSingletonList = new LinkedHashSet<>(
        Arrays.asList("Neque"));
    final LinkedHashSet<String> refEmptyList = new LinkedHashSet<>();

    // run tests
    assertEquals(refListWithoutDupl, CombineCsv.getDistinct(listWithoutDupl));
    assertEquals(refListWithDuplMadeUnique,
        CombineCsv.getDistinct(listWithDupl));
    assertEquals(refSingletonList, CombineCsv.getDistinct(singletonList));
    assertEquals(refEmptyList, CombineCsv.getDistinct(emptyList));
  }

  // Test CombineCsv.main()
  // Todo:
  // - csv format does not correspond to expectation (delimiter is not ;)
  // - more columns in header than body
  // - merging large csv files

  @Test
  void testMainWithUniqueColumnNames() {
    try {
      final String resourcePath = "src/test/resources/csv/";
      final String refFile = resourcePath + "reference-data/gss-append.csv";
      final BufferedReader reader = new BufferedReader(new FileReader(refFile));
      final String[] testFiles = CombineCsv
          .findFiles("resourcePath/gss-merge*.csv").stream()
          .map(e -> e.getAbsolutePath()).toArray(String[]::new);
      final StringBuilder stringBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line).append("\r\n");
      }
      CombineCsv.main(testFiles);
      assertEquals(outContent.toString(), stringBuilder.toString());
      reader.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void testMainWithDuplicatedColumnNames() {
    try {
      final String resourcePath = "src/test/resources/csv/";
      final String refFile = resourcePath + "reference-data/gss-merge.csv";
      final String[] testFiles = CombineCsv
          .findFiles("resourcePath/gss-merge*.csv").stream()
          .map(e -> e.getAbsolutePath()).toArray(String[]::new);
      final BufferedReader reader = new BufferedReader(new FileReader(refFile));
      final StringBuilder stringBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line).append("\r\n");
      }
      CombineCsv.main(testFiles);
      assertEquals(outContent.toString(), stringBuilder.toString());
      reader.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void testMakeDistinct() {
    // strings
    final String[] withoutDupl = {
        "Neque", "porro", "quisquam", "est,", "qui", "dolorem", "ipsum,",
        "quia", "dolor", "sit,", "amet,", "consectetur", "adipisci",
        "velit [...]."
    };
    final String[] withDupl = {
        "Neque", "porro", "quisquam", "Neque", "quisquam", "porro", "est,",
        "qui", "qui", "dolorem", "ipsum,", "quia", "dolor", "sit,", "amet,",
        "consectetur", "adipisci", "quisquam", "velit [...].", "ipsum,"
    };
    final String[] withDuplMadeUnique = {
        "Neque", "porro", "quisquam", "Neque__duplicated_1",
        "quisquam__duplicated_1", "porro__duplicated_1", "est,", "qui",
        "qui__duplicated_1", "dolorem", "ipsum,", "quia", "dolor", "sit,",
        "amet,", "consectetur", "adipisci", "quisquam__duplicated_2",
        "velit [...].", "ipsum,__duplicated_1"
    };

    // inputs lists
    final List<String> listWithoutDupl = Arrays.asList(withoutDupl);
    final List<String> listWithDupl = Arrays.asList(withDupl);
    final List<String> singletonList = Arrays.asList("Neque");
    final List<String> emptyList = new ArrayList<String>();

    // expected results
    final LinkedHashSet<String> refListWithoutDupl = new LinkedHashSet<>(
        Arrays.asList(withoutDupl));
    final LinkedHashSet<String> refListWithDuplMadeUnique = new LinkedHashSet<>(
        Arrays.asList(withDuplMadeUnique));
    final LinkedHashSet<String> refSingletonList = new LinkedHashSet<>(
        Arrays.asList("Neque"));
    final LinkedHashSet<String> refEmptyList = new LinkedHashSet<String>();

    // run tests
    assertEquals(refListWithoutDupl,
        CombineCsv.makeDistinct(listWithoutDupl, "__duplicated_"));
    assertEquals(refListWithDuplMadeUnique,
        CombineCsv.makeDistinct(listWithDupl, "__duplicated_"));
    assertEquals(refSingletonList,
        CombineCsv.makeDistinct(singletonList, "__duplicated_"));
    assertEquals(refEmptyList,
        CombineCsv.makeDistinct(emptyList, "__duplicated_"));

  }

  @Test
  void testMerge() {
    // observation order corresponds to:
    // - row order in imported csv files and
    // - import order of csv files
  }

  @Test
  void testParseCsv() {
    // write:
    // - number of colum names is smaller than number of columns per row
    // - number of colum names is larger than number of columns per row
  }

  @Test
  void testReadRegularHeader() {
    // write:
    // - regular header (no duplicates)
    assertEquals(Arrays.asList(new String[] {
        "obs", "year", "age", "denom", "relig", "partyid", "rincome", "race",
        "marital", "tvhours"
    }), CombineCsv.readHeader("src/test/resources/csv/test-data/header-1.csv"));
  }

  @Test
  void testReadHeaderWithDuplicates() {
    // - header with duplicates
    assertEquals(Arrays.asList(new String[] {
        "obs", "year", "age", "denom", "relig", "partyid", "rincome", "race",
        "marital", "tvhours", "year", "age", "denom"
    }), CombineCsv.readHeader("src/test/resources/csv/test-data/header-2.csv"));
  }

  @Test
  void testReadHeaderWithCommaSeparatedColumnNames() {
    // - header with comma-separated columns (yields list with single string)
    assertEquals(Arrays.asList(new String[] {
        "obs,year,age,denom,relig,partyid,rincome,race,marital,tvhours"
    }), CombineCsv.readHeader("src/test/resources/csv/test-data/header-3.csv"));
  }

  @Test
  void testReadHeaderWithDoubleQuotedColumnNames() {
    // - header with double-quoted column names
    assertEquals(Arrays.asList(new String[] {
        "obs", "year", "age", "denom", "relig", "partyid", "rincome", "race",
        "marital", "tvhours"
    }), CombineCsv.readHeader("src/test/resources/csv/test-data/header-4.csv"));
  }

  @Test
  void testReadHeaderWithSingleQuotedColumnNames() {
    // - header with single-quoted column names
    assertEquals(Arrays.asList(new String[] {
        "'obs'", "'year'", "'age'", "'denom'", "'relig'", "'partyid'",
        "'rincome'", "'race'", "'marital'", "'tvhours'"
    }), CombineCsv.readHeader("src/test/resources/csv/test-data/header-5.csv"));
  }

  @Test
  void testReadHeaderWithEscapedDoubleQuotes() {
    // - header with escaped double quotes and double-quoted single quotes
    assertEquals(Arrays.asList(new String[] {
        "obs", "year \";\"", "age \"(in years)\"", "denom", "relig", "partyid",
        "rincome", "race", "marital",
        "tvhours \"'double-quoted single quotes'\""
    }), CombineCsv.readHeader("src/test/resources/csv/test-data/header-6.csv"));
  }

  @Test
  void testRearrange() {

  }

  @Test
  void testRearrangeMap() {

  }

}
