# DPM Diff command-line reference

<br>

## 1. Overview

The DPM Diff is tool for generating change report from Data Point Model. 
The tool reads  Data Point Model versions from given SQLite databases, compares model contents and produces spreadsheet report from found differences. 

The DPM Diff is executed via Command Line Interface (CLI) and its operation is controlled by passing parameters in the command line.

<br>

### 1.1 Revision history

| Revision | Date       | Author(s) | Description                                   |
| -------- | ---------- | --------- | --------------------------------------------- |
| 0.1      | 2020-09-07 | HE        | Initial command line reference for DPM Diff |
| 0.2      | 2020-11-23 | HE        | New translation languages option |


<br>

## 2. DPM Diff command line options

` --help`

Prints help text about command line options and exit.

`--version`

Prints information about the DPM Diff version and exit.

`--baseline-dpm-db` _[Filename]_

Instructs DPM Diff to use given DPM database as baseline in comparison. 
_[Filename]_ must be a valid filename pointing to DPM database. 

`--current-dpm-db` _[Filename]_

Instructs DPM Diff to use given DPM database as current in comparison. 
_[Filename]_ must be a valid filename pointing to DPM database. 

`--output` _[Filename]_

File where to write spreadsheet report.

`--force-overwrite`

When given, DPM Diff silently overwrites existing conflicting output file. 
 
`--identificationLabelLanguages` _[Language codes]_

Controls for which languages element identification labels are generated to report.
Comma separated list of language codes, for example `fi,sv`.


`--translationLanguages` _[Language codes]_

Controls which languages are reported in translation sections (Dictionary, ReportingFramework and AxisOrdinate translations).
Comma separated list of language codes, for example `fi,sv`.
Optional option. If not given, changes for all languages are reported.


`--verbosity` _[Verbosity mode]_

Execution output verbosity, modes: `NORMAL` and `DEBUG`.
If not given, output verbosity defaults to `NORMAL`. 
`DEBUG` causes the tool output additional detailed information from its execution.

<br>

## 3. Command line examples 

### 3.1 Show DPM Diff command line help

```
$ dpmdiff --help
```

Prints help text about command line options and exit.

<br>

### 3.2 Compare Data Point Models and report all differences

```
$ dpmdiff -baseline-dpm-db SBR-2020-01-07.db --current-dpm-db SBR-2020-04-21.db --output SBR-2020-04-21-changes.xlsx --identificationLabelLanguages fi,sv
```

Compares two Data Point Models, writes a spreadsheet report from differences and exit. 
Baseline model (`-baseline-dpm-db`) for comparison is read from `SBR-2020-01-07.db` file and current model (`--current-dpm-db`) is read from `SBR-2020-04-21.db` file.
Output report (`--output`) is created with filename `SBR-2020-04-21-changes.xlsx`.
Element identification labels (`--identificationLabelLanguages`) are generated to report for languages `fi` and `sv`.
