# Copyright (C) 2021 Alexander Staudt
# 
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free Software
# Foundation, either version 3 of the License, or (at your option) any later
# version.
# 
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
# 
# You should have received a copy of the GNU General Public License along with
# this program. If not, see <http://www.gnu.org/licenses/>.

# =============================================================================
#     NOTE
# =============================================================================
# csvtools is intended to be built with Maven. In case Maven is not available 
# on your system but make is, use this makefile to build, test, and package 
# csvtools.

# =============================================================================
#     Main variables
# =============================================================================

JAVAC = javac
JAR = jar
JAVA = java
BOOTCLASSPATH_FLAG = 

LIBDIR = lib
SRCDIR = src/main/java/de/staudtlex/csvtools
TARGETDIR = target
CLASSDIR = $(TARGETDIR)/classes
TMPDIR = $(TARGETDIR)/tmp
TESTSRCDIR = src/test/java/de/staudtlex/csvtools
TESTCLASSDIR = $(TARGETDIR)/test-classes
TESTDATADIR = src/test/resources/csv/test-data
RESULTDIR = $(TARGETDIR)/test-result-data

COMMONS_CSV_VERSION = 1.9.0
COMMONS_CSV_JAR = commons-csv-$(COMMONS_CSV_VERSION).jar
COMMONS_CSV_URL = https://repo1.maven.org/maven2/org/apache/commons/commons-csv/$(COMMONS_CSV_VERSION)/$(COMMONS_CSV_JAR)

JUNIT_CONSOLE_STANDALONE_VERSION = 1.8.1
JUNIT_CONSOLE_STANDALONE_JAR = junit-platform-console-standalone-$(JUNIT_CONSOLE_STANDALONE_VERSION).jar
JUNIT_CONSOLE_STANDALONE_URL = https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/$(JUNIT_CONSOLE_STANDALONE_VERSION)/$(JUNIT_CONSOLE_STANDALONE_JAR)

VERSION = 1.0.0
JAR_NAME = combine-csv-$(VERSION)
PACKAGE_JAR = $(JAR_NAME).jar
PACKAGE_UBER_JAR = $(JAR_NAME)-jar-with-dependencies.jar

# =============================================================================
#     Targets
# =============================================================================
# Default target
.PHONY: all
all: jar uber-jar


# Get build, runtime, and test dependencies
deps: \
	$(LIBDIR) \
	$(LIBDIR)/$(COMMONS_CSV_JAR) \
	$(LIBDIR)/$(JUNIT_CONSOLE_STANDALONE_JAR)

$(LIBDIR):
	@[ -d $@ ] || mkdir -p $@;

$(LIBDIR)/$(COMMONS_CSV_JAR): $(LIBDIR)
	$(info *** Download dependency ($(COMMONS_CSV_JAR)) ***)
	@if command -v curl; then \
		curl -s -O $(COMMONS_CSV_URL) -o $(COMMONS_CSV_JAR); \
		mv $(COMMONS_CSV_JAR) $@; \
	elif command -v wget; then \
		wget -P $< "$(COMMONS_CSV_URL)"; \
	else \
		echo "Neither curl nor wget installed."; \
	fi; 

$(LIBDIR)/$(JUNIT_CONSOLE_STANDALONE_JAR): $(LIBDIR)
	$(info *** Download dependency ($(JUNIT_CONSOLE_STANDALONE_JAR)) ***)
	@if command -v curl; then \
		curl -s -O $(JUNIT_CONSOLE_STANDALONE_URL) -o $(JUNIT_CONSOLE_STANDALONE_JAR); \
		mv $(JUNIT_CONSOLE_STANDALONE_JAR) $@; \
	elif command -v wget; then \
		wget -P $< "$(JUNIT_CONSOLE_STANDALONE_URL)"; \
	else \
		echo "Neither curl nor wget installed."; \
	fi; 


# Compile: generate class files
.PHONY: classes

$(CLASSDIR):
	@[ -d $@ ] || mkdir -p $@;

