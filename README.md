Let me read the Retention Guide to understand the specific metrics you need, then I'll revise the solution to focus on attrition and retention with better terminology.Now I understand both guides. Let me revise the solution to focus specifically on **Attrition and Retention** with better terminology. I'll use **"Period Start"** and **"Period End"** instead of "Earlier" and "Later".

---

# REVISED POWER BI SOLUTION: DISA ATTRITION & RETENTION

## KEY TERMINOLOGY CHANGES

| Old Term | New Term | Purpose |
|----------|----------|---------|
| Earlier | Period Start | Beginning of measurement period |
| Later | Period End | End of measurement period |
| DimSnapshotEarlier | DimPeriodStart | Slicer for start date |
| DimSnapshotLater | DimPeriodEnd | Slicer for end date |

---

## UNDERSTANDING THE METRICS (From Your Guides)

### Retention Rate Formula
```
Retention Rate = Employees Remaining / On-Hand at Period Start
```
- **Remaining** = Present in Period Start AND Present in Period End AND in same Org/Center/Series
- For Agency-level: Don't check Org/Center/Series movement
- **Exclude**: Students/Interns

### Attrition Rate Formula
```
Attrition Rate = Losses / Average On-Hand
```
- **Average On-Hand** = (Period Start count + Period End count) / 2
- **Losses** = Departures during period (from Personnel Actions)
- Based on NOA codes (300s, T352, CAO for external; 501, 570, 702, 721 for internal)

### Student/Intern Identification
Per the Retention Guide:
1. Title contains "STUDENT" or "INTERN"
2. OR Occ Series Current ends with "99"
3. BUT EXCLUDE if Manpower PCN contains "RG" or "VT" (Recent Grads, Transitioning Veterans)

---

## 1. REVISED DATA MODEL

```
                    ┌─────────────────┐
                    │  DimPeriodStart │
                    │  (Disconnected) │
                    └─────────────────┘
                    
                    ┌─────────────────┐
                    │  DimPeriodEnd   │
                    │  (Disconnected) │
                    └─────────────────┘

┌──────────┐       ┌─────────────────────┐       ┌──────────┐
│  DimOrg  │◄──────│ FactEmployeeSnapshot│──────►│ DimDate  │
│          │  *:1  │                     │  *:1  │          │
└──────────┘       └─────────────────────┘       └──────────┘
                            │
                            │ *:1
                            ▼
                    ┌──────────────┐
                    │  DimSeries   │
                    └──────────────┘
```

**Tables:**
| Table | Type | Purpose |
|-------|------|---------|
| FactEmployeeSnapshot | Fact | All employee records from all snapshots |
| DimOrg | Dimension | Organization hierarchy from OrgMAPPING |
| DimSeries | Dimension | Job series lookup |
| DimDate | Dimension | Calendar dimension |
| DimPeriodStart | Disconnected | Slicer for period start date |
| DimPeriodEnd | Disconnected | Slicer for period end date |

---

## 2. POWER QUERY M CODE

### 2.1 Query: `fnGetSnapshotDate` (Helper Function)

```powerquery-m
// Query Name: fnGetSnapshotDate
// Purpose: Extract date from filename like "DISA Empl Data 31 03 25.xlsx"

let
    fnGetSnapshotDate = (fileName as text) as date =>
    let
        FileNameNoExt = Text.BeforeDelimiter(fileName, ".xlsx"),
        Parts = Text.Split(FileNameNoExt, " "),
        PartCount = List.Count(Parts),
        DayText = Parts{PartCount - 3},
        MonthText = Parts{PartCount - 2},
        YearText = Parts{PartCount - 1},
        Day = Number.FromText(DayText),
        Month = Number.FromText(MonthText),
        Year = Number.FromText(YearText),
        FullYear = if Year < 100 then 2000 + Year else Year,
        Result = #date(FullYear, Month, Day)
    in
        Result
in
    fnGetSnapshotDate
```

---

### 2.2 Query: `OrgMapping`

```powerquery-m
// Query Name: OrgMapping
// Purpose: Load organization mapping from OrgMAPPING.xlsx

let
    // CHANGE THIS PATH to your actual file location
    Source = Excel.Workbook(
        File.Contents("C:\YourPath\OrgMAPPING.xlsx"), 
        null, 
        true
    ),
    DataTable = Source{[Item="Table1",Kind="Table"]}[Data],
    PromotedHeaders = Table.PromoteHeaders(DataTable, [PromoteAllScalars=true]),
    TypedTable = Table.TransformColumnTypes(PromotedHeaders, {
        {"Org", type text},
        {"Center", type text},
        {"Org Code", type text},
        {"CAM ID", type text}
    }),
    CleanedTable = Table.TransformColumns(TypedTable, {
        {"Org", Text.Trim, type text},
        {"Center", Text.Trim, type text},
        {"Org Code", Text.Trim, type text},
        {"CAM ID", Text.Trim, type text}
    }),
    RemovedBlanks = Table.SelectRows(CleanedTable, each 
        [Org] <> null and [Org] <> ""
    )
in
    RemovedBlanks
```

---

### 2.3 Query: `FactEmployeeSnapshot` (Main Fact Table)

