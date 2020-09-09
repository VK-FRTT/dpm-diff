# DPM Diff

[![Build status](https://github.com/VK-FRTT/dpm-diff/workflows/build/badge.svg)](#)

<br/>

Command line tool for generating change report from Data Point Model. 
The tool reads two Data Point Model versions from given SQLite databases, compares model contents and produces spreadsheet report from found differences. 

## 1. Data Point Model change report

[Empty sample report](docs/dpm_change_report_empty.xlsx) shows generated change report structure.
Change report covers following Data Point Model details:  

| Report section                    | Covered DPM changes |
| --------------------------------- | ------------------------------------------------------------------------------- |
| Dictionary overview               | Added and deleted Domains, Members, Metrics, Dimensions and Hierarchies |
| Dictionary translations           | Added, deleted and changed translations in Domains, Members, Metrics, Dimensions and Hierarchies |
| Domains                           | Added and deleted Domains, changes in IsTypedDomain and DataType |
| Members                           | Added and deleted Members, changes in IsDefaultMember |
| Metrics                           | Added and deleted Metrics, changes in DataType, FlowType, BalanceType, Domain reference and Hierarchy reference |
| Dimensions                        | Added and deleted Dimensions, changes in Domain reference and IsTypedDimension |
| Hierarchies                       | Added and deleted Hierarchies |
| HierarchyNodes                    | Added and deleted HierarchyNodes, changes in ComparisonOperator, UnaryOperator and IsAbstract details |
| HierarchyNodes structure          | Added and deleted HierarchyNodes, changes in Parent Member, Order and Level details |
| ReportingFramework overview       | Added and deleted Reporting Frameworks, Taxonomies, Modules and Tables |
| ReportingFramework translations   | Added, deleted and changed translations in Reporting Frameworks, Taxonomies, Modules and Tables |
| Tables                            | Added and deleted Tables, changes in FilingIndicator |
| TableAxis                         | Added and deleted Table Axis, changes in Order |
| AxisOrdinates                     | Added and deleted Axis Ordinates, changes in IsDisplayBeforeChildren, IsAbstractHeader, IsRowKey and TypeOfKey |
| AxisOrdinate translations         | Added, deleted and changed translations  in Axis Ordinates |
| OrdinateCategorisations           | Added and deleted OrdinateCategorisations |

<br/>

## 2. System structure

DPM Diff consists from following modules:

#### `diff-model`
- Implements object comparison framework for DPM Diff 
- Provides: 
    - Data structures for describing report sections and their object structure (data fields)
    - Data structures for object data 
    - Logic for composing correlation key from object data
    - Logic for correlating objects between data sets and resolving detailed object changes
    - Data structures for object change information
    - Logic for sorting object change entries 
        
#### `diff-report-generator`
- Describes Data Point Model change report sections and their content (in declarative manner)
- Reads Data Point Model data from SQLite database (with SQL queries) and maps it to `diff-model`
- Executes object comparison with `diff-model` 

#### `diff-spr-output`  
- Outputs `diff-model` based change report information to spreadsheet file 

#### `diff-cli`
- Stand-alone command line application for executing DPM Diff from command line.
- DPM Diff execution can be controlled with command line parameters. See [DPM Diff command-line reference](docs/dpm-diff-command-line-reference.md) for further information.

<br/>

## 3. Development

### 3.1 Building

Prerequisites:

- Java 8+
- Gradle 4.6

<br/>

Building runnable JAR:

`$ gradlew jar`

<br/>

Executing DPM Diff from JAR:

`$ java -jar diff-cli/build/libs/diff-cli.jar`

<br/>

### 3.2 Testing

DPM Diff test suite can be executed with:

`$ gradlew test`

<br/>

### 3.3 Code style 

Code style is managed by Spotless and ktlint. Source code can be scanned for format violations with: 

`$ gradlew spotlessCheck`

