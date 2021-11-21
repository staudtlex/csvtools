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
    String[] arrayWithoutDuplicates = {
        "Neque", "porro", "quisquam", "est,", "qui", "dolorem", "ipsum,",
        "quia", "dolor", "sit,", "amet,", "consectetur", "adipisci",
        "velit [...]."
    };
    String[] arrayWithDuplicates = {
        "Neque", "porro", "quisquam", "Neque", "quisquam", "porro", "est,",
        "qui", "qui", "dolorem", "ipsum,", "quia", "dolor", "sit,", "amet,",
        "consectetur", "adipisci", "quisquam", "velit [...].", "ipsum,"
    };

    // inputs lists
    List<String> listWithoutDuplicates = Arrays.asList(arrayWithoutDuplicates);
    List<String> listWithDuplicates = Arrays.asList(arrayWithDuplicates);
    List<String> singletonList = Arrays.asList("Neque");
    List<String> emptyList = new ArrayList<String>();

    // expected results
    List<String> referenceListWithoutDuplicates = Arrays
        .asList(arrayWithoutDuplicates);
    List<String> referenceListWithDuplicatesMadeUnique = Arrays
        .asList(arrayWithoutDuplicates);
    List<String> referenceSingletonList = Arrays.asList("Neque");
    List<String> referenceEmptyList = new ArrayList<String>();

    // run tests
    assertEquals(referenceListWithoutDuplicates,
        CombineCsv.getDistinct(listWithoutDuplicates));
    assertEquals(referenceListWithDuplicatesMadeUnique,
        CombineCsv.getDistinct(listWithDuplicates));
    assertEquals(referenceSingletonList, CombineCsv.getDistinct(singletonList));
    assertEquals(referenceEmptyList, CombineCsv.getDistinct(emptyList));
  }

  @Test
  void testMainWithUniqueColumnNames() {
    try {
      String resourcePath = "src/test/resources/csv/";
      String refFile = resourcePath + "reference-data/gss-append.csv";
      BufferedReader reader = new BufferedReader(new FileReader(refFile));
      String[] testFiles = CombineCsv.findFiles("resourcePath/gss-merge*.csv")
          .stream().map(e -> e.getAbsolutePath()).toArray(String[]::new);
      StringBuilder stringBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line).append("\r\n");
      }
      CombineCsv.main(testFiles);
      assertEquals(outContent.toString(), stringBuilder.toString());
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void testMainWithDuplicatedColumnNames() {
    try {
      String resourcePath = "src/test/resources/csv/";
      String refFile = resourcePath + "reference-data/gss-merge.csv";
      String[] testFiles = CombineCsv.findFiles("resourcePath/gss-merge*.csv")
          .stream().map(e -> e.getAbsolutePath()).toArray(String[]::new);
      BufferedReader reader = new BufferedReader(new FileReader(refFile));
      StringBuilder stringBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line).append("\r\n");
      }
      CombineCsv.main(testFiles);
      assertEquals(outContent.toString(), stringBuilder.toString());
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void testMakeDistinct() {
    // strings
    String[] arrayWithoutDuplicates = {
        "Neque", "porro", "quisquam", "est,", "qui", "dolorem", "ipsum,",
        "quia", "dolor", "sit,", "amet,", "consectetur", "adipisci",
        "velit [...]."
    };
    String[] arrayWithDuplicates = {
        "Neque", "porro", "quisquam", "Neque", "quisquam", "porro", "est,",
        "qui", "qui", "dolorem", "ipsum,", "quia", "dolor", "sit,", "amet,",
        "consectetur", "adipisci", "quisquam", "velit [...].", "ipsum,"
    };
    String[] arrayWithDuplicatesMadeUnique = {
        "Neque", "porro", "quisquam", "Neque__duplicated_1",
        "quisquam__duplicated_1", "porro__duplicated_1", "est,", "qui",
        "qui__duplicated_1", "dolorem", "ipsum,", "quia", "dolor", "sit,",
        "amet,", "consectetur", "adipisci", "quisquam__duplicated_2",
        "velit [...].", "ipsum,__duplicated_1"
    };

    // inputs lists
    List<String> listWithoutDuplicates = Arrays.asList(arrayWithoutDuplicates);
    List<String> listWithDuplicates = Arrays.asList(arrayWithDuplicates);
    List<String> singletonList = Arrays.asList("Neque");
    List<String> emptyList = new ArrayList<String>();

    // expected results
    List<String> referenceListWithoutDuplicates = Arrays
        .asList(arrayWithoutDuplicates);
    List<String> referenceListWithDuplicatesMadeUnique = Arrays
        .asList(arrayWithDuplicatesMadeUnique);
    List<String> referenceSingletonList = Arrays.asList("Neque");
    List<String> referenceEmptyList = new ArrayList<String>();

    // run tests
    assertEquals(referenceListWithoutDuplicates,
        CombineCsv.makeDistinct(listWithoutDuplicates, "__duplicated_"));
    assertEquals(referenceListWithDuplicatesMadeUnique,
        CombineCsv.makeDistinct(listWithDuplicates, "__duplicated_"));
    assertEquals(referenceSingletonList,
        CombineCsv.makeDistinct(singletonList, "__duplicated_"));
    assertEquals(referenceEmptyList,
        CombineCsv.makeDistinct(emptyList, "__duplicated_"));

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

}