```powerquery-m
// Query Name: FactEmployeeSnapshot
// Purpose: Load all employee snapshot files, combine with SnapshotDate
// Includes Student/Intern logic per Retention Guide

let
    // =====================================================
    // STEP 1: CONNECT TO FOLDER
    // CHANGE THIS PATH to your snapshot folder
    // =====================================================
    FolderPath = "C:\YourPath\SnapshotFolder\",
    Source = Folder.Files(FolderPath),
    
    // =====================================================
    // STEP 2: FILTER TO VALID SNAPSHOT FILES
    // =====================================================
    FilteredFiles = Table.SelectRows(Source, each 
        Text.StartsWith([Name], "DISA Empl Data") and 
        Text.EndsWith([Name], ".xlsx") and
        not Text.Contains([Name], "~$")
    ),
    
    // =====================================================
    // STEP 3: EXTRACT SNAPSHOT DATE FROM FILENAME
    // =====================================================
    AddedSnapshotDate = Table.AddColumn(FilteredFiles, "SnapshotDate", each 
        fnGetSnapshotDate([Name]), 
        type date
    ),
    
    // =====================================================
    // STEP 4: FUNCTION TO LOAD EACH EXCEL FILE
    // =====================================================
    LoadExcelContent = (fileContent as binary) as table =>
        let
            Workbook = Excel.Workbook(fileContent, null, true),
            DataTable = try Workbook{[Item="Table1",Kind="Table"]}[Data] 
                        otherwise Workbook{0}[Data],
            Promoted = Table.PromoteHeaders(DataTable, [PromoteAllScalars=true])
        in
            Promoted,
    
    // =====================================================
    // STEP 5: LOAD CONTENT FROM EACH FILE
    // =====================================================
    AddedContent = Table.AddColumn(AddedSnapshotDate, "FileData", each 
        LoadExcelContent([Content]), 
        type table
    ),
    
    SelectColumns = Table.SelectColumns(AddedContent, {"SnapshotDate", "FileData"}),
    
    // =====================================================
    // STEP 6: EXPAND FILE DATA (all columns)
    // =====================================================
    ExpandedData = Table.ExpandTableColumn(SelectColumns, "FileData", {
        "SSAN_EMPL_CON_NR", "DoD ID (EDIPI)", "Employee Num", "Position ID",
        "CPCN", "Dt of Birth", "Sex", "Step or Rate", "SCD Leave",
        "SCD Civilian", "SCD RIF", "SALARY_HR", "Pay Plan Current",
        "Occ Series Current", "NAME-FULL", "Telework Indicator",
        "Telework Indicator Desc", "Telework Eligibility",
        "Telework Eligibility Desc", "Handicap Code", "Grade or Level",
        "TARGET_GR_CIV", "WGI Last Equivalent Increase", "Dt Last Promotion",
        "Tenure", "Country World Citizenship", "CCPO_ID", "Competitive Level",
        "Location", "Manpower PCN", "PAS Code", "PAS_AUTH",
        "Personnel Office ID", "Org Structure ID", "Title", "Psn Sensitivity",
        "Unit ID Code", "Basic Salary Rate", "Locality Pay Rate",
        "Total Pay Amt", "Pay Rate Determinant", "Bargaining Unit Status",
        "Payroll Office ID", "Pay Basis", "Pay Table ID", "Work Schedule",
        "Reserve Category", "Veterans Preference", "Veterans Status",
        "Veteran Status Desc", "Dt Appraisal  Effective", "Rating of Record",
        "Appraisal Type", "Supervisory Status", "Dt Assigned Current Agency",
        "Retirement Plan", "Appointment Type", "Current Appointment Auth 1",
        "Current Appointment Auth 2", "Duty Status", "Dt Arrived Personnel Office",
        "Psn Seq", "WGI Dt Due", "FUNDING_CATEGORIES", "PAYROLL_COST_CODE",
        "Occupation Category Code", "FLSA Category", "Ethnicity and Race Full Desc",
        "Ethnicity and Race Full", "IA Category", "IA_CATEGORY_TEXT",
        "IA Level", "IA_LEVEL_TEXT", "IA Duty", "IA_DUTY_TEXT",
        "Psn Specialty Code", "IA Workforce Category", "IA Specialty Type",
        "IA Workforce Level", "Dt Appt Ltr Signed", "Cumulative Sustainment Tng Hrs",
        "In Sourced Indicator", "In Sourced Date", "Orig In Source Seq Num",
        "Billet OUID", "Billet OUID Start Date", "Pers Primary Work Role",
        "Pers Primary Work Role Desc", "Pers Additional Work Role 1",
        "Pers Additional Work Role 1 Desc", "Pers Additional Work Role 2",
        "Pers Additional Work Role 2 Desc"
    }),
    
    // =====================================================
    // STEP 7: ADD EmployeeKey (clean EDIPI)
    // =====================================================
    AddEmployeeKey = Table.AddColumn(ExpandedData, "EmployeeKey", each 
        Text.Trim([#"DoD ID (EDIPI)"] ?? ""),
        type text
    ),
    
    // =====================================================
    // STEP 8: ADD OrgFirst2 (first 2 chars of Org Structure ID)
    // Per Retention Guide
    // =====================================================
    AddOrgFirst2 = Table.AddColumn(AddEmployeeKey, "OrgFirst2", each 
        if [Org Structure ID] = null or [Org Structure ID] = "" 
        then null 
        else Text.Start(Text.Trim([Org Structure ID]), 2),
        type text
    ),
    
    // =====================================================
    // STEP 9: ADD SeriesCode (clean series)
    // =====================================================
    AddSeriesCode = Table.AddColumn(AddOrgFirst2, "SeriesCode", each 
        if [Occ Series Current] = null 
        then "Unknown" 
        else Text.Trim([Occ Series Current]),
        type text
    ),
    
    // =====================================================
    // STEP 10: MERGE WITH ORG MAPPING
    // =====================================================
    MergedOrg = Table.NestedJoin(
        AddSeriesCode, {"OrgFirst2"}, 
        OrgMapping, {"Org"}, 
        "OrgMappingData", JoinKind.LeftOuter
    ),
    
    ExpandedOrg = Table.ExpandTableColumn(MergedOrg, "OrgMappingData", 
        {"Center", "Org Code", "CAM ID"}, 
        {"Center", "OrgCode", "CAM_ID"}
    ),
    
    FilledOrgDefaults = Table.ReplaceValue(ExpandedOrg, null, "Unmapped", 
        Replacer.ReplaceValue, {"Center", "OrgCode", "CAM_ID"}
    ),
    
    // =====================================================
    // STEP 11: ADD IsStudentIntern (PER RETENTION GUIDE)
    // Logic:
    //   1. Title contains "STUDENT" OR "INTERN" = Yes
    //   2. OR Occ Series ends with "99" = Yes
    //   3. BUT if Manpower PCN contains "RG" or "VT" = No (exclude these)
    // =====================================================
    AddIsStudentIntern = Table.AddColumn(FilledOrgDefaults, "IsStudentIntern", each 
        let
            TitleUpper = Text.Upper([Title] ?? ""),
            SeriesCode = [Occ Series Current] ?? "",
            ManpowerPCN = Text.Upper([Manpower PCN] ?? ""),
            
            // Check if title contains student or intern
            TitleIndicatesStudent = Text.Contains(TitleUpper, "STUDENT") or 
                                    Text.Contains(TitleUpper, "INTERN"),
            
            // Check if series ends with 99
            SeriesEnds99 = Text.EndsWith(SeriesCode, "99"),
            
            // Check if Recent Grad or Veteran Transition (should NOT be student)
            IsRecentGradOrVetTrans = Text.Contains(ManpowerPCN, "RG") or 
                                      Text.Contains(ManpowerPCN, "VT"),
            
            // Final determination
            Result = if IsRecentGradOrVetTrans then "No"
                     else if TitleIndicatesStudent or SeriesEnds99 then "Yes"
                     else "No"
        in
            Result,
        type text
    ),
    
    // =====================================================
    // STEP 12: ADD IsNewHire (within last 2 years of snapshot)
    // Per Attrition Guide
    // =====================================================
    AddIsNewHire = Table.AddColumn(AddIsStudentIntern, "IsNewHire", each 
        let
            ArrivalDateText = [Dt Arrived Personnel Office],
            ArrivalDate = try Date.FromText(ArrivalDateText) otherwise null,
            TwoYearsAgo = Date.AddYears([SnapshotDate], -2)
        in
            if ArrivalDate = null then "Unknown"
            else if ArrivalDate >= TwoYearsAgo then "Yes"
            else "No",
        type text
    ),
    
    // =====================================================
    // STEP 13: ADD IsProbationary (Tenure = 2)
    // =====================================================
    AddIsProbationary = Table.AddColumn(AddIsNewHire, "IsProbationary", each 
        if [Tenure] = "2" then "Yes" else "No",
        type text
    ),
    
    // =====================================================
    // STEP 14: CONVERT SALARY TO NUMBER
    // =====================================================
    ConvertedSalary = Table.TransformColumns(AddIsProbationary, {
        {"Total Pay Amt", each 
            try Number.FromText(Text.Replace(_ ?? "0", ",", "")) otherwise 0, 
            type number}
    }),
    
    // =====================================================
    // STEP 15: SET DATA TYPES
    // =====================================================
    FinalTypes = Table.TransformColumnTypes(ConvertedSalary, {
        {"SnapshotDate", type date},
        {"EmployeeKey", type text},
        {"OrgFirst2", type text},
        {"OrgCode", type text},
        {"Center", type text},
        {"SeriesCode", type text},
        {"IsStudentIntern", type text},
        {"IsNewHire", type text},
        {"IsProbationary", type text}
    }),
    
    // =====================================================
    // STEP 16: REMOVE BLANK EMPLOYEES
    // =====================================================
    RemovedBlanks = Table.SelectRows(FinalTypes, each 
        [EmployeeKey] <> null and [EmployeeKey] <> ""
    )
in
    RemovedBlanks
```

