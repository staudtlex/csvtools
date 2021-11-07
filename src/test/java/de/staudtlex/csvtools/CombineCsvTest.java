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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CombineCsvTest {
  @Test
  void testGetDistinct() {
    // strings
    String[] arrayWithoutDuplicates = {
        "Neque", "porro", "quisquam", "est", "qui", "dolorem", "ipsum", "quia",
        "dolor", "sit", "amet", "consectetur", "adipisci", "velit"
    };
    String[] arrayWithDuplicates = {
        "Neque", "porro", "quisquam", "Neque", "quisquam", "porro", "est",
        "qui", "qui", "dolorem", "ipsum", "quia", "dolor", "sit", "amet",
        "consectetur", "adipisci", "quisquam", "velit", "ipsum"
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
  void testMakeDistinct() {

    // strings
    String[] arrayWithoutDuplicates = {
        "Neque", "porro", "quisquam", "est", "qui", "dolorem", "ipsum", "quia",
        "dolor", "sit", "amet", "consectetur", "adipisci", "velit"
    };
    String[] arrayWithDuplicates = {
        "Neque", "porro", "quisquam", "Neque", "quisquam", "porro", "est",
        "qui", "qui", "dolorem", "ipsum", "quia", "dolor", "sit", "amet",
        "consectetur", "adipisci", "quisquam", "velit", "ipsum"
    };
    String[] arrayWithDuplicatesMadeUnique = {
        "Neque", "porro", "quisquam", "Neque__duplicated_1",
        "quisquam__duplicated_1", "porro__duplicated_1", "est", "qui",
        "qui__duplicated_1", "dolorem", "ipsum", "quia", "dolor", "sit", "amet",
        "consectetur", "adipisci", "quisquam__duplicated_2", "velit",
        "ipsum__duplicated_1"
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

}