classes: $(CLASSDIR) $(LIBDIR)/$(COMMONS_CSV_JAR)
	$(info *** Compile source files ***)
	@$(JAVAC) -classpath $(LIBDIR)/$(COMMONS_CSV_JAR) $(SRCDIR)/*.java \
	-target 1.8 -source 1.8 $(BOOTCLASSPATH_FLAG) -encoding utf8 -d $(CLASSDIR)


# Create jar
jar: $(TARGETDIR)/$(PACKAGE_JAR)

$(TARGETDIR)/$(PACKAGE_JAR): $(CLASSDIR) classes
	$(info *** Create jar $@ ***)
	@$(JAR) -cfe $@ \
		de.staudtlex.csvtools.CombineCsv \
		-C $< de


# Create uber-jar
$(TMPDIR):
	@[ -d $@ ] || mkdir -p $@;

uber-jar: $(TARGETDIR)/$(PACKAGE_UBER_JAR)

$(TARGETDIR)/$(PACKAGE_UBER_JAR): $(LIBDIR)/$(COMMONS_CSV_JAR) $(TMPDIR) classes
	$(info *** Create uber-jar $(PACKAGE_UBER_JAR) ***)
	@(cp $< $(TMPDIR)/ && cd $(TMPDIR) && $(JAR) -xf $(COMMONS_CSV_JAR)) && \
	$(JAR) -cfe $@ \
		de.staudtlex.csvtools.CombineCsv \
		-C $(CLASSDIR) de \
		-C $(TMPDIR) org \
		-C $(TMPDIR) META-INF


# Compile test-classes
$(TESTCLASSDIR):
	@[ -d $@ ] || mkdir -p $@;

test-classes: $(TESTCLASSDIR) $(LIBDIR)/$(JUNIT_CONSOLE_STANDALONE_JAR) classes
	$(info *** Compile test source files ***)
	@$(JAVAC) -classpath $(LIBDIR)/$(JUNIT_CONSOLE_STANDALONE_JAR):$(CLASSDIR)/ \
	$(TESTSRCDIR)/*.java \
	-target 1.8 -source 1.8 $(BOOTCLASSPATH_FLAG) -encoding utf8 -d $(TESTCLASSDIR)


# Run tests
test: test-classes
	$(info *** Run JUnit tests ***)
	java -jar $(LIBDIR)/$(JUNIT_CONSOLE_STANDALONE_JAR) \
		-cp $(TESTCLASSDIR):$(CLASSDIR):$(LIBDIR)/$(COMMONS_CSV_JAR) \
		--select-package de.staudtlex.csvtools


# Example: run example
.PHONY: examples \
	$(RESULTDIR)/jar-append-example.csv \
	$(RESULTDIR)/uber-jar-append-example.csv \
	$(RESULTDIR)/jar-merge-example.csv \
	$(RESULTDIR)/uber-jar-merge-example.csv 

$(RESULTDIR):
	@[ -d $@ ] || mkdir -p $@;

examples: \
	$(RESULTDIR)/jar-append-example.csv \
	$(RESULTDIR)/uber-jar-append-example.csv \
	$(RESULTDIR)/jar-merge-example.csv \
	$(RESULTDIR)/uber-jar-merge-example.csv 
	cd $(RESULTDIR) && dos2unix *

$(RESULTDIR)/jar-append-example.csv: jar $(RESULTDIR)
	$(info *** Run example (see $@) ***)
	@$(JAVA) -classpath $(LIBDIR)/$(CCSV_JAR):$(TARGETDIR)/$(PACKAGE_JAR) \
	de.staudtlex.csvtools.CombineCsv $(TESTDATADIR)/gss-append-*.csv > $@

$(RESULTDIR)/uber-jar-append-example.csv: uber-jar $(RESULTDIR)
	$(info *** Run example (see $@) ***)
	@$(JAVA) -jar $(TARGETDIR)/$(PACKAGE_UBER_JAR) \
	$(TESTDATADIR)/gss-append-*.csv > $@

$(RESULTDIR)/jar-merge-example.csv: jar $(RESULTDIR)
	$(info *** Run example (see $@) ***)
	@$(JAVA) -classpath $(LIBDIR)/$(CCSV_JAR):$(TARGETDIR)/$(PACKAGE_JAR) \
	de.staudtlex.csvtools.CombineCsv $(TESTDATADIR)/gss-merge-*.csv > $@

$(RESULTDIR)/uber-jar-merge-example.csv: uber-jar $(RESULTDIR)
	$(info *** Run example (see $@) ***)
	@$(JAVA) -jar $(TARGETDIR)/$(PACKAGE_UBER_JAR) \
	$(TESTDATADIR)/gss-merge*.csv > $@


# Cleanup: remove artifacts created by make targets
.PHONY: clean clean_targets clean_libs clean_examples 

clean: clean_targets

clean_all: clean_targets clean_libs clean_examples

clean_examples:
	@rm -f $(RESULTDIR)/*-example.csv

clean_targets:
	@rm -rf $(TARGETDIR)

clean_libs:
	@rm -rf $(LIBDIR)