---

### 2.4 Query: `DimOrg`

```powerquery-m
// Query Name: DimOrg
// Purpose: Organization dimension

let
    Source = OrgMapping,
    DistinctOrgs = Table.Distinct(Source, {"Org Code"}),
    AddSortOrder = Table.AddIndexColumn(DistinctOrgs, "SortOrder", 1, 1, Int64.Type),
    RenamedColumns = Table.RenameColumns(AddSortOrder, {{"Org", "OrgFirst2"}}),
    FinalTypes = Table.TransformColumnTypes(RenamedColumns, {
        {"OrgFirst2", type text},
        {"Center", type text},
        {"Org Code", type text},
        {"CAM ID", type text},
        {"SortOrder", Int64.Type}
    })
in
    FinalTypes
```

---

### 2.5 Query: `DimSeries`

```powerquery-m
// Query Name: DimSeries
// Purpose: Job Series dimension

let
    Source = FactEmployeeSnapshot,
    SelectSeries = Table.SelectColumns(Source, {"SeriesCode"}),
    DistinctSeries = Table.Distinct(SelectSeries),
    FilteredSeries = Table.SelectRows(DistinctSeries, each 
        [SeriesCode] <> null and [SeriesCode] <> "" and [SeriesCode] <> "Unknown"
    ),
    AddSeriesName = Table.AddColumn(FilteredSeries, "SeriesName", each [SeriesCode], type text),
    Sorted = Table.Sort(AddSeriesName, {{"SeriesCode", Order.Ascending}}),
    AddUnknown = Table.InsertRows(Sorted, 0, {[SeriesCode = "Unknown", SeriesName = "Unknown"]}),
    FinalTypes = Table.TransformColumnTypes(AddUnknown, {
        {"SeriesCode", type text},
        {"SeriesName", type text}
    })
in
    FinalTypes
```

---

### 2.6 Query: `DimDate`

