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
# on your system, use this makefile to build and package csvtools.  

# =============================================================================
#     Main variables
# =============================================================================

JAVAC = javac
JAR = jar
JAVA = java

LIBDIR = lib
SRCDIR = src/main/java/de/staudtlex/csvtools
TARGETDIR = target
CLASSDIR = $(TARGETDIR)/classes
TMPDIR = $(TARGETDIR)/tmp
TESTDATADIR = src/test/resources/csv/test-data
TESTRESULTDIR = $(TARGETDIR)/test-result-data

CCSV_VERSION = 1.9.0
CCSV_JAR = commons-csv-$(CCSV_VERSION).jar

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


# Get commons-csv-1.9.0.jar (build and runtime dependency)
$(LIBDIR):
	@[ -d $@ ] || mkdir -p $@;

$(LIBDIR)/$(CCSV_JAR): $(LIBDIR)
	$(info *** Download dependency ($(PACKAGE_JAR)) ***)
	@if command -v curl; then \
		curl -s -O https://repo1.maven.org/maven2/org/apache/commons/commons-csv/$(CCSV_VERSION)/$(CCSV_JAR) -o $(CCSV_JAR); \
		mv $(CCSV_JAR) $@; \
	elif command -v wget; then \
		wget -P $< "https://repo1.maven.org/maven2/org/apache/commons/commons-csv/$(CCSV_VERSION)/$(CCSV_JAR)"; \
	else \
		echo "Neither curl nor wget installed."; \
	fi; 


# Compile: generate class files
.PHONY: compile

$(CLASSDIR):
	@[ -d $@ ] || mkdir -p $@;

compile: $(CLASSDIR) $(LIBDIR)/$(CCSV_JAR)
	$(info *** Compile source files ***)
	@$(JAVAC) -classpath $(LIBDIR)/$(CCSV_JAR) $(SRCDIR)/*.java \
	-target 1.8 -source 1.8 -encoding utf8 -d $(CLASSDIR)


# Create jar
jar: $(TARGETDIR)/$(PACKAGE_JAR)
$(TARGETDIR)/$(PACKAGE_JAR): $(CLASSDIR) compile
	$(info *** Create jar $@ ***)
	@$(JAR) -cfe $@ \
		de.staudtlex.csvtools.CombineCsv \
		-C $< de

# Create uber-jar
$(TMPDIR):
	@[ -d $@ ] || mkdir -p $@;

uber-jar: $(TARGETDIR)/$(PACKAGE_UBER_JAR)
$(TARGETDIR)/$(PACKAGE_UBER_JAR): $(LIBDIR)/$(CCSV_JAR) $(TMPDIR) compile 
	$(info *** Create uber-jar $(PACKAGE_UBER_JAR) ***)
	@(cp $< $(TMPDIR)/ && cd $(TMPDIR) && $(JAR) -xf $(CCSV_JAR)) && \
	$(JAR) -cfe $@ \
		de.staudtlex.csvtools.CombineCsv \
		-C $(CLASSDIR) de \
		-C $(TMPDIR) org \
		-C $(TMPDIR) META-INF


# Example: run example
.PHONY: examples \
	$(TESTRESULTDIR)/jar-append-example.csv \
	$(TESTRESULTDIR)/uber-jar-append-example.csv \
	$(TESTRESULTDIR)/jar-merge-example.csv \
	$(TESTRESULTDIR)/uber-jar-merge-example.csv 

$(TESTRESULTDIR):
	@[ -d $@ ] || mkdir -p $@;

examples: \
	$(TESTRESULTDIR)/jar-append-example.csv \
	$(TESTRESULTDIR)/uber-jar-append-example.csv \
	$(TESTRESULTDIR)/jar-merge-example.csv \
	$(TESTRESULTDIR)/uber-jar-merge-example.csv 

$(TESTRESULTDIR)/jar-append-example.csv: jar $(TESTRESULTDIR)
	$(info *** Run example (see $@) ***)
	@$(JAVA) -classpath $(LIBDIR)/$(CCSV_JAR):$(TARGETDIR)/$(PACKAGE_JAR) \
	de.staudtlex.csvtools.CombineCsv $(TESTDATADIR)/gss-append-*.csv > $@

$(TESTRESULTDIR)/uber-jar-append-example.csv: uber-jar $(TESTRESULTDIR)
	$(info *** Run example (see $@) ***)
	@$(JAVA) -jar $(TARGETDIR)/$(PACKAGE_UBER_JAR) \
	$(TESTDATADIR)/gss-append-*.csv > $@

$(TESTRESULTDIR)/jar-merge-example.csv: jar $(TESTRESULTDIR)
	$(info *** Run example (see $@) ***)
	@$(JAVA) -classpath $(LIBDIR)/$(CCSV_JAR):$(TARGETDIR)/$(PACKAGE_JAR) \
	de.staudtlex.csvtools.CombineCsv $(TESTDATADIR)/gss-merge-*.csv > $@

$(TESTRESULTDIR)/uber-jar-merge-example.csv: uber-jar $(TESTRESULTDIR)
	$(info *** Run example (see $@) ***)
	@$(JAVA) -jar $(TARGETDIR)/$(PACKAGE_UBER_JAR) \
	$(TESTDATADIR)/gss-merge*.csv > $@

# Cleanup: remove artifacts created by make targets
.PHONY: clean clean_targets clean_libs clean_examples 

clean: clean_targets

clean_all: clean_targets clean_libs clean_examples

clean_examples:
	@rm -f $(TESTRESULTDIR)/*-example.csv

clean_targets:
	@rm -rf $(TARGETDIR)

clean_libs:
	@rm -rf $(LIBDIR)