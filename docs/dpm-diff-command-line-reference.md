# DPM Diff command-line reference

<br>

## 1. Overview

The DPM Diff is tool for generating change report from Data Point Model and VK Data databases. 
The tool reads Data Point Model versions from given databases, compares model contents and produces spreadsheet report from found differences. 

The DPM Diff is executed via Command Line Interface (CLI) and its operation is controlled by passing parameters in the command line.

<br>

### 1.1 Revision history

| Revision | Date       | Author(s) | Description                                   |
| -------- | ---------- | --------- | --------------------------------------------- |
| 0.1      | 2020-09-07 | HE        | Initial command line reference for DPM Diff |
| 0.2      | 2020-11-23 | HE        | New translation languages option |
| 0.3      | 2021-01-27 | HE        | Separate DPM and VK database compare commands, option to control included report sections  |


<br>

## 2. DPM Diff command line options

`--help`

Command to print help text about command line options and exit.


`--version`

Command to print information about the tool version and exit.


`--compareDpm`

Command to compare two DPM databases and create report from found differences.


`--compareVkData`

Command to compare two VK databases and create report from found differences.


`--baselineDb` _[Filename]_

Database to use as baseline in comparison. 
_[Filename]_ must be a valid filename pointing to database. 


`--currentDb` _[Filename]_

Database to use as current in comparison. 
_[Filename]_ must be a valid filename pointing to database. 


`--output` _[Filename]_

Filename where to write generated report.


`--forceOverwrite`

When given, DPM Diff silently overwrites existing conflicting output file. 
 
 
`--identificationLabelLanguages` _[Language codes]_

Controls for which languages element identification labels are generated to report.
Comma separated list of language codes, for example `fi,sv`.
Works only with `--compareDpm` command. 


`--translationLanguages` _[Language codes]_

Controls which languages are reported in translation sections (Dictionary, ReportingFramework and AxisOrdinate translations).
Comma separated list of language codes, for example `fi,sv`.
Optional option. If not given, changes for all languages are reported.
Works only with `--compareDpm` command. 


`--reportSections`

Controls which report sections are included into generated report.
Comma separated list of section names, for example: `domain,member,metric`.


`--verbosity` _[Verbosity mode]_

Execution output verbosity, modes: `NORMAL`, `VERBOSE` and `DEBUG`.
If not given, output verbosity defaults to `NORMAL`. 

<br>

## 3. Command line examples 

### 3.1 Show DPM Diff command line help

```
$ dpmdiff --help
```

Prints help text about command line options and exit.

<br>

### 3.2 Compare two Data Point Models and report all differences

```
$ dpmdiff --compareDpm --baselineDb SBR-2020-01-07.db --currentDb SBR-2020-04-21.db --output SBR-2020-04-21-changes.xlsx --identificationLabelLanguages fi,sv
```

Compares two Data Point Models (`--compareDpm`), writes a spreadsheet report from differences and exit. 
Baseline model (`--baselineDb`) for comparison is read from `SBR-2020-01-07.db` file and current model (`--currentDb`) is read from `SBR-2020-04-21.db` file.
Output report (`--output`) is created with filename `SBR-2020-04-21-changes.xlsx`.
Element identification labels (`--identificationLabelLanguages`) are generated to report for languages `fi` and `sv`.