```powerquery-m
// Query Name: DimDate
// Purpose: Date dimension with fiscal year support

let
    StartDate = #date(2020, 1, 1),
    EndDate = #date(2030, 12, 31),
    NumberOfDays = Duration.Days(EndDate - StartDate) + 1,
    DateList = List.Dates(StartDate, NumberOfDays, #duration(1, 0, 0, 0)),
    DateTable = Table.FromList(DateList, Splitter.SplitByNothing(), {"Date"}, null, ExtraValues.Error),
    TypedDate = Table.TransformColumnTypes(DateTable, {{"Date", type date}}),
    AddYear = Table.AddColumn(TypedDate, "Year", each Date.Year([Date]), Int64.Type),
    AddMonth = Table.AddColumn(AddYear, "Month", each Date.Month([Date]), Int64.Type),
    AddMonthName = Table.AddColumn(AddMonth, "MonthName", each Date.MonthName([Date]), type text),
    AddQuarter = Table.AddColumn(AddMonthName, "Quarter", each Date.QuarterOfYear([Date]), Int64.Type),
    AddYearQuarter = Table.AddColumn(AddQuarter, "YearQuarter", each 
        Text.From([Year]) & "-Q" & Text.From([Quarter]), type text),
    
    // Federal Fiscal Year (Oct-Sep)
    AddFiscalYear = Table.AddColumn(AddYearQuarter, "FiscalYear", each 
        if [Month] >= 10 then [Year] + 1 else [Year], Int64.Type),
    AddFiscalYearText = Table.AddColumn(AddFiscalYear, "FiscalYearText", each 
        "FY" & Text.From([FiscalYear]), type text),
    
    // Mark Snapshot Dates
    SnapshotDates = List.Distinct(FactEmployeeSnapshot[SnapshotDate]),
    AddIsSnapshotDate = Table.AddColumn(AddFiscalYearText, "IsSnapshotDate", each 
        if List.Contains(SnapshotDates, [Date]) then "Yes" else "No", type text),
    AddSnapshotLabel = Table.AddColumn(AddIsSnapshotDate, "SnapshotLabel", each 
        if [IsSnapshotDate] = "Yes" 
        then Date.ToText([Date], "dd MMM yyyy")
        else null, type text)
in
    AddSnapshotLabel
```

---

### 2.7 Query: `DimPeriodStart` (Disconnected Slicer)

```powerquery-m
// Query Name: DimPeriodStart
// Purpose: Disconnected table for Period Start slicer
// NO RELATIONSHIPS to other tables

let
    Source = FactEmployeeSnapshot,
    SnapshotDates = Table.SelectColumns(Source, {"SnapshotDate"}),
    DistinctDates = Table.Distinct(SnapshotDates),
    FilteredDates = Table.SelectRows(DistinctDates, each [SnapshotDate] <> null),
    RenamedColumn = Table.RenameColumns(FilteredDates, {{"SnapshotDate", "PeriodStartDate"}}),
    AddLabel = Table.AddColumn(RenamedColumn, "PeriodStartLabel", each 
        Date.ToText([PeriodStartDate], "dd MMM yyyy"), type text),
    Sorted = Table.Sort(AddLabel, {{"PeriodStartDate", Order.Descending}}),
    FinalTypes = Table.TransformColumnTypes(Sorted, {
        {"PeriodStartDate", type date},
        {"PeriodStartLabel", type text}
    })
in
    FinalTypes
```

---

### 2.8 Query: `DimPeriodEnd` (Disconnected Slicer)

```powerquery-m
// Query Name: DimPeriodEnd
// Purpose: Disconnected table for Period End slicer
// NO RELATIONSHIPS to other tables

let
    Source = FactEmployeeSnapshot,
    SnapshotDates = Table.SelectColumns(Source, {"SnapshotDate"}),
    DistinctDates = Table.Distinct(SnapshotDates),
    FilteredDates = Table.SelectRows(DistinctDates, each [SnapshotDate] <> null),
    RenamedColumn = Table.RenameColumns(FilteredDates, {{"SnapshotDate", "PeriodEndDate"}}),
    AddLabel = Table.AddColumn(RenamedColumn, "PeriodEndLabel", each 
        Date.ToText([PeriodEndDate], "dd MMM yyyy"), type text),
    Sorted = Table.Sort(AddLabel, {{"PeriodEndDate", Order.Descending}}),
    FinalTypes = Table.TransformColumnTypes(Sorted, {
        {"PeriodEndDate", type date},
        {"PeriodEndLabel", type text}
    })
in
    FinalTypes
```

---

## 3. DAX MEASURES - ATTRITION & RETENTION FOCUSED

### 3.1 Group: PERIOD SELECTION

```dax
// =====================================================
// Selected Period Start Date
// =====================================================
Selected Period Start = 
SELECTEDVALUE(DimPeriodStart[PeriodStartDate], BLANK())


// =====================================================
// Selected Period End Date
// =====================================================
Selected Period End = 
SELECTEDVALUE(DimPeriodEnd[PeriodEndDate], BLANK())


// =====================================================
// Period Start Label (for display)
// =====================================================
Period Start Label = 
VAR StartDate = [Selected Period Start]
RETURN
IF(ISBLANK(StartDate), "Select Period Start", FORMAT(StartDate, "dd MMM yyyy"))


// =====================================================
// Period End Label (for display)
// =====================================================
Period End Label = 
VAR EndDate = [Selected Period End]
RETURN
IF(ISBLANK(EndDate), "Select Period End", FORMAT(EndDate, "dd MMM yyyy"))


// =====================================================
// Days in Period
// =====================================================
Days in Period = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
RETURN
IF(
    ISBLANK(StartDate) || ISBLANK(EndDate),
    BLANK(),
    DATEDIFF(StartDate, EndDate, DAY)
)


// =====================================================
// Selection Validation
// =====================================================
Selection Status = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
RETURN
SWITCH(
    TRUE(),
    ISBLANK(StartDate) && ISBLANK(EndDate), "⚠️ Select both dates",
    ISBLANK(StartDate), "⚠️ Select Period Start",
    ISBLANK(EndDate), "⚠️ Select Period End",
    StartDate = EndDate, "⚠️ Same date selected",
    StartDate > EndDate, "⚠️ Start date is after End date",
    "✓ Valid"
)


// =====================================================
// Is Valid Selection (1=Yes, 0=No)
// =====================================================
Is Valid Selection = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
RETURN
IF(
    NOT(ISBLANK(StartDate)) && 
    NOT(ISBLANK(EndDate)) && 
    StartDate < EndDate,
    1, 0
)
```

---

### 3.2 Group: BASE HEADCOUNT (Excluding Students/Interns)

```dax
// =====================================================
// Headcount at Period Start (excludes students/interns)
// This is the DENOMINATOR for retention
// =====================================================
Headcount Period Start = 
VAR StartDate = [Selected Period Start]
RETURN
IF(
    ISBLANK(StartDate),
    BLANK(),
    CALCULATE(
        DISTINCTCOUNT(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = StartDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
)


// =====================================================
// Headcount at Period End (excludes students/interns)
// =====================================================
Headcount Period End = 
VAR EndDate = [Selected Period End]
RETURN
IF(
    ISBLANK(EndDate),
    BLANK(),
    CALCULATE(
        DISTINCTCOUNT(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = EndDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
)


// =====================================================
// Average Headcount (for attrition denominator)
// Per Attrition Guide: (Start + End) / 2
// =====================================================
Average Headcount = 
VAR HC_Start = [Headcount Period Start]
VAR HC_End = [Headcount Period End]
RETURN
IF(
    ISBLANK(HC_Start) || ISBLANK(HC_End),
    BLANK(),
    (HC_Start + HC_End) / 2
)


// =====================================================
// Headcount Change (absolute)
// =====================================================
Headcount Change = 
VAR HC_Start = [Headcount Period Start]
VAR HC_End = [Headcount Period End]
RETURN
IF(ISBLANK(HC_Start) || ISBLANK(HC_End), BLANK(), HC_End - HC_Start)


// =====================================================
// Headcount Change %
// =====================================================
Headcount Change Pct = 
VAR HC_Start = [Headcount Period Start]
VAR HC_End = [Headcount Period End]
RETURN
IF(
    ISBLANK(HC_Start) || ISBLANK(HC_End) || HC_Start = 0,
    BLANK(),
    DIVIDE(HC_End - HC_Start, HC_Start, 0)
)
```

---

### 3.3 Group: RETENTION METRICS (Core)

```dax
// =====================================================
// Employees Present in Both Periods (Stayers)
// Present at Start AND Present at End (excl students)
// =====================================================
Employees Present Both Periods = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR StartEmployees = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = StartDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
VAR EndEmployees = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = EndDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
RETURN
IF(
    ISBLANK(StartDate) || ISBLANK(EndDate),
    BLANK(),
    COUNTROWS(INTERSECT(StartEmployees, EndEmployees))
)


// =====================================================
// RETENTION RATE - AGENCY LEVEL
// Per Guide: For agency-level, don't check Org/Center/Series move
// Formula: Present in Both / On-Hand at Start
// =====================================================
Retention Rate Agency = 
VAR Remaining = [Employees Present Both Periods]
VAR StartCount = [Headcount Period Start]
RETURN
IF(
    ISBLANK(Remaining) || ISBLANK(StartCount) || StartCount = 0,
    BLANK(),
    DIVIDE(Remaining, StartCount, 0)
)


// =====================================================
// EMPLOYEES WHO STAYED IN SAME ORG
// Present in both AND in same OrgCode at both times
// =====================================================
Employees Stayed Same Org = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR StartData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = StartDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "StartOrg", FactEmployeeSnapshot[OrgCode]
    )
VAR EndData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = EndDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "EndOrg", FactEmployeeSnapshot[OrgCode]
    )
VAR Joined = 
    NATURALINNERJOIN(StartData, EndData)
VAR SameOrg = 
    FILTER(Joined, [StartOrg] = [EndOrg])
RETURN
IF(
    ISBLANK(StartDate) || ISBLANK(EndDate),
    BLANK(),
    COUNTROWS(SameOrg)
)


// =====================================================
// RETENTION RATE - BY ORG
// Per Guide: Must be in same Org at both periods
// =====================================================
Retention Rate by Org = 
VAR StayedSameOrg = [Employees Stayed Same Org]
VAR StartCount = [Headcount Period Start]
RETURN
IF(
    ISBLANK(StayedSameOrg) || ISBLANK(StartCount) || StartCount = 0,
    BLANK(),
    DIVIDE(StayedSameOrg, StartCount, 0)
)


// =====================================================
// EMPLOYEES WHO STAYED IN SAME CENTER
// =====================================================
Employees Stayed Same Center = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR StartData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = StartDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "StartCenter", FactEmployeeSnapshot[Center]
    )
VAR EndData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = EndDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "EndCenter", FactEmployeeSnapshot[Center]
    )
VAR Joined = NATURALINNERJOIN(StartData, EndData)
VAR SameCenter = FILTER(Joined, [StartCenter] = [EndCenter])
RETURN
IF(ISBLANK(StartDate) || ISBLANK(EndDate), BLANK(), COUNTROWS(SameCenter))


// =====================================================
// RETENTION RATE - BY CENTER
// =====================================================
Retention Rate by Center = 
VAR StayedSameCenter = [Employees Stayed Same Center]
VAR StartCount = [Headcount Period Start]
RETURN
IF(
    ISBLANK(StayedSameCenter) || ISBLANK(StartCount) || StartCount = 0,
    BLANK(),
    DIVIDE(StayedSameCenter, StartCount, 0)
)


// =====================================================
// EMPLOYEES WHO STAYED IN SAME SERIES
// =====================================================
Employees Stayed Same Series = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR StartData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = StartDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "StartSeries", FactEmployeeSnapshot[SeriesCode]
    )
VAR EndData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = EndDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "EndSeries", FactEmployeeSnapshot[SeriesCode]
    )
VAR Joined = NATURALINNERJOIN(StartData, EndData)
VAR SameSeries = FILTER(Joined, [StartSeries] = [EndSeries])
RETURN
IF(ISBLANK(StartDate) || ISBLANK(EndDate), BLANK(), COUNTROWS(SameSeries))


// =====================================================
// RETENTION RATE - BY SERIES
// =====================================================
Retention Rate by Series = 
VAR StayedSameSeries = [Employees Stayed Same Series]
VAR StartCount = [Headcount Period Start]
RETURN
IF(
    ISBLANK(StayedSameSeries) || ISBLANK(StartCount) || StartCount = 0,
    BLANK(),
    DIVIDE(StayedSameSeries, StartCount, 0)
)
```

---

### 3.4 Group: ATTRITION METRICS (Snapshot-Based)

```dax
// =====================================================
// LOSSES (Departed Employees)
// Employees in Period Start but NOT in Period End
// These are the employees who left during the period
// =====================================================
Losses Total = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR StartEmployees = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = StartDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
VAR EndEmployees = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = EndDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
RETURN
IF(
    ISBLANK(StartDate) || ISBLANK(EndDate),
    BLANK(),
    COUNTROWS(EXCEPT(StartEmployees, EndEmployees))
)


// =====================================================
// ATTRITION RATE
// Per Guide: Losses / Average On-Hand * 100
// =====================================================
Attrition Rate = 
VAR Losses = [Losses Total]
VAR AvgHC = [Average Headcount]
RETURN
IF(
    ISBLANK(Losses) || ISBLANK(AvgHC) || AvgHC = 0,
    BLANK(),
    DIVIDE(Losses, AvgHC, 0)
)


// =====================================================
// NEW ARRIVALS (Gains)
// Employees in Period End but NOT in Period Start
// =====================================================
Gains Total = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR StartEmployees = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = StartDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
VAR EndEmployees = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = EndDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
RETURN
IF(
    ISBLANK(StartDate) || ISBLANK(EndDate),
    BLANK(),
    COUNTROWS(EXCEPT(EndEmployees, StartEmployees))
)


// =====================================================
// NET CHANGE (Gains - Losses)
// =====================================================
Net Change = 
VAR G = [Gains Total]
VAR L = [Losses Total]
RETURN
IF(ISBLANK(G) || ISBLANK(L), BLANK(), G - L)


// =====================================================
// TURNOVER RATE (Losses + Gains) / Average
// Measures total movement
// =====================================================
Turnover Rate = 
VAR Losses = [Losses Total]
VAR Gains = [Gains Total]
VAR AvgHC = [Average Headcount]
RETURN
IF(
    ISBLANK(Losses) || ISBLANK(Gains) || ISBLANK(AvgHC) || AvgHC = 0,
    BLANK(),
    DIVIDE(Losses + Gains, AvgHC, 0)
)
```

---

### 3.5 Group: INTERNAL MOVEMENT

```dax
// =====================================================
// EMPLOYEES WHO MOVED ORG (Internal Transfer)
// Present in both periods but different Org
// =====================================================
Employees Moved Org = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR StartData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = StartDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "StartOrg", FactEmployeeSnapshot[OrgCode]
    )
VAR EndData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = EndDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "EndOrg", FactEmployeeSnapshot[OrgCode]
    )
VAR Joined = NATURALINNERJOIN(StartData, EndData)
VAR MovedOrg = FILTER(Joined, [StartOrg] <> [EndOrg])
RETURN
IF(ISBLANK(StartDate) || ISBLANK(EndDate), BLANK(), COUNTROWS(MovedOrg))


// =====================================================
// INTERNAL MOVEMENT RATE (Org)
// =====================================================
Internal Movement Rate Org = 
VAR Moved = [Employees Moved Org]
VAR StartCount = [Headcount Period Start]
RETURN
IF(
    ISBLANK(Moved) || ISBLANK(StartCount) || StartCount = 0,
    BLANK(),
    DIVIDE(Moved, StartCount, 0)
)


// =====================================================
// EMPLOYEES WHO CHANGED SERIES
// =====================================================
Employees Changed Series = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR StartData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = StartDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "StartSeries", FactEmployeeSnapshot[SeriesCode]
    )
VAR EndData = 
    SELECTCOLUMNS(
        FILTER(
            FactEmployeeSnapshot,
            FactEmployeeSnapshot[SnapshotDate] = EndDate &&
            FactEmployeeSnapshot[IsStudentIntern] = "No"
        ),
        "EmpKey", FactEmployeeSnapshot[EmployeeKey],
        "EndSeries", FactEmployeeSnapshot[SeriesCode]
    )
VAR Joined = NATURALINNERJOIN(StartData, EndData)
VAR ChangedSeries = FILTER(Joined, [StartSeries] <> [EndSeries])
RETURN
IF(ISBLANK(StartDate) || ISBLANK(EndDate), BLANK(), COUNTROWS(ChangedSeries))


// =====================================================
// SERIES CHANGE RATE
// =====================================================
Series Change Rate = 
VAR Changed = [Employees Changed Series]
VAR StartCount = [Headcount Period Start]
RETURN
IF(
    ISBLANK(Changed) || ISBLANK(StartCount) || StartCount = 0,
    BLANK(),
    DIVIDE(Changed, StartCount, 0)
)
```

---

### 3.6 Group: NEW HIRE ATTRITION

```dax
// =====================================================
// NEW HIRE LOSSES
// New hires at Period Start who are not present at Period End
// =====================================================
New Hire Losses = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR NewHiresStart = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = StartDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No",
        FactEmployeeSnapshot[IsNewHire] = "Yes"
    )
VAR EndEmployees = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = EndDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
RETURN
IF(
    ISBLANK(StartDate) || ISBLANK(EndDate),
    BLANK(),
    COUNTROWS(EXCEPT(NewHiresStart, EndEmployees))
)


// =====================================================
// NEW HIRE COUNT AT PERIOD START
// =====================================================
New Hire Count Start = 
VAR StartDate = [Selected Period Start]
RETURN
IF(
    ISBLANK(StartDate),
    BLANK(),
    CALCULATE(
        DISTINCTCOUNT(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = StartDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No",
        FactEmployeeSnapshot[IsNewHire] = "Yes"
    )
)


// =====================================================
// NEW HIRE ATTRITION RATE
// Per Guide: New hire attrition from pool of new hires
// =====================================================
New Hire Attrition Rate = 
VAR Losses = [New Hire Losses]
VAR NewHireStart = [New Hire Count Start]
RETURN
IF(
    ISBLANK(Losses) || ISBLANK(NewHireStart) || NewHireStart = 0,
    BLANK(),
    DIVIDE(Losses, NewHireStart, 0)
)


// =====================================================
// NEW HIRE RETENTION RATE
// =====================================================
New Hire Retention Rate = 
VAR AttritionRate = [New Hire Attrition Rate]
RETURN
IF(ISBLANK(AttritionRate), BLANK(), 1 - AttritionRate)
```

---

### 3.7 Group: PROBATIONARY EMPLOYEE METRICS

```dax
// =====================================================
// PROBATIONARY LOSSES
// =====================================================
Probationary Losses = 
VAR StartDate = [Selected Period Start]
VAR EndDate = [Selected Period End]
VAR ProbStart = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = StartDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No",
        FactEmployeeSnapshot[IsProbationary] = "Yes"
    )
VAR EndEmployees = 
    CALCULATETABLE(
        VALUES(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = EndDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No"
    )
RETURN
IF(ISBLANK(StartDate) || ISBLANK(EndDate), BLANK(), COUNTROWS(EXCEPT(ProbStart, EndEmployees)))


// =====================================================
// PROBATIONARY COUNT AT START
// =====================================================
Probationary Count Start = 
VAR StartDate = [Selected Period Start]
RETURN
IF(
    ISBLANK(StartDate),
    BLANK(),
    CALCULATE(
        DISTINCTCOUNT(FactEmployeeSnapshot[EmployeeKey]),
        FactEmployeeSnapshot[SnapshotDate] = StartDate,
        FactEmployeeSnapshot[IsStudentIntern] = "No",
        FactEmployeeSnapshot[IsProbationary] = "Yes"
    )
)


// =====================================================
// PROBATIONARY ATTRITION RATE
// =====================================================
Probationary Attrition Rate = 
VAR Losses = [Probationary Losses]
VAR ProbCount = [Probationary Count Start]
RETURN
IF(ISBLANK(Losses) || ISBLANK(ProbCount) || ProbCount = 0, BLANK(), DIVIDE(Losses, ProbCount, 0))
```

---

### 3.8 Group: ANNUALIZED METRICS

```dax
// =====================================================
// ANNUALIZED ATTRITION RATE
// Adjusts rate to annual basis
// =====================================================
Annualized Attrition Rate = 
VAR AttritionRate = [Attrition Rate]
VAR DaysInPeriod = [Days in Period]
RETURN
IF(
    ISBLANK(AttritionRate) || ISBLANK(DaysInPeriod) || DaysInPeriod = 0,
    BLANK(),
    1 - POWER(1 - AttritionRate, 365 / DaysInPeriod)
)


// =====================================================
// ANNUALIZED RETENTION RATE
// =====================================================
Annualized Retention Rate = 
VAR RetentionRate = [Retention Rate Agency]
VAR DaysInPeriod = [Days in Period]
RETURN
IF(
    ISBLANK(RetentionRate) || ISBLANK(DaysInPeriod) || DaysInPeriod = 0,
    BLANK(),
    POWER(RetentionRate, 365 / DaysInPeriod)
)
```

---

### 3.9 Group: ADVANCED ANALYTICS

```dax
// =====================================================
// WORKFORCE STABILITY INDEX
// Composite score: Higher = more stable
// =====================================================
Workforce Stability Index = 
VAR Retention = [Retention Rate Agency]
VAR Attrition = [Attrition Rate]
VAR InternalMove = [Internal Movement Rate Org]
RETURN
IF(
    ISBLANK(Retention) || ISBLANK(Attrition),
    BLANK(),
    (Retention * 0.5) + 
    ((1 - Attrition) * 0.3) + 
    ((1 - COALESCE(InternalMove, 0)) * 0.2)
)


// =====================================================
// RETENTION RISK SCORE
// Lower is better (more risk = higher score)
// =====================================================
Retention Risk Score = 
VAR Attrition = [Attrition Rate]
VAR NewHireAttrition = [New Hire Attrition Rate]
VAR ProbAttrition = [Probationary Attrition Rate]
RETURN
IF(
    ISBLANK(Attrition),
    BLANK(),
    (COALESCE(Attrition, 0) * 0.4) + 
    (COALESCE(NewHireAttrition, 0) * 0.35) + 
    (COALESCE(ProbAttrition, 0) * 0.25)
)


// =====================================================
// EXPECTED LOSSES NEXT PERIOD
// Projects losses based on current attrition rate
// =====================================================
Expected Losses Next Period = 
VAR CurrentAttrition = [Attrition Rate]
VAR CurrentHC = [Headcount Period End]
RETURN
IF(
    ISBLANK(CurrentAttrition) || ISBLANK(CurrentHC),
    BLANK(),
    ROUND(CurrentHC * CurrentAttrition, 0)
)


// =====================================================
// BREAK-EVEN HIRES NEEDED
// Number of hires needed to maintain current headcount
// =====================================================
Break Even Hires Needed = 
VAR ExpectedLosses = [Expected Losses Next Period]
RETURN
IF(ISBLANK(ExpectedLosses), BLANK(), ExpectedLosses)


// =====================================================
// YEARS TO REPLACE WORKFORCE
// At current attrition, how long to replace entire workforce
// =====================================================
Years to Replace Workforce = 
VAR AnnualizedAttr = [Annualized Attrition Rate]
RETURN
IF(
    ISBLANK(AnnualizedAttr) || AnnualizedAttr = 0,
    BLANK(),
    DIVIDE(1, AnnualizedAttr, BLANK())
)
```

---

### 3.10 Group: FORMATTING & CONDITIONAL MEASURES

```dax
// =====================================================
// Retention Rate Color
// Green >= 90%, Yellow 80-90%, Red < 80%
// =====================================================
Retention Rate Color = 
VAR Rate = [Retention Rate Agency]
RETURN
SWITCH(
    TRUE(),
    ISBLANK(Rate), "#808080",
    Rate >= 0.9, "#00AA00",
    Rate >= 0.8, "#FFAA00",
    "#CC0000"
)


// =====================================================
// Attrition Rate Color
// Green <= 10%, Yellow 10-20%, Red > 20%
// =====================================================
Attrition Rate Color = 
VAR Rate = [Attrition Rate]
RETURN
SWITCH(
    TRUE(),
    ISBLANK(Rate), "#808080",
    Rate <= 0.1, "#00AA00",
    Rate <= 0.2, "#FFAA00",
    "#CC0000"
)


// =====================================================
// Trend Arrow
// =====================================================
Headcount Trend Arrow = 
VAR Change = [Headcount Change]
RETURN
SWITCH(
    TRUE(),
    ISBLANK(Change), "—",
    Change > 0, "▲",
    Change < 0, "▼",
    "—"
)


// =====================================================
// Retention vs Target Variance
// Assuming 90% target
// =====================================================
Retention vs Target = 
VAR Actual = [Retention Rate Agency]
VAR Target = 0.90
RETURN
IF(ISBLANK(Actual), BLANK(), Actual - Target)
```

---

## 4. STEP-BY-STEP BUILD INSTRUCTIONS

### Step 1: Prepare Your Files

1. **Create a folder** for snapshots: `C:\DISA_Data\Snapshots\`
2. **Place all snapshot files** in this folder with naming pattern: `DISA Empl Data dd mm yy.xlsx`
3. **Place OrgMAPPING.xlsx** in: `C:\DISA_Data\OrgMAPPING.xlsx`

### Step 2: Open Power BI Desktop

1. Open **Power BI Desktop**
2. **File > New**
3. **File > Save As**: `DISA_Attrition_Retention.pbix`

### Step 3: Create Queries in Power Query

1. Click **Home > Transform data** (opens Power Query Editor)

**For each query below:**
1. Click **Home > New Source > Blank Query**
2. Click **View > Advanced Editor**
3. Delete all existing text
4. Paste the complete M code
5. Click **Done**
6. Rename the query in the right panel under "Query Settings"

**Create queries in this order:**
1. `fnGetSnapshotDate`
2. `OrgMapping` (update file path first!)
3. `FactEmployeeSnapshot` (update folder path first!)
4. `DimOrg`
5. `DimSeries`
6. `DimDate`
7. `DimPeriodStart`
8. `DimPeriodEnd`

### Step 4: Close & Apply

1. Click **Home > Close & Apply**
2. Wait for data to load

### Step 5: Create Relationships

1. Click **Model** view (left sidebar, 3rd icon)
2. **Drag and drop to create:**
   - `FactEmployeeSnapshot[OrgCode]` → `DimOrg[Org Code]`
   - `FactEmployeeSnapshot[SnapshotDate]` → `DimDate[Date]`
   - `FactEmployeeSnapshot[SeriesCode]` → `DimSeries[SeriesCode]`

3. **DO NOT** connect `DimPeriodStart` or `DimPeriodEnd` to anything

### Step 6: Create Measures Table

1. Click **Report** view
2. **Home > Enter Data**
3. Create single column "Helper" with value "1"
4. Rename table to `_Measures`
5. **Load**
6. Right-click the Helper column > **Delete**

### Step 7: Create All DAX Measures

For each measure:
1. Click on `_Measures` table in Data pane
2. **Home > New Measure**
3. Paste the DAX code
4. Press **Enter**

### Step 8: Build Report Page

**Page 1: Attrition & Retention Dashboard**

1. **Add Text Box**: "DISA Attrition & Retention Dashboard"

2. **Add Slicers:**
   - Period Start: `DimPeriodStart[PeriodStartLabel]`
   - Period End: `DimPeriodEnd[PeriodEndLabel]`
   - Organization: `DimOrg[Org Code]`
   - Center: `DimOrg[Center]`
   - Series: `DimSeries[SeriesCode]`

3. **Add Cards:**
   - Selection Status
   - Headcount Period Start
   - Headcount Period End
   - Headcount Change

4. **Add KPI Row (Cards):**
   - Retention Rate Agency (format as %)
   - Attrition Rate (format as %)
   - Turnover Rate (format as %)

5. **Add Bar Chart:**
   - Y-axis: `DimOrg[Org Code]`
   - Values: `Retention Rate by Org`

6. **Add Table:**
   - Columns: Org Code, Center, Headcount Period Start, Headcount Period End, Losses Total, Retention Rate by Org

---

## 5. SUMMARY OF ALL OBJECTS

### Power Query Queries (8)
1. fnGetSnapshotDate
2. OrgMapping
3. FactEmployeeSnapshot
4. DimOrg
5. DimSeries
6. DimDate
7. DimPeriodStart
8. DimPeriodEnd

### Relationships (3)
1. FactEmployeeSnapshot[OrgCode] → DimOrg[Org Code]
2. FactEmployeeSnapshot[SnapshotDate] → DimDate[Date]
3. FactEmployeeSnapshot[SeriesCode] → DimSeries[SeriesCode]

### DAX Measures (40+)
Organized by groups:
- Period Selection (7)
- Base Headcount (5)
- Retention Metrics (9)
- Attrition Metrics (6)
- Internal Movement (4)
- New Hire Attrition (4)
- Probationary (3)
- Annualized (2)
- Advanced Analytics (5)
- Formatting (5)

---

This revised solution focuses specifically on **Attrition and Retention** metrics per your guides, with clearer **Period Start/Period End** terminology. Let me know if you need any adjustments or have questions about implementation!
