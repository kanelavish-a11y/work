# DISA Workforce Analytics Dashboard
## Complete Implementation Guide

**Version:** 1.0
**Last Updated:** October 2025
**Author:** Implementation Team

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Data Model Architecture](#2-data-model-architecture)
3. [Phase 1: Core Attrition & Retention Measures](#3-phase-1-core-attrition--retention-measures)
4. [Phase 2: High-Level Analysis Measures](#4-phase-2-high-level-analysis-measures)
   - 4.6 [Phase 3: Internal Movement & Sankey Optimization](#46-phase-3-internal-movement--sankey-optimization-14-measures--calculated-columns)
5. [Dashboard Design Specifications](#5-dashboard-design-specifications)
6. [Validation & Testing](#6-validation--testing)
7. [Implementation Roadmap](#7-implementation-roadmap)
8. [Presentation & Rollout Strategy](#8-presentation--rollout-strategy)
9. [Quick Reference](#9-quick-reference)

---

## 1. Executive Summary

### 1.1 Project Overview

The DISA Workforce Analytics Dashboard provides comprehensive workforce analytics focusing on attrition and retention patterns. This implementation follows a phased approach:

- **Phase 1:** Core attrition and retention analysis (19 measures)
- **Phase 2:** High-level analytics including demographics, trending, movement patterns, and predictive insights (45 measures)
- **Phase 3:** Internal movement analysis, transparency metrics, and Sankey optimization (14 measures + 3 calculated columns)

**Total Implementation:** 78 DAX measures + 3 calculated columns + 1 parameter across 12+ dashboard pages

### 1.2 Key Features

- Between-date analysis with automatic closest date snapping
- Voluntary vs Non-Voluntary attrition classification
- Student/Intern exclusion logic with PCN exceptions
- Tenure cohort analysis
- Risk scoring algorithms
- 12-month projections
- Movement pattern tracking
- Comprehensive demographic breakdowns
- **Internal transfer analysis with org-to-org flow tracking**
- **Top N Sankey optimization for cleaner visualizations**
- **Rate-based metrics for fair org comparison**
- **Transparency measures showing actual snapshot dates used**

### 1.3 Implementation Priorities

1. **FIRST:** Attrition & retention analysis (Phase 1)
2. **SECOND:** High-level analysis - demographics, trending (Phase 2)
3. **THIRD:** Internal movement & Sankey optimization (Phase 3)
4. **FOURTH:** Overview dashboard as first page

---

## 2. Data Model Architecture

### 2.1 Tables Overview

Your semantic model contains 5 primary tables:

#### **Employees Table**
- **Purpose:** Employee snapshot data at various points in time
- **Key Fields:**
  - `DoD ID (EDIPI)` - Unique employee identifier
  - `Snapshot Date` - Date of the snapshot
  - `Dt Arrived Personnel Office` - Arrival date for tenure calculation
  - `ORG First 2` - First 2 characters of org structure
  - `Center` - Mapped from OrgMAPPING
  - `Org` - Mapped from OrgMAPPING
  - `Student/Intern` - Flag for student/intern status
  - `Manpower PCN` - Used for RG/VT exception logic
  - `Occ Series Current` - Occupation series
  - `Year` - Snapshot year

#### **KW DISA Personnel Actions Table**
- **Purpose:** Personnel action records (hires, losses, movements)
- **Key Fields:**
  - `EFFECTIVE_DATE_4` - Effective date of action
  - `NOA` - Nature of Action code
  - `NOA Category` - Categorized as 300s, T352, 501, 570, 702, 721, CAO
  - `Vol/Non-Vol` - Voluntary or Non-Voluntary classification
  - `Student/Intern` - Flag for student/intern status
  - `Internal/External Movement` - "Internal" for 501, 570, 702, 721; "External" for 300s, T352, CAO
  - `From ORG Trim` / `To ORG Trim` - Org codes (first 2 chars)
  - `From Org` / `To Org` - Full org names (mapped)
  - `From Center` / `To Center` - Center names (mapped)
  - `Compare From/To Org` - "TRUE" if same org, "FALSE" if different or either is blank
  - `From OCC` - Source occupation series code
  - `To OCC` - Destination occupation series code
  - `Compare From/To Series` - "TRUE" if same series, "FALSE" if different
  - `Destination Agency` - Agency employee transferred to (for external movements)

#### **OrgMAPPING Table**
- **Purpose:** Maps org codes to center and org names
- **Key Fields:**
  - `Dcode` - 2-character org code
  - `Center` - Center name
  - `Org` - Organization name
  - `CAM ID` - CAM identifier

#### **Date_Actions Table (Calculated)**
- **Purpose:** Date dimension spanning all relevant dates
- **Key Fields:**
  - `Date` - Date value
  - `Year`, `Month`, `YearMonth`, `Quarter`, `FY`
- **Source:** Calculated table covering range from MIN to MAX of both Employees[Snapshot Date] and Personnel Actions[EFFECTIVE_DATE_4]

#### **Series Table**
- **Purpose:** Occupation series dimension for filtering and analysis
- **Key Fields:**
  - `Tittle` - Occupation series code (e.g., "0301", "2210")
- **Usage:** Used as slicer/filter for occupation series analysis
- **Cardinality:** One row per unique occupation series code

### 2.2 Relationships

**CRITICAL DESIGN DECISION:** No relationship exists between Employees and Personnel Actions tables.

**Why?** To prevent double-counting. Employees table provides denominators (on-hand counts), Personnel Actions table provides numerators (losses, hires). Using relationships would create incorrect many-to-many scenarios.

**Active Relationships:**
1. `Employees[ORG First 2]` → `OrgMAPPING[Dcode]`
2. `Employees[Snapshot Date]` → `Date_Actions[Date]`
3. `Personnel Actions[From ORG Trim]` → `OrgMAPPING[Dcode]`
4. `Personnel Actions[EFFECTIVE_DATE_4]` → `Date_Actions[Date]`
5. `Personnel Actions[From OCC]` → `Series[Tittle]`

**Inactive Relationships:**
- `Personnel Actions[To ORG Trim]` → `OrgMAPPING[Dcode]` (inactive by design - use USERELATIONSHIP for Internal Transfers In)
- `Personnel Actions[To OCC]` → `Series[Tittle]` (inactive by design - use USERELATIONSHIP when filtering by destination series)
- `Employees[Occ Series Current]` → `Series[Tittle]` (inactive by design - use USERELATIONSHIP when filtering current employee series)

### 2.3 Data Model Best Practices

1. **Use CALCULATE with explicit filters** when crossing from Employees to Personnel Actions
2. **Always filter out Student/Intern** using `ISBLANK(Student/Intern)` unless PCN contains "RG" or "VT"
3. **Use between-date logic** with Start Snapshot Date and End Snapshot Date
4. **Closest date snapping** - find nearest available snapshot when selected date has no data

### 2.4 Series Table & Filtering Strategy

**Why a Separate Series Table?**

The Series table acts as a **dimension table** that enables visual-level filtering by occupation series. Without it, you'd need multiple slicers (one for From OCC, one for To OCC, one for Employees) which would be confusing for users.

**How It Works:**

1. **User selects a series from Series table slicer** (e.g., "2210")
2. **Active relationship filters:** `Personnel Actions[From OCC]` automatically filters to "2210"
   - This affects measures like Total Losses, Voluntary Losses (people leaving FROM series 2210)
3. **Inactive relationships require USERELATIONSHIP:**
   - `Personnel Actions[To OCC]` - For measures tracking transfers TO a series
   - `Employees[Occ Series Current]` - For measures counting current employees IN a series

**When to Use Each Relationship:**

| Scenario | Relationship to Use | Example Measure |
|----------|-------------------|-----------------|
| **Employees currently in series** | `Employees[Occ Series Current]` → `Series[Tittle]` | OnHand End, New Hire On-Hand, Tenure measures |
| **Actions FROM a series** | `Personnel Actions[From OCC]` → `Series[Tittle]` (Active) | Total Losses, Attrition, Transfers Out |
| **Actions TO a series** | `Personnel Actions[To OCC]` → `Series[Tittle]` | Internal Transfers In (use USERELATIONSHIP) |

**Important Notes:**

- Most measures use the **active relationship** (`From OCC`) because they track losses/movements FROM a series
- Current employee measures need **USERELATIONSHIP** to activate the `Employees[Occ Series Current]` relationship
- Transfer IN measures need **USERELATIONSHIP** to activate the `To OCC` relationship

**Example: How Series Filter Affects OnHand End**

Currently, your OnHand End measure doesn't activate the Series relationship:

```dax
OnHand End =
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    DISTINCTCOUNT(Employees[DoD ID (EDIPI)]),
    Employees[Snapshot Date] = d1,
    Employees[Student/Intern] <> "Student/Intern"
)
```

**This measure won't respond to Series slicer** because the `Employees → Series` relationship is inactive.

**To make it work with Series filter, you'd need:**

```dax
OnHand End (Series Aware) =
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    DISTINCTCOUNT(Employees[DoD ID (EDIPI)]),
    Employees[Snapshot Date] = d1,
    Employees[Student/Intern] <> "Student/Intern",
    USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
)
```

**However**, you mentioned you're **"filtering for Org and Series"** at the visual level, which means:
- You're likely using the Series table as a slicer
- The active `Personnel Actions[From OCC] → Series[Tittle]` relationship handles most measures automatically
- Only measures that need to count current employees or transfers IN require USERELATIONSHIP

**Decision: Do you want all measures to respond to Series filter?**

If **YES**, I can update measures like OnHand End, New Hire On-Hand, and all tenure measures to use USERELATIONSHIP.

If **NO**, the current setup is correct - Series filter will only affect Personnel Actions measures (losses, transfers).

---

## 3. Phase 1: Core Attrition & Retention Measures

### 3.1 Foundation Measures (Already Complete)

These measures were implemented by the user and form the foundation for all other calculations:

#### **Date Slicer Measures** (Closest Date Snapping Logic)

```dax
StartDate Selected = MIN(Date_Actions[Date])

EndDate Selected = MAX(Date_Actions[Date])

_First Snapshot Date =
CALCULATE(MIN(Employees[Snapshot Date]), REMOVEFILTERS(Employees))

_Last Snapshot Date =
CALCULATE(MAX(Employees[Snapshot Date]), REMOVEFILTERS(Employees))

Start Snapshot Date =
VAR dSel = [StartDate Selected]
VAR Snap =
    CALCULATE(
        MAX(Employees[Snapshot Date]),
        FILTER(ALL(Employees[Snapshot Date]), Employees[Snapshot Date] <= dSel)
    )
RETURN COALESCE(Snap, [_First Snapshot Date])

End Snapshot Date =
VAR dSel = [EndDate Selected]
VAR Raw =
    CALCULATE(
        MIN(Employees[Snapshot Date]),
        FILTER(ALL(Employees[Snapshot Date]), Employees[Snapshot Date] >= dSel)
    )
VAR Cand = COALESCE(Raw, [_Last Snapshot Date])
VAR NextAfterStart =
    CALCULATE(
        MIN(Employees[Snapshot Date]),
        FILTER(ALL(Employees[Snapshot Date]), Employees[Snapshot Date] > [Start Snapshot Date])
    )
RETURN IF(Cand < [Start Snapshot Date], COALESCE(NextAfterStart, [Start Snapshot Date]), Cand)
```

**How it works:**
- User selects date range in Date_Actions slicer
- Start Snapshot Date finds closest snapshot **on or before** selected start
- End Snapshot Date finds closest snapshot **on or after** selected end
- Ensures End is always after Start
- Falls back to first/last available snapshot if no match

---

#### **On-Hand Count Measures** (Student/Intern Exclusion)

```dax
OnHand Start =
VAR d0 = [Start Snapshot Date]
RETURN
CALCULATE(
    DISTINCTCOUNT(Employees[DoD ID (EDIPI)]),
    Employees[Snapshot Date] = d0,
    Employees[Student/Intern] <> "Student/Intern",
    USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
)

OnHand End =
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    DISTINCTCOUNT(Employees[DoD ID (EDIPI)]),
    Employees[Snapshot Date] = d1,
    Employees[Student/Intern] <> "Student/Intern",
    USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
)

Average On-Hand =
DIVIDE([OnHand Start] + [OnHand End], 2)
```

**Note:** Uses `DISTINCTCOUNT(Employees[DoD ID (EDIPI)])` to ensure unique employee count. Includes `USERELATIONSHIP` to make measures respond to Series slicer.

---

#### **Additional Foundation Measures** (To be created if not already present)

```dax
Total Hires =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Internal/External] = "External",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern"
)

Adjusted Total Hires =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR AllHires = [Total Hires]
VAR RGVTStudentHires =
    CALCULATE(
        COUNTROWS('KW DISA Personnel Actions'),
        'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
        'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
        'KW DISA Personnel Actions'[Internal/External] = "External",
        'KW DISA Personnel Actions'[Student/Intern] = "Student/Intern",
        (
            CONTAINSSTRING('KW DISA Personnel Actions'[MOST_RECENT_EMPL_MANPOWER_PCN], "RG") ||
            CONTAINSSTRING('KW DISA Personnel Actions'[MOST_RECENT_EMPL_MANPOWER_PCN], "VT")
        )
    )
RETURN AllHires + RGVTStudentHires
```

**Important:** All subsequent measures use `[Start Snapshot Date]` and `[End Snapshot Date]` to leverage your date snapping logic.

### 3.2 Phase 1 Additional Measures (19 Measures)

Create these measures in the **_Measures** table (note the underscore prefix).

#### 3.2.1 Net Headcount Change

```dax
Net Headcount Change = [OnHand End] - [OnHand Start]
```

**Purpose:** Shows the overall change in workforce size between two periods.

---

#### 3.2.2 Total Losses

```dax
Total Losses =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern"
)
```

**Purpose:** Counts all losses (where Compare From/To Org = "FALSE"). This includes:
- External separations (300s NOAs)
- Transfers to other orgs (501, 570, 702, 721)
- Cases where BOTH From Org and To Org are blank (treated as "FALSE" per logic)

**Note:** Uses `<> "Student/Intern"` to match your foundation measure pattern.

---

#### 3.2.3 Adjusted Total Losses

```dax
Adjusted Total Losses =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR AllLosses = [Total Losses]
VAR RGVTStudentLosses =
    CALCULATE(
        COUNTROWS('KW DISA Personnel Actions'),
        'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
        'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
        'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
        'KW DISA Personnel Actions'[Student/Intern] = "Student/Intern",
        (
            CONTAINSSTRING('KW DISA Personnel Actions'[MOST_RECENT_EMPL_MANPOWER_PCN], "RG") ||
            CONTAINSSTRING('KW DISA Personnel Actions'[MOST_RECENT_EMPL_MANPOWER_PCN], "VT")
        )
    )
RETURN AllLosses + RGVTStudentLosses
```

**Purpose:** Includes RG/VT students/interns who would otherwise be excluded.

---

#### 3.2.4 Voluntary Losses

```dax
Voluntary Losses =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
    'KW DISA Personnel Actions'[Vol/Non-Vol] = "Voluntary",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern"
)
```

**Purpose:** Counts losses classified as Voluntary (all NOAs except 330, 355, 357, 385).

---

#### 3.2.5 Non-Voluntary Losses

```dax
Non-Voluntary Losses =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
    'KW DISA Personnel Actions'[Vol/Non-Vol] = "Non-Voluntary",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern"
)
```

**Purpose:** Counts losses classified as Non-Voluntary (NOAs 330, 355, 357, 385).

---

#### 3.2.6 Agency Losses

```dax
Agency Losses =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern",
    'KW DISA Personnel Actions'[Internal/External Movement] = "External"
)
```

**Purpose:** Agency-level attrition (employees who left DISA entirely). Uses only External movements (300s, T352, CAO NOA codes). Excludes internal transfers (501, 570, 702, 721).

**Note:** Based on attrition.txt guidance - "When calculating attrition at the Agency level (i.e., attrition for all of DISA) we only use the 300s, T352, and CAO NOA codes."

---

#### 3.2.7 Agency Attrition Rate

```dax
Agency Attrition Rate =
DIVIDE([Agency Losses], [Average On-Hand], 0)
```

**Format:** Percentage with 1 decimal place (e.g., 8.5%)

**Purpose:** True attrition rate at agency level (external departures only). Lower than Total Attrition since it excludes internal org transfers.

---

#### 3.2.8 Attrition Rate

```dax
Attrition Rate =
DIVIDE([Total Losses], [Average On-Hand], 0)
```

**Format:** Percentage with 1 decimal place (e.g., 13.2%)

**Purpose:** Primary attrition metric = Total Losses / Average On-Hand Count

---

#### 3.2.7 Voluntary Attrition Rate

```dax
Voluntary Attrition Rate =
DIVIDE([Voluntary Losses], [Average On-Hand], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Shows what portion of attrition is voluntary.

---

#### 3.2.8 Non-Voluntary Attrition Rate

```dax
Non-Voluntary Attrition Rate =
DIVIDE([Non-Voluntary Losses], [Average On-Hand], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Shows what portion of attrition is non-voluntary.

---

#### 3.2.9 Remain Count

```dax
Remain Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR StartEDIPIs =
    CALCULATETABLE(
        VALUES(Employees[DoD ID (EDIPI)]),
        FILTER(
            ALL(Employees),
            Employees[Snapshot Date] = d0 &&
            Employees[Student/Intern] <> "Student/Intern"
        ),
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR EndEDIPIs =
    CALCULATETABLE(
        VALUES(Employees[DoD ID (EDIPI)]),
        FILTER(
            ALL(Employees),
            Employees[Snapshot Date] = d1 &&
            Employees[Student/Intern] <> "Student/Intern"
        ),
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
RETURN COUNTROWS(INTERSECT(StartEDIPIs, EndEDIPIs))
```

**Purpose:** Counts employees present at BOTH start and end periods (retention calculation).

---

#### 3.2.10 Remain Same Org Count

```dax
Remain Same Org Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR StartData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Org]
        ),
        Employees[Snapshot Date] = d0,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR EndData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Org]
        ),
        Employees[Snapshot Date] = d1,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR Matched =
    COUNTROWS(INTERSECT(StartData, EndData))
RETURN Matched
```

**Purpose:** Counts employees present at both periods AND in the same org (stricter retention).

---

#### 3.2.11 Remain Same Center Count

```dax
Remain Same Center Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR StartData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Center]
        ),
        Employees[Snapshot Date] = d0,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR EndData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Center]
        ),
        Employees[Snapshot Date] = d1,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR Matched =
    COUNTROWS(INTERSECT(StartData, EndData))
RETURN Matched
```

**Purpose:** Counts employees present at both periods AND in the same center.

---

#### 3.2.12 Remain Same Series Count

```dax
Remain Same Series Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR StartData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Occ Series Current]
        ),
        Employees[Snapshot Date] = d0,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR EndData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Occ Series Current]
        ),
        Employees[Snapshot Date] = d1,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR Matched =
    COUNTROWS(INTERSECT(StartData, EndData))
RETURN Matched
```

**Purpose:** Counts employees present at both periods AND in the same occupation series.

---

#### 3.2.13 Remain Same Center Same Series Count

```dax
Remain Same Center Same Series Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR StartData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Center],
            Employees[Occ Series Current]
        ),
        Employees[Snapshot Date] = d0,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR EndData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Center],
            Employees[Occ Series Current]
        ),
        Employees[Snapshot Date] = d1,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR Matched =
    COUNTROWS(INTERSECT(StartData, EndData))
RETURN Matched
```

**Purpose:** Strictest retention metric - same center AND same occupation series.

---

#### 3.2.12 Retention Rate

```dax
Retention Rate =
DIVIDE([Remain Count], [OnHand Start], 0)
```

**Format:** Percentage with 1 decimal place (e.g., 86.8%)

**Purpose:** Primary retention metric = Remain Count / OnHand Start

---

#### 3.2.13 Retention Rate (Same Org)

```dax
Retention Rate (Same Org) =
DIVIDE([Remain Same Org Count], [OnHand Start], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Retention rate for employees who stayed in same org.

---

#### 3.2.14 Retention Rate (Same Center)

```dax
Retention Rate (Same Center) =
DIVIDE([Remain Same Center Count], [OnHand Start], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Retention rate for employees who stayed in same center.

---

#### 3.2.15 Retention Rate (Same Series)

```dax
Retention Rate (Same Series) =
DIVIDE([Remain Same Series Count], [OnHand Start], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Retention rate for employees who stayed in same occupation series.

---

#### 3.2.16 Retention Rate (Same Center Same Series)

```dax
Retention Rate (Same Center Same Series) =
DIVIDE([Remain Same Center Same Series Count], [OnHand Start], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Strictest retention rate metric.

---

#### 3.2.15 Internal Movement Count

```dax
Internal Movement Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Internal/External] = "Internal",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern"
)
```

**Purpose:** Counts internal movements (NOAs 501, 570, 702, 721 where From Org = To Org).

---

#### 3.2.16 Lost from Start Org Count

```dax
Lost from Start Org Count =
[OnHand Start] - [Remain Same Org Count]
```

**Purpose:** Employees who were in the org at start but not at end (includes losses and transfers out).

---

#### 3.2.17 Attrition Rate % of Total

```dax
Attrition Rate % of Total =
VAR CurrentRate = [Attrition Rate]
VAR TotalAttrition =
    CALCULATE(
        [Attrition Rate],
        REMOVEFILTERS(Employees[Org]),
        REMOVEFILTERS(Employees[Center]),
        REMOVEFILTERS(OrgMAPPING)
    )
RETURN DIVIDE(CurrentRate, TotalAttrition, 0)
```

**Format:** Percentage with 0 decimal places (e.g., 125%)

**Purpose:** Shows how current slice's attrition compares to overall (>100% = worse than average).

---

#### 3.2.18 Voluntary % of Total Losses

```dax
Voluntary % of Total Losses =
DIVIDE([Voluntary Losses], [Total Losses], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Shows what portion of losses are voluntary.

---

#### 3.2.19 Non-Voluntary % of Total Losses

```dax
Non-Voluntary % of Total Losses =
DIVIDE([Non-Voluntary Losses], [Total Losses], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Shows what portion of losses are non-voluntary. Should sum to 100% with Voluntary %.

---

### 3.3 Phase 1 Validation Checklist

After creating all Phase 1 measures, verify:

- [ ] All 19 measures created in `_Measures` table
- [ ] Headcount balance: `OnHand End = OnHand Start - Losses + Hires`
- [ ] Voluntary + Non-Voluntary Losses = Total Losses (exactly)
- [ ] Attrition Rate + Retention Rate ≈ 100% (within rounding)
- [ ] Voluntary % + Non-Voluntary % = 100% (exactly)
- [ ] Student/Intern exclusion working (except RG/VT PCNs)

---

## 4. Phase 2: High-Level Analysis Measures

Phase 2 adds 45 measures across 4 categories:
1. Demographics & Tenure (15 measures)
2. Trending & Comparison (12 measures)
3. Movement & Career Patterns (8 measures)
4. Predictive & Risk Analysis (10 measures)

### 4.1 Demographics & Tenure (15 Measures)

#### 4.1.1 Avg Tenure (Current)

```dax
Avg Tenure (Current) =
VAR d1 = [End Snapshot Date]
VAR AvgTenureDays =
    CALCULATE(
        AVERAGEX(
            Employees,
            d1 - Employees[Dt Arrived Personnel Office]
        ),
        Employees[Snapshot Date] = d1,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
RETURN DIVIDE(AvgTenureDays, 365.25, 0)
```

**Format:** Number with 1 decimal place (years)

**Purpose:** Average years of service for current workforce.

---

#### 4.1.2 Avg Tenure (Start)

```dax
Avg Tenure (Start) =
VAR d0 = [Start Snapshot Date]
VAR AvgTenureDays =
    CALCULATE(
        AVERAGEX(
            Employees,
            d0 - Employees[Dt Arrived Personnel Office]
        ),
        Employees[Snapshot Date] = d0,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
RETURN DIVIDE(AvgTenureDays, 365.25, 0)
```

**Format:** Number with 1 decimal place (years)

**Purpose:** Average years of service at start period.

---

#### 4.1.3 Tenure Change (Years)

```dax
Tenure Change (Years) =
[Avg Tenure (Current)] - [Avg Tenure (Start)]
```

**Format:** Number with 2 decimal places, conditional formatting (green if positive, red if negative)

**Purpose:** Shows whether workforce is getting more or less experienced.

---

#### 4.1.4 New Hire On-Hand

```dax
New Hire On-Hand =
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS(Employees),
    Employees[Snapshot Date] = d1,
    Employees[Student/Intern] <> "Student/Intern",
    (d1 - Employees[Dt Arrived Personnel Office]) < 730.5,
    USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
)
```

**Purpose:** Count of employees with less than 2 years tenure (730.5 days).

---

#### 4.1.5 New Hire %

```dax
New Hire % =
DIVIDE([New Hire On-Hand], [OnHand End], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Percentage of workforce that are new hires (<2 years).

---

#### 4.1.6 Tenure 0-2 Years

```dax
Tenure 0-2 Years =
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS(Employees),
    Employees[Snapshot Date] = d1,
    Employees[Student/Intern] <> "Student/Intern",
    (d1 - Employees[Dt Arrived Personnel Office]) < 730.5,
    USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
)
```

**Purpose:** Same as New Hire On-Hand (cohort definition).

---

#### 4.1.7 Tenure 2-5 Years

```dax
Tenure 2-5 Years =
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS(Employees),
    Employees[Snapshot Date] = d1,
    Employees[Student/Intern] <> "Student/Intern",
    (d1 - Employees[Dt Arrived Personnel Office]) >= 730.5,
    (d1 - Employees[Dt Arrived Personnel Office]) < 1826.25,
    USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
)
```

**Purpose:** Employees with 2 to <5 years tenure.

---

#### 4.1.8 Tenure 5-10 Years

```dax
Tenure 5-10 Years =
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS(Employees),
    Employees[Snapshot Date] = d1,
    Employees[Student/Intern] <> "Student/Intern",
    (d1 - Employees[Dt Arrived Personnel Office]) >= 1826.25,
    (d1 - Employees[Dt Arrived Personnel Office]) < 3652.5,
    USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
)
```

**Purpose:** Employees with 5 to <10 years tenure.

---

#### 4.1.9 Tenure 10-20 Years

```dax
Tenure 10-20 Years =
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS(Employees),
    Employees[Snapshot Date] = d1,
    Employees[Student/Intern] <> "Student/Intern",
    (d1 - Employees[Dt Arrived Personnel Office]) >= 3652.5,
    (d1 - Employees[Dt Arrived Personnel Office]) < 7305,
    USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
)
```

**Purpose:** Employees with 10 to <20 years tenure.

---

#### 4.1.10 Tenure 20+ Years

```dax
Tenure 20+ Years =
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS(Employees),
    Employees[Snapshot Date] = d1,
    Employees[Student/Intern] <> "Student/Intern",
    (d1 - Employees[Dt Arrived Personnel Office]) >= 7305,
    USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
)
```

**Purpose:** Employees with 20+ years tenure.

---

#### 4.1.11 New Hire Losses

```dax
New Hire Losses =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern",
    (
        'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] -
        'KW DISA Personnel Actions'[DT_ARR_SVCG_CCPO]
    ) < 730.5
)
```

**Purpose:** Losses of employees with <2 years tenure at time of loss.

---

#### 4.1.12 New Hire Attrition Rate

```dax
New Hire Attrition Rate =
VAR NewHireStart =
    VAR d0 = [Start Snapshot Date]
    RETURN
    CALCULATE(
        COUNTROWS(Employees),
        Employees[Snapshot Date] = d0,
        Employees[Student/Intern] <> "Student/Intern",
        (d0 - Employees[Dt Arrived Personnel Office]) < 730.5,
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR NewHireEnd = [New Hire On-Hand]
VAR AvgNewHire = DIVIDE(NewHireStart + NewHireEnd, 2)
RETURN DIVIDE([New Hire Losses], AvgNewHire, 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Attrition rate specifically for new hires (<2 years).

---

#### 4.1.13 Tenure 0-2 Years %

```dax
Tenure 0-2 Years % =
DIVIDE([Tenure 0-2 Years], [OnHand End], 0)
```

**Format:** Percentage with 1 decimal place

---

#### 4.1.14 Tenure 2-5 Years %

```dax
Tenure 2-5 Years % =
DIVIDE([Tenure 2-5 Years], [OnHand End], 0)
```

**Format:** Percentage with 1 decimal place

---

#### 4.1.15 Tenure 5-10 Years %

```dax
Tenure 5-10 Years % =
DIVIDE([Tenure 5-10 Years], [OnHand End], 0)
```

**Format:** Percentage with 1 decimal place

---

### 4.2 Trending & Comparison (12 Measures)

#### 4.2.1 Prior Period Attrition Rate

```dax
Prior Period Attrition Rate =
VAR CurrentEndDate = [End Snapshot Date]
VAR AllDates =
    CALCULATETABLE(
        VALUES(Employees[Snapshot Date]),
        REMOVEFILTERS(Date_Actions)
    )
VAR PriorDate =
    MAXX(
        FILTER(
            AllDates,
            Employees[Snapshot Date] < CurrentEndDate
        ),
        Employees[Snapshot Date]
    )
VAR PriorPriorDate =
    MAXX(
        FILTER(
            AllDates,
            Employees[Snapshot Date] < PriorDate
        ),
        Employees[Snapshot Date]
    )
RETURN
CALCULATE(
    [Attrition Rate],
    FILTER(
        ALL(Date_Actions),
        Date_Actions[Date] >= PriorPriorDate &&
        Date_Actions[Date] <= PriorDate
    )
)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Attrition rate from the previous period for comparison.

---

#### 4.2.2 Attrition Rate Change (pp)

```dax
Attrition Rate Change (pp) =
[Attrition Rate] - [Prior Period Attrition Rate]
```

**Format:** Percentage with 1 decimal place, conditional formatting

**Purpose:** Period-over-period change in attrition rate (percentage points).

---

#### 4.2.3 Attrition Rate Change %

```dax
Attrition Rate Change % =
DIVIDE(
    [Attrition Rate] - [Prior Period Attrition Rate],
    [Prior Period Attrition Rate],
    0
)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Relative change (e.g., "15% increase").

---

#### 4.2.4 Prior Period Retention Rate

```dax
Prior Period Retention Rate =
VAR CurrentEndDate = [End Snapshot Date]
VAR AllDates =
    CALCULATETABLE(
        VALUES(Employees[Snapshot Date]),
        REMOVEFILTERS(Date_Actions)
    )
VAR PriorDate =
    MAXX(
        FILTER(
            AllDates,
            Employees[Snapshot Date] < CurrentEndDate
        ),
        Employees[Snapshot Date]
    )
VAR PriorPriorDate =
    MAXX(
        FILTER(
            AllDates,
            Employees[Snapshot Date] < PriorDate
        ),
        Employees[Snapshot Date]
    )
RETURN
CALCULATE(
    [Retention Rate],
    FILTER(
        ALL(Date_Actions),
        Date_Actions[Date] >= PriorPriorDate &&
        Date_Actions[Date] <= PriorDate
    )
)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Retention rate from previous period.

---

#### 4.2.5 Retention Rate Change (pp)

```dax
Retention Rate Change (pp) =
[Retention Rate] - [Prior Period Retention Rate]
```

**Format:** Percentage with 1 decimal place, conditional formatting

**Purpose:** Period-over-period change in retention rate.

---

#### 4.2.6 YoY Attrition Rate

```dax
YoY Attrition Rate =
VAR CurrentYear = YEAR([End Snapshot Date])
VAR PriorYear = CurrentYear - 1
RETURN
CALCULATE(
    [Attrition Rate],
    FILTER(
        ALL(Date_Actions),
        YEAR(Date_Actions[Date]) = PriorYear
    )
)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Attrition rate from same period last year.

---

#### 4.2.7 YoY Attrition Change (pp)

```dax
YoY Attrition Change (pp) =
[Attrition Rate] - [YoY Attrition Rate]
```

**Format:** Percentage with 1 decimal place, conditional formatting

**Purpose:** Year-over-year comparison.

---

#### 4.2.8 Rolling 3-Period Avg Attrition

```dax
Rolling 3-Period Avg Attrition =
VAR CurrentEndDate = [End Snapshot Date]
VAR AllDates =
    CALCULATETABLE(
        VALUES(Employees[Snapshot Date]),
        REMOVEFILTERS(Date_Actions)
    )
VAR Last3Dates =
    TOPN(
        3,
        FILTER(
            AllDates,
            Employees[Snapshot Date] <= CurrentEndDate
        ),
        Employees[Snapshot Date],
        DESC
    )
RETURN
AVERAGEX(
    Last3Dates,
    CALCULATE(
        [Attrition Rate],
        FILTER(
            ALL(Date_Actions),
            Date_Actions[Date] = Employees[Snapshot Date]
        )
    )
)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Smooths short-term volatility, shows trend.

---

#### 4.2.9 Rolling 6-Period Avg Attrition

```dax
Rolling 6-Period Avg Attrition =
VAR CurrentEndDate = [End Snapshot Date]
VAR AllDates =
    CALCULATETABLE(
        VALUES(Employees[Snapshot Date]),
        REMOVEFILTERS(Date_Actions)
    )
VAR Last6Dates =
    TOPN(
        6,
        FILTER(
            AllDates,
            Employees[Snapshot Date] <= CurrentEndDate
        ),
        Employees[Snapshot Date],
        DESC
    )
RETURN
AVERAGEX(
    Last6Dates,
    CALCULATE(
        [Attrition Rate],
        FILTER(
            ALL(Date_Actions),
            Date_Actions[Date] = Employees[Snapshot Date]
        )
    )
)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Longer-term trend smoothing.

---

#### 4.2.10 Attrition Velocity

```dax
Attrition Velocity =
[Attrition Rate Change (pp)]
```

**Format:** Percentage with 2 decimal places, show +/- sign

**Purpose:** Rate of change (first derivative). Positive = accelerating attrition.

---

#### 4.2.11 Attrition Acceleration

```dax
Attrition Acceleration =
VAR CurrentChange = [Attrition Rate Change (pp)]
VAR PriorChange =
    VAR CurrentEndDate = [End Snapshot Date]
    VAR AllDates =
        CALCULATETABLE(
            VALUES(Employees[Snapshot Date]),
            REMOVEFILTERS(Date_Actions)
        )
    VAR PriorDate =
        MAXX(
            FILTER(
                AllDates,
                Employees[Snapshot Date] < CurrentEndDate
            ),
            Employees[Snapshot Date]
        )
    RETURN
    CALCULATE(
        [Attrition Rate Change (pp)],
        FILTER(
            ALL(Date_Actions),
            Date_Actions[Date] = PriorDate
        )
    )
RETURN CurrentChange - PriorChange
```

**Format:** Percentage with 2 decimal places, show +/- sign

**Purpose:** Change in velocity (second derivative). Detects inflection points.

---

#### 4.2.12 Benchmark Gap

```dax
Benchmark Gap =
VAR CurrentRate = [Attrition Rate]
VAR BenchmarkRate = 0.12
RETURN CurrentRate - BenchmarkRate
```

**Format:** Percentage with 1 decimal place, conditional formatting

**Purpose:** Difference from 12% benchmark. Adjust benchmark value as needed.

---

### 4.3 Movement & Career Patterns (8 Measures)

#### 4.3.1 Promotion Count

```dax
Promotion Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR StartData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            "StartGrade", Employees[Grade or Level]
        ),
        Employees[Snapshot Date] = d0,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR EndData =
    CALCULATETABLE(
        ADDCOLUMNS(
            SUMMARIZE(
                Employees,
                Employees[DoD ID (EDIPI)],
                "EndGrade", Employees[Grade or Level]
            ),
            "StartGrade",
            VAR CurrentEDIPI = Employees[DoD ID (EDIPI)]
            RETURN
            MAXX(
                FILTER(
                    StartData,
                    [DoD ID (EDIPI)] = CurrentEDIPI
                ),
                [StartGrade]
            )
        ),
        Employees[Snapshot Date] = d1,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR Promotions =
    COUNTROWS(
        FILTER(
            EndData,
            NOT(ISBLANK([StartGrade])) &&
            NOT(ISBLANK([EndGrade])) &&
            VALUE([EndGrade]) > VALUE([StartGrade])
        )
    )
RETURN Promotions
```

**Purpose:** Count of employees promoted between periods (grade increase).

---

#### 4.3.2 Promotion Rate

```dax
Promotion Rate =
DIVIDE([Promotion Count], [Remain Count], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** % of retained employees who were promoted.

---

#### 4.3.3 Lateral Move Count

```dax
Lateral Move Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR StartData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Occ Series Current]
        ),
        Employees[Snapshot Date] = d0,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR EndData =
    CALCULATETABLE(
        SUMMARIZE(
            Employees,
            Employees[DoD ID (EDIPI)],
            Employees[Occ Series Current]
        ),
        Employees[Snapshot Date] = d1,
        Employees[Student/Intern] <> "Student/Intern",
        USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
    )
VAR RemainedEDIPIs =
    VALUES(Employees[DoD ID (EDIPI)])
VAR LateralMoves =
    COUNTROWS(
        FILTER(
            RemainedEDIPIs,
            VAR CurrentEDIPI = Employees[DoD ID (EDIPI)]
            VAR StartSeries =
                MAXX(
                    FILTER(
                        StartData,
                        [DoD ID (EDIPI)] = CurrentEDIPI
                    ),
                    [Occ Series Current]
                )
            VAR EndSeries =
                MAXX(
                    FILTER(
                        EndData,
                        [DoD ID (EDIPI)] = CurrentEDIPI
                    ),
                    [Occ Series Current]
                )
            RETURN
            NOT(ISBLANK(StartSeries)) &&
            NOT(ISBLANK(EndSeries)) &&
            StartSeries <> EndSeries
        )
    )
RETURN LateralMoves
```

**Purpose:** Count of employees who changed occupation series (career change).

---

#### 4.3.4 Lateral Move Rate

```dax
Lateral Move Rate =
DIVIDE([Lateral Move Count], [Remain Count], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** % of retained employees who made lateral career moves.

---

#### 4.3.5 Internal Mobility Rate

```dax
Internal Mobility Rate =
DIVIDE([Internal Movement Count], [OnHand Start], 0)
```

**Format:** Percentage with 1 decimal place

**Purpose:** % of workforce making internal moves (NOAs 501, 570, 702, 721).

---

#### 4.3.6 Transfer In Count

```dax
Transfer In Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR SelectedOrg = SELECTEDVALUE(Employees[Org])
RETURN
IF(
    ISBLANK(SelectedOrg),
    BLANK(),
    CALCULATE(
        COUNTROWS('KW DISA Personnel Actions'),
        'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
        'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
        'KW DISA Personnel Actions'[To Org] = SelectedOrg,
        'KW DISA Personnel Actions'[From Org] <> SelectedOrg,
        'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern"
    )
)
```

**Purpose:** Count of employees transferring INTO the selected org.

---

#### 4.3.7 Transfer Out Count

```dax
Transfer Out Count =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
VAR SelectedOrg = SELECTEDVALUE(Employees[Org])
RETURN
IF(
    ISBLANK(SelectedOrg),
    BLANK(),
    CALCULATE(
        COUNTROWS('KW DISA Personnel Actions'),
        'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
        'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
        'KW DISA Personnel Actions'[From Org] = SelectedOrg,
        'KW DISA Personnel Actions'[To Org] <> SelectedOrg,
        'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern"
    )
)
```

**Purpose:** Count of employees transferring OUT of the selected org.

---

#### 4.3.8 Net Transfer

```dax
Net Transfer =
[Transfer In Count] - [Transfer Out Count]
```

**Format:** Whole number with +/- sign, conditional formatting

**Purpose:** Net gain/loss from internal transfers.

---

### 4.4 Predictive & Risk Analysis (10 Measures)

#### 4.4.1 Projected 12mo Losses

```dax
Projected 12mo Losses =
VAR CurrentRate = [Attrition Rate]
VAR CurrentOnHand = [OnHand End]
VAR ProjectedLosses = CurrentOnHand * CurrentRate
RETURN ProjectedLosses
```

**Format:** Whole number

**Purpose:** Estimate losses over next 12 months assuming current rate continues.

---

#### 4.4.2 Projected 12mo Headcount

```dax
Projected 12mo Headcount =
VAR CurrentOnHand = [OnHand End]
VAR ProjectedLosses = [Projected 12mo Losses]
VAR ProjectedHires =
    VAR HistoricalHires = [Adjusted Total Hires]
    VAR PeriodLength = [End Snapshot Date] - [Start Snapshot Date]
    VAR AnnualizedHires = HistoricalHires * (365.25 / PeriodLength)
    RETURN AnnualizedHires
RETURN CurrentOnHand - ProjectedLosses + ProjectedHires
```

**Format:** Whole number

**Purpose:** Estimated headcount in 12 months based on current trends.

---

#### 4.4.3 Projected Headcount Change

```dax
Projected Headcount Change =
[Projected 12mo Headcount] - [OnHand End]
```

**Format:** Whole number with +/- sign, conditional formatting

**Purpose:** Expected net change over 12 months.

---

#### 4.4.4 Attrition Risk Score

```dax
Attrition Risk Score =
VAR CurrentRate = [Attrition Rate]
VAR TrendDirection = [Attrition Rate Change (pp)]
VAR NewHireRate = [New Hire Attrition Rate]
VAR RateScore =
    SWITCH(
        TRUE(),
        CurrentRate > 0.20, 100,
        CurrentRate > 0.15, 80,
        CurrentRate > 0.12, 60,
        CurrentRate > 0.10, 40,
        CurrentRate > 0.08, 20,
        0
    )
VAR TrendScore =
    SWITCH(
        TRUE(),
        TrendDirection > 0.05, 30,
        TrendDirection > 0.03, 20,
        TrendDirection > 0.01, 10,
        0
    )
VAR NewHireScore =
    SWITCH(
        TRUE(),
        NewHireRate > 0.25, 20,
        NewHireRate > 0.20, 15,
        NewHireRate > 0.15, 10,
        0
    )
VAR TotalScore = RateScore + TrendScore + NewHireScore
RETURN MIN(TotalScore, 100)
```

**Format:** Whole number (0-100)

**Purpose:** Composite risk score. 0-40 = Green, 41-70 = Yellow, 71-100 = Red.

---

#### 4.4.5 Risk Category

```dax
Risk Category =
VAR Score = [Attrition Risk Score]
RETURN
SWITCH(
    TRUE(),
    Score >= 71, "High",
    Score >= 41, "Medium",
    "Low"
)
```

**Purpose:** Text label for risk level.

---

#### 4.4.6 Retention Risk (Experienced)

```dax
Retention Risk (Experienced) =
VAR Experienced10Plus = [Tenure 10-20 Years] + [Tenure 20+ Years]
VAR Total = [OnHand End]
VAR ExperiencedPct = DIVIDE(Experienced10Plus, Total, 0)
VAR ExperiencedAttrition =
    VAR d0 = [Start Snapshot Date]
    VAR d1 = [End Snapshot Date]
    VAR ExperiencedLosses =
        CALCULATE(
            COUNTROWS('KW DISA Personnel Actions'),
            'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
            'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
            'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
            'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern",
            (
                'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] -
                'KW DISA Personnel Actions'[DT_ARR_SVCG_CCPO]
            ) >= 3652.5
        )
    VAR ExperiencedAvg =
        VAR Start10Plus =
            VAR d0 = [Start Snapshot Date]
            RETURN
            CALCULATE(
                COUNTROWS(Employees),
                Employees[Snapshot Date] = d0,
                Employees[Student/Intern] <> "Student/Intern",
                (d0 - Employees[Dt Arrived Personnel Office]) >= 3652.5,
                USERELATIONSHIP(Employees[Occ Series Current], Series[Tittle])
            )
        RETURN DIVIDE(Start10Plus + Experienced10Plus, 2)
    RETURN DIVIDE(ExperiencedLosses, ExperiencedAvg, 0)
VAR RiskScore =
    SWITCH(
        TRUE(),
        ExperiencedAttrition > 0.10 && ExperiencedPct < 0.30, 100,
        ExperiencedAttrition > 0.08 && ExperiencedPct < 0.35, 80,
        ExperiencedAttrition > 0.06 && ExperiencedPct < 0.40, 60,
        ExperiencedAttrition > 0.05, 40,
        20
    )
RETURN RiskScore
```

**Format:** Whole number (0-100)

**Purpose:** Risk score for experienced employee retention (10+ years).

---

#### 4.4.7 Stability Index

```dax
Stability Index =
VAR RetentionRate = [Retention Rate]
VAR TenureStability =
    VAR LongTenure = [Tenure 10-20 Years] + [Tenure 20+ Years]
    RETURN DIVIDE(LongTenure, [OnHand End], 0)
VAR PromotionHealth =
    VAR PromRate = [Promotion Rate]
    RETURN MIN(PromRate / 0.15, 1)
VAR WeightedScore =
    (RetentionRate * 0.50) +
    (TenureStability * 0.30) +
    (PromotionHealth * 0.20)
RETURN WeightedScore
```

**Format:** Percentage with 0 decimal places

**Purpose:** Composite stability metric (0-100%). Higher = more stable workforce.

---

#### 4.4.8 Hiring Demand (12mo)

```dax
Hiring Demand (12mo) =
VAR ProjectedLosses = [Projected 12mo Losses]
VAR DesiredGrowth = 0
RETURN ProjectedLosses + DesiredGrowth
```

**Format:** Whole number

**Purpose:** Number of hires needed to maintain/grow workforce. Adjust DesiredGrowth as needed.

---

#### 4.4.9 Replacement Pipeline Ratio

```dax
Replacement Pipeline Ratio =
VAR HistoricalHires = [Adjusted Total Hires]
VAR PeriodLength = [End Snapshot Date] - [Start Snapshot Date]
VAR AnnualizedHires = HistoricalHires * (365.25 / PeriodLength)
VAR HiringDemand = [Hiring Demand (12mo)]
RETURN DIVIDE(AnnualizedHires, HiringDemand, 0)
```

**Format:** Decimal with 2 places (e.g., 0.85)

**Purpose:** Ratio of hiring capacity to demand. <1.0 = insufficient pipeline.

---

#### 4.4.10 Attrition Trend Status

```dax
Attrition Trend Status =
VAR Velocity = [Attrition Velocity]
VAR Acceleration = [Attrition Acceleration]
RETURN
SWITCH(
    TRUE(),
    Velocity > 0.02 && Acceleration > 0.01, "Accelerating Up",
    Velocity > 0.02 && Acceleration < -0.01, "Rising (Slowing)",
    Velocity < -0.02 && Acceleration < -0.01, "Accelerating Down",
    Velocity < -0.02 && Acceleration > 0.01, "Falling (Slowing)",
    ABS(Velocity) <= 0.02, "Stable",
    "Stable"
)
```

**Purpose:** Text description of attrition trend dynamics.

---

### 4.5 Phase 2 Validation Checklist

After creating all Phase 2 measures:

- [ ] All 45 measures created in `_Measures` table
- [ ] Tenure cohort percentages sum to ~100%
- [ ] Risk scores between 0-100
- [ ] Rolling averages smoother than raw rates
- [ ] Velocity sign matches direction of change
- [ ] Prior period measures return valid results
- [ ] Projection measures return reasonable estimates

---

## 4.6 Phase 3: Internal Movement & Sankey Optimization (14 Measures + Calculated Columns)

### Overview

Phase 3 incorporates enhancements from the Power BI Personnel Analytics Enhancement Guide, focusing on:
1. **Transparency Metrics** - Show which snapshots are being used
2. **Internal Transfer Analysis** - Track org-to-org movement patterns
3. **Rate-Based Comparative Metrics** - Fair comparison across different org sizes
4. **Sankey Diagram Optimization** - Top N filtering for cleaner visualizations

**Total Addition:** 14 measures + 3 calculated columns

---

### 4.6.1 Transparency Measures (2 Measures)

These measures display the actual snapshot dates being used in analysis, providing transparency to users.

#### 4.6.1.1 Start Snapshot Used

```dax
Start Snapshot Used =
FORMAT([Start Snapshot Date], "DD MMM YYYY")
```

**Format:** Text (displays as "31 Mar 2024")

**Purpose:** Shows the actual start snapshot date being used after closest-date snapping logic.

**Usage:** Display in card visual on every page to show users which snapshot is driving results.

---

#### 4.6.1.2 End Snapshot Used

```dax
End Snapshot Used =
FORMAT([End Snapshot Date], "DD MMM YYYY")
```

**Format:** Text (displays as "30 Sep 2024")

**Purpose:** Shows the actual end snapshot date being used after closest-date snapping logic.

**Usage:** Display in card visual on every page to show users which snapshot is driving results.

---

### 4.6.2 Internal Transfer Measures (8 Measures)

These measures track internal movement patterns between orgs and centers using your established NOA Category logic.

#### 4.6.2.1 Internal Transfer Losses

```dax
Internal Transfer Losses =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern",
    'KW DISA Personnel Actions'[NOA Category] IN {"501", "570", "702", "721"}
)
```

**Purpose:** Count of employees who left the org via internal transfers (NOAs 501, 570, 702, 721).

---

#### 4.6.2.2 External Transfer Losses

```dax
External Transfer Losses =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern",
    'KW DISA Personnel Actions'[NOA Category] IN {"300s", "T352", "CAO"}
)
```

**Purpose:** Count of employees who left DISA entirely (external separations).

---

#### 4.6.2.3 Internal Transfers Out (Org)

```dax
Internal Transfers Out (Org) =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern",
    'KW DISA Personnel Actions'[NOA Category] IN {"501", "570", "702", "721"}
)
```

**Purpose:** Count of employees transferring OUT of the selected org to another DISA org.

**Note:** Uses From Org relationship by default.

---

#### 4.6.2.4 Internal Transfers In (Org)

```dax
Internal Transfers In (Org) =
VAR d0 = [Start Snapshot Date]
VAR d1 = [End Snapshot Date]
RETURN
CALCULATE(
    COUNTROWS('KW DISA Personnel Actions'),
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] >= d0,
    'KW DISA Personnel Actions'[EFFECTIVE_DATE_4] <= d1,
    'KW DISA Personnel Actions'[Compare From/To Org] = "FALSE",
    'KW DISA Personnel Actions'[Student/Intern] <> "Student/Intern",
    'KW DISA Personnel Actions'[NOA Category] IN {"501", "570", "702", "721"},
    CROSSFILTER(
        'KW DISA Personnel Actions'[From ORG Trim],
        OrgMAPPING[Dcode],
        NONE
    ),
    USERELATIONSHIP(
        'KW DISA Personnel Actions'[To ORG Trim],
        OrgMAPPING[Dcode]
    )
)
```

**Purpose:** Count of employees transferring INTO the selected org from another DISA org.

**Technical Note:** Uses CROSSFILTER to disable the From Org relationship and USERELATIONSHIP to activate the To Org relationship.

---

#### 4.6.2.5 Net Internal Transfers (Org)

```dax
Net Internal Transfers (Org) =
[Internal Transfers In (Org)] - [Internal Transfers Out (Org)]
```

**Format:** Whole number with +/- sign, conditional formatting (green if positive, red if negative)

**Purpose:** Net gain/loss from internal transfers. Positive = org is gaining people, Negative = org is losing people.

---

#### 4.6.2.6 Net Flow (Org)

```dax
Net Flow (Org) =
[Internal Transfers In (Org)] - [Internal Transfers Out (Org)] - [External Transfer Losses]
```

**Format:** Whole number with +/- sign

**Purpose:** Overall net flow including both internal transfers and external losses.

**Calculation Logic:**
- Gains: Internal Transfers In
- Losses: Internal Transfers Out + External Transfer Losses
- Net Flow = Gains - Losses

---

#### 4.6.2.7 Top Gainer Org (Internal)

```dax
Top Gainer Org (Internal) =
VAR t =
    TOPN(
        1,
        VALUES(OrgMAPPING[Org]),
        [Net Internal Transfers (Org)],
        DESC
    )
RETURN
IF(
    ISEMPTY(t),
    BLANK(),
    CONCATENATEX(t, OrgMAPPING[Org], ", ")
)
```

**Format:** Text

**Purpose:** Returns the name of the org with the highest net internal transfer gains.

---

#### 4.6.2.8 Top Loser Org (Internal)

```dax
Top Loser Org (Internal) =
VAR t =
    TOPN(
        1,
        VALUES(OrgMAPPING[Org]),
        [Net Internal Transfers (Org)],
        ASC
    )
RETURN
IF(
    ISEMPTY(t),
    BLANK(),
    CONCATENATEX(t, OrgMAPPING[Org], ", ")
)
```

**Format:** Text

**Purpose:** Returns the name of the org with the highest net internal transfer losses (most negative).

---

### 4.6.3 Rate-Based Comparative Metrics (4 Measures)

These measures enable fair comparison between orgs of different sizes by expressing metrics as percentages of workforce.

#### 4.6.3.1 Internal Mobility Rate

```dax
Internal Mobility Rate =
DIVIDE(
    [Internal Transfer Losses],
    [Average On-Hand],
    BLANK()
)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Percentage of workforce that moved internally (enables comparison across org sizes).

**Example:** Org A has 10 internal transfers out of 50 people = 20% rate. Org B has 10 transfers out of 500 people = 2% rate.

---

#### 4.6.3.2 External Attrition Rate

```dax
External Attrition Rate =
DIVIDE(
    [External Transfer Losses],
    [Average On-Hand],
    BLANK()
)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Percentage of workforce lost externally (separations from DISA entirely).

**Conditional Formatting:** Red if >15%, Yellow if 10-15%, Green if <10%

---

#### 4.6.3.3 Net Flow Rate (Org)

```dax
Net Flow Rate (Org) =
DIVIDE(
    [Net Flow (Org)],
    [Average On-Hand],
    BLANK()
)
```

**Format:** Percentage with 1 decimal place, show +/- sign

**Purpose:** Net change rate as percentage of workforce.
- Positive: Org is net gaining people (growing)
- Negative: Org is net losing people (shrinking)
- Magnitude: How fast they're growing/shrinking relative to size

---

#### 4.6.3.4 Total Turnover Rate

```dax
Total Turnover Rate =
DIVIDE(
    [Internal Transfer Losses] + [External Transfer Losses],
    [Average On-Hand],
    BLANK()
)
```

**Format:** Percentage with 1 decimal place

**Purpose:** Combined internal + external turnover rate (total workforce churn).

**Conditional Formatting:** Red if >25%, Yellow if 15-25%, Green if <15%

---

### 4.6.4 Sankey Optimization - Top N Calculated Columns

To prevent Sankey diagram spaghetti (50 orgs × 50 orgs = 2,500 possible flows), create these calculated columns in the **'KW DISA Personnel Actions'** table to roll up small flows into "Other".

#### 4.6.4.1 Create Top N Parameter First

**Before creating calculated columns:**

1. **Modeling** tab → **New Parameter** → **Numeric range**
2. Configure:
   - **Name:** `Top N Destinations`
   - **Minimum:** 5
   - **Maximum:** 20
   - **Increment:** 1
   - **Default:** 10
   - **Add slicer to this page:** ✓ (checked)

This creates a parameter table that can be referenced in calculated columns.

---

#### 4.6.4.2 To Org (Top N)

```dax
To Org (Top N) =
VAR thisOrg = 'KW DISA Personnel Actions'[To ORG Trim]
VAR topN = 'Top N Destinations'[Top N Destinations Value]
VAR topOrgs =
    TOPN(
        topN,
        SUMMARIZE(
            ALL('KW DISA Personnel Actions'),
            'KW DISA Personnel Actions'[To ORG Trim],
            "Moves",
            CALCULATE(COUNTROWS('KW DISA Personnel Actions'))
        ),
        [Moves],
        DESC
    )
RETURN
IF(
    thisOrg IN SELECTCOLUMNS(topOrgs, "Org", [To ORG Trim]),
    thisOrg,
    "Other Orgs"
)
```

**Purpose:** Ranks all destination orgs by transfer volume. Keeps Top N as-is, replaces everything else with "Other Orgs".

**Usage:** Use this column instead of `To ORG Trim` in Sankey diagram Destination field.

---

#### 4.6.4.3 To Center (Top N)

```dax
To Center (Top N) =
VAR thisCenter = 'KW DISA Personnel Actions'[To Center Trim]
VAR topN = 'Top N Destinations'[Top N Destinations Value]
VAR topCenters =
    TOPN(
        topN,
        SUMMARIZE(
            ALL('KW DISA Personnel Actions'),
            'KW DISA Personnel Actions'[To Center Trim],
            "Moves",
            CALCULATE(COUNTROWS('KW DISA Personnel Actions'))
        ),
        [Moves],
        DESC
    )
RETURN
IF(
    thisCenter IN SELECTCOLUMNS(topCenters, "Center", [To Center Trim]),
    thisCenter,
    "Other Centers"
)
```

**Purpose:** Same as To Org (Top N) but for Center level analysis.

---

#### 4.6.4.4 Destination Agency (Top N)

```dax
Destination Agency (Top N) =
VAR thisAgency = 'KW DISA Personnel Actions'[Destination Agency]
VAR topN = 'Top N Destinations'[Top N Destinations Value]
VAR topAgencies =
    TOPN(
        topN,
        SUMMARIZE(
            ALL('KW DISA Personnel Actions'),
            'KW DISA Personnel Actions'[Destination Agency],
            "Moves",
            CALCULATE(COUNTROWS('KW DISA Personnel Actions'))
        ),
        [Moves],
        DESC
    )
RETURN
IF(
    thisAgency IN SELECTCOLUMNS(topAgencies, "Agency", [Destination Agency]),
    thisAgency,
    "Other Agencies"
)
```

**Purpose:** For external transfers, shows top N destination agencies and rolls up rest into "Other Agencies".

**Usage:** Use in External Transfers Sankey diagram.

---

### 4.6.5 Phase 3 Implementation Steps

#### Step 1: Create Transparency Measures
1. Open Power BI Desktop
2. Go to **Modeling** tab → **New Measure**
3. Create `Start Snapshot Used` and `End Snapshot Used` measures
4. Add to every page as small card visuals in top-right corner

---

#### Step 2: Create Internal Transfer Measures
1. Create all 8 internal transfer measures in `_Measures` table
2. Test with known org to verify counts
3. Verify `Net Internal Transfers (Org)` = `Transfers In - Transfers Out`

---

#### Step 3: Create Rate-Based Metrics
1. Create all 4 rate measures
2. Format as percentages with 1 decimal place
3. Add conditional formatting as specified

---

#### Step 4: Create Top N Parameter
1. **Modeling** → **New Parameter** → **Numeric range**
2. Configure as specified in 4.6.4.1
3. Test the slicer updates the value

---

#### Step 5: Create Top N Calculated Columns
1. Select **'KW DISA Personnel Actions'** table
2. **Table tools** → **New column**
3. Create all 3 calculated columns
4. Refresh the model if needed
5. Verify "Other Orgs" appears for smaller flows

---

#### Step 6: Build Internal Movement Page
1. **New page** → Rename to "Internal Movement"
2. Add slicers: Date range, `Top N Destinations`, `OrgMAPPING[Org]`, `OrgMAPPING[Center]`
3. Add transparency cards (top-right): `Start Snapshot Used`, `End Snapshot Used`
4. Add KPI cards row 1 (6 cards):
   - Internal Transfers Out (Org)
   - Internal Transfers In (Org)
   - Net Internal Transfers (Org)
   - Internal Transfers Out (Center)
   - Internal Transfers In (Center)
   - Net Internal Transfers (Center)
5. Add KPI cards row 2 (4 cards):
   - Internal Mobility Rate
   - External Attrition Rate
   - Total Turnover Rate
   - Net Flow Rate (Org)
6. Add LARGE Sankey diagram:
   - **Source:** `From ORG Trim`
   - **Destination:** `To Org (Top N)` *(use the calculated column)*
   - **Weight:** `[Internal Transfers Out (Org)]`
   - **Filter:** `NOA Category` IN {"501", "570", "702", "721"}
   - **Format:** Link opacity 40%, Node width 30
7. Add 2 tables:
   - **Top Gainers Table:**
     - Rows: `OrgMAPPING[Org]`
     - Values: `Net Internal Transfers (Org)`, `Internal Transfers In (Org)`, `Internal Transfers Out (Org)`
     - Sort: Descending by `Net Internal Transfers (Org)`
     - Filter: Top 5
   - **Top Losers Table:**
     - Same structure
     - Sort: Ascending by `Net Internal Transfers (Org)` (shows most negative first)
     - Filter: Top 5

---

### 4.6.6 Phase 3 Validation Checklist

After creating all Phase 3 measures and calculated columns:

- [ ] All 14 measures created in `_Measures` table
- [ ] All 3 calculated columns created in `KW DISA Personnel Actions` table
- [ ] `Top N Destinations` parameter created and slicer functional
- [ ] Transparency cards show correct formatted dates
- [ ] `Net Internal Transfers (Org)` = `Transfers In - Transfers Out` (exact match)
- [ ] `Net Flow (Org)` = `Transfers In - Transfers Out - External Losses` (exact match)
- [ ] Rate measures return percentages between 0-100%
- [ ] Sankey diagram shows "Other Orgs" for flows outside Top N
- [ ] Adjusting Top N slider updates Sankey diagram
- [ ] `Top Gainer Org (Internal)` returns valid org name
- [ ] `Top Loser Org (Internal)` returns valid org name
- [ ] All measures exclude Student/Intern (except RG/VT PCNs)

---

### 4.6.7 Phase 3 Measure Summary Table

| Measure | Purpose | Format |
|---|---|---|
| Start Snapshot Used | Show actual start date used | Text (DD MMM YYYY) |
| End Snapshot Used | Show actual end date used | Text (DD MMM YYYY) |
| Internal Transfer Losses | Count of internal transfers out | Whole number |
| External Transfer Losses | Count of external separations | Whole number |
| Internal Transfers Out (Org) | Transfers leaving selected org | Whole number |
| Internal Transfers In (Org) | Transfers entering selected org | Whole number |
| Net Internal Transfers (Org) | Net internal gain/loss | Whole number (+/-) |
| Net Flow (Org) | Overall net flow (internal + external) | Whole number (+/-) |
| Top Gainer Org (Internal) | Org with highest internal gains | Text |
| Top Loser Org (Internal) | Org with highest internal losses | Text |
| Internal Mobility Rate | Internal transfers as % of workforce | Percentage (1 dec) |
| External Attrition Rate | External losses as % of workforce | Percentage (1 dec) |
| Net Flow Rate (Org) | Net flow as % of workforce | Percentage (1 dec) |
| Total Turnover Rate | Total churn as % of workforce | Percentage (1 dec) |

---

## 5. Dashboard Design Specifications

### 5.1 Page 1: Executive Overview

**Purpose:** High-level workforce health dashboard (becomes first page after build)

**Layout:** 3 rows

#### Row 1: Key Metrics Cards (4 cards across)

1. **Attrition Rate Card**
   - Value: `[Attrition Rate]`
   - Format: 13.2%
   - Conditional Formatting:
     - Red if >12%
     - Yellow if 8-12%
     - Green if <8%
   - Subtitle: vs Prior Period ([Attrition Rate Change (pp)])

2. **Retention Rate Card**
   - Value: `[Retention Rate]`
   - Format: 86.8%
   - Conditional Formatting:
     - Green if >92%
     - Yellow if 88-92%
     - Red if <88%
   - Subtitle: vs Prior Period ([Retention Rate Change (pp)])

3. **Net Headcount Change Card**
   - Value: `[Net Headcount Change]`
   - Format: -45
   - Show +/- sign
   - Conditional Formatting:
     - Green if positive
     - Red if negative

4. **Attrition Risk Score Card**
   - Value: `[Attrition Risk Score]`
   - Format: 68
   - Conditional Formatting:
     - Red if 71-100
     - Yellow if 41-70
     - Green if 0-40
   - Subtitle: Risk Category ([Risk Category])

#### Row 2: Trend Charts (2 charts)

1. **Attrition & Retention Trend (Line Chart)**
   - X-Axis: `Snapshot Date`
   - Y-Axis: `[Attrition Rate]`, `[Retention Rate]`, `[Rolling 3-Period Avg Attrition]`
   - Colors:
     - Attrition Rate: #E74C3C (Red)
     - Retention Rate: #27AE60 (Green)
     - Rolling Avg: #95A5A6 (Gray, dashed line)
   - Data Labels: On for latest period only
   - Legend: Bottom

2. **Headcount Movement (Waterfall Chart)**
   - Categories: OnHand Start, Hires, Losses, Net Change, OnHand End
   - Values: Respective measures
   - Colors:
     - Start/End: #3498DB (Blue)
     - Hires: #27AE60 (Green)
     - Losses: #E74C3C (Red)
     - Net Change: Conditional (green if positive, red if negative)

#### Row 3: Breakdowns (3 visuals)

1. **Attrition by Organization (Bar Chart)**
   - Y-Axis: `Employees[Org]`
   - X-Axis: `[Attrition Rate]`
   - Sort: Descending by Attrition Rate
   - Color: Conditional by value
   - Data Labels: On
   - Top N: Show top 10

2. **Tenure Distribution (Donut Chart)**
   - Legend: Tenure categories
   - Values: Tenure cohort measures
   - Colors: Sequential blues (#DCEEF3 to #003B5C)
   - Data Labels: Percentage

3. **Voluntary vs Non-Voluntary (Stacked Bar)**
   - Y-Axis: Single bar
   - X-Axis: `[Voluntary Losses]`, `[Non-Voluntary Losses]`
   - Colors:
     - Voluntary: #F39C12 (Orange)
     - Non-Voluntary: #C0392B (Dark Red)
   - Data Labels: Count and percentage

**Slicers:**
- Date range slicer (between dates)
- Organization slicer (dropdown)
- Center slicer (dropdown)

---

### 5.2 Page 2: Attrition Analysis

**Purpose:** Deep dive into attrition patterns

**Layout:** 3 rows

#### Row 1: Attrition Metrics (5 cards)

1. Total Losses
2. Attrition Rate
3. Voluntary Attrition Rate
4. Non-Voluntary Attrition Rate
5. New Hire Attrition Rate

**Format:** All with conditional formatting

#### Row 2: Attrition Breakdown Charts (3 charts)

1. **Attrition by Organization (Column Chart)**
   - X-Axis: `Employees[Org]`
   - Y-Axis: `[Attrition Rate]`
   - Reference Line: Benchmark (12%)
   - Sort: Descending

2. **Attrition by Tenure Cohort (Column Chart)**
   - X-Axis: Tenure categories (0-2, 2-5, 5-10, 10-20, 20+)
   - Y-Axis: Cohort-specific attrition rates (manual calculation)
   - Color: By value

3. **Attrition Trend with Velocity (Line + Column Combo)**
   - X-Axis: `Snapshot Date`
   - Line: `[Attrition Rate]`, `[Rolling 6-Period Avg Attrition]`
   - Column: `[Attrition Velocity]`
   - Dual Axis

#### Row 3: Detail Tables (2 tables)

1. **Top Attrition Organizations (Table)**
   - Columns:
     - Org
     - Attrition Rate
     - Attrition Rate Change (pp)
     - Total Losses
     - Voluntary Losses
     - Non-Voluntary Losses
     - OnHand End
   - Conditional Formatting: On Attrition Rate and Change
   - Sort: Descending by Attrition Rate
   - Top N: 15

2. **Attrition Risk Assessment (Table)**
   - Columns:
     - Org
     - Attrition Risk Score
     - Risk Category
     - Current Rate
     - Trend Direction
     - New Hire Rate
   - Conditional Formatting: Risk-based coloring
   - Sort: Descending by Risk Score

**Filters:**
- Same slicers as Page 1
- Vol/Non-Vol slicer (buttons)

---

### 5.3 Page 3: Retention Analysis

**Purpose:** Focus on retention patterns

**Layout:** 3 rows

#### Row 1: Retention Metrics (5 cards)

1. Retention Rate
2. Retention Rate (Same Org)
3. Retention Rate (Same Center Same Series)
4. Remain Count
5. Stability Index

**Format:** Percentages with conditional formatting

#### Row 2: Retention Breakdown (3 charts)

1. **Retention by Organization (Bar Chart)**
   - Y-Axis: `Employees[Org]`
   - X-Axis: `[Retention Rate]`
   - Reference Line: 92% target
   - Sort: Ascending (show lowest retention first)
   - Color: Conditional

2. **Retention Funnel (Funnel Chart)**
   - Stages:
     - OnHand Start
     - Remain Count (anywhere)
     - Remain Same Org Count
     - Remain Same Center Same Series Count
   - Values: Respective measures
   - Show percentages

3. **Retention Trend (Line Chart)**
   - X-Axis: `Snapshot Date`
   - Y-Axis:
     - Retention Rate
     - Retention Rate (Same Org)
     - Retention Rate (Same Center Same Series)
   - Colors: Sequential greens

#### Row 3: Movement Analysis (2 visuals + 1 matrix)

1. **Internal Movement (Stacked Column)**
   - X-Axis: `Employees[Org]`
   - Y-Axis:
     - Promotion Count
     - Lateral Move Count
     - Internal Movement Count
   - Colors: Distinct

2. **Transfer Flow (Sankey Diagram)**
   - Source: From Org
   - Target: To Org
   - Value: Transfer count
   - Filter: Only show orgs with >5 transfers

3. **Retention Detail Matrix**
   - Rows: Org, Center
   - Values:
     - Retention Rate
     - Remain Count
     - Lost from Start Org Count
     - Promotion Rate
     - Internal Mobility Rate
   - Conditional Formatting: All columns

---

### 5.4 Page 4: Demographics Deep Dive

**Purpose:** Workforce composition analysis

#### Visuals:

1. **Tenure Distribution Over Time (Stacked Area Chart)**
   - X-Axis: Snapshot Date
   - Y-Axis: Count of employees
   - Legend: Tenure cohorts (0-2, 2-5, 5-10, 10-20, 20+)
   - Colors: Sequential

2. **Average Tenure Trend (Line Chart)**
   - X-Axis: Snapshot Date
   - Y-Axis: Avg Tenure (Current)
   - Reference Line: Overall average

3. **Demographics Matrix**
   - Rows: Org
   - Values:
     - OnHand End
     - Avg Tenure (Current)
     - New Hire %
     - Tenure 0-2 Years %
     - Tenure 10+ Years %

4. **New Hire Analysis (Column + Line Combo)**
   - X-Axis: Snapshot Date
   - Column: New Hire On-Hand
   - Line: New Hire Attrition Rate

---

### 5.5 Page 5: Movement Patterns

**Purpose:** Career progression and mobility

#### Visuals:

1. **Promotion Analysis (Clustered Column)**
   - X-Axis: Org
   - Y-Axis: Promotion Count, Promotion Rate
   - Dual Axis

2. **Lateral Movement Matrix**
   - Rows: Org
   - Columns:
     - Lateral Move Count
     - Lateral Move Rate
     - Internal Mobility Rate
     - Transfer In Count
     - Transfer Out Count
     - Net Transfer
   - Conditional Formatting

3. **Transfer Heat Map**
   - Rows: From Org
   - Columns: To Org
   - Values: Transfer count
   - Color Scale: White to Blue

---

### 5.6 Page 6: Predictive Insights

**Purpose:** Forward-looking analytics

#### Row 1: Projection Cards (4 cards)

1. Projected 12mo Losses
2. Projected 12mo Headcount
3. Projected Headcount Change
4. Hiring Demand (12mo)

#### Row 2: Risk Assessment (2 visuals)

1. **Risk Score Matrix**
   - Rows: Org
   - Values:
     - Attrition Risk Score
     - Risk Category
     - Retention Risk (Experienced)
     - Stability Index
   - Conditional Formatting

2. **Risk Quadrant (Scatter Chart)**
   - X-Axis: Attrition Rate
   - Y-Axis: Attrition Velocity
   - Size: OnHand End
   - Color: Risk Category
   - Quadrant Lines: At benchmark and 0 velocity

#### Row 3: Trend Analysis

1. **Attrition Trend Status (Table)**
   - Columns:
     - Org
     - Attrition Trend Status
     - Attrition Rate
     - Attrition Velocity
     - Attrition Acceleration
   - Icon-based formatting

2. **Pipeline Analysis (Gauge + KPI)**
   - Gauge: Replacement Pipeline Ratio (0-2.0 scale)
     - Red: <0.8
     - Yellow: 0.8-1.2
     - Green: >1.2
   - KPI Card: Hiring Demand vs Actual Hires

---

### 5.7 Page 7: Comparative Analytics

**Purpose:** Benchmarking and period comparisons

#### Visuals:

1. **Period-over-Period Comparison (Table)**
   - Rows: Org
   - Columns:
     - Current Attrition Rate
     - Prior Period Attrition Rate
     - Attrition Rate Change (pp)
     - Attrition Rate Change %
   - Conditional Formatting with arrows

2. **Year-over-Year Comparison (Column + Line)**
   - X-Axis: Org
   - Column: Current Period Attrition Rate
   - Line: YoY Attrition Rate
   - Gap: YoY Attrition Change (pp)

3. **Benchmark Comparison (Bullet Chart)**
   - Rows: Org
   - Actual: Current Attrition Rate
   - Target: Benchmark (12%)
   - Satisfactory Range: 8-12%

4. **Rolling Average Trends (Line Chart)**
   - X-Axis: Snapshot Date
   - Y-Axis:
     - Attrition Rate (thin line)
     - Rolling 3-Period Avg (medium line)
     - Rolling 6-Period Avg (thick line)
   - Shows trend smoothing

---

### 5.8 Page 8: Organization Detail (Drill-Through)

**Purpose:** Single-org deep dive (drill-through target)

**Drill-Through Fields:** Employees[Org]

#### Layout:

**Header Section:**
- Org Name (large text)
- Center Name
- Current Snapshot Date

**KPI Row (6 cards):**
1. OnHand End
2. Attrition Rate
3. Retention Rate
4. Avg Tenure (Current)
5. Attrition Risk Score
6. Org Rank (new measure below)

**Charts Row:**
1. Trend over time (line chart)
2. Tenure distribution (bar chart)
3. Vol vs Non-Vol breakdown (pie)

**Detail Table:**
- All key metrics with period-over-period comparison

**New Measure for Page 8:**

```dax
Org Rank =
VAR CurrentOrg = SELECTEDVALUE(Employees[Org])
VAR AttrRate = [Attrition Rate]
VAR AllOrgs =
    CALCULATETABLE(
        VALUES(Employees[Org]),
        ALL(Employees)
    )
VAR Ranking =
    COUNTROWS(
        FILTER(
            AllOrgs,
            CALCULATE([Attrition Rate]) > AttrRate
        )
    ) + 1
VAR TotalOrgs = COUNTROWS(AllOrgs)
RETURN Ranking & " of " & TotalOrgs
```

---

### 5.9 Page 9: Exit Analysis

**Purpose:** Detailed loss analysis

#### Visuals:

1. **Loss Type Breakdown (Treemap)**
   - Groups: NOA Category
   - Values: Loss count
   - Tooltips: NOA descriptions

2. **Exit Reasons (Waterfall)**
   - Categories: Voluntary, Non-Voluntary subcategories
   - Values: Counts

3. **Loss Timing Analysis (Line Chart)**
   - X-Axis: Month of effective date
   - Y-Axis: Loss count
   - Shows seasonality

4. **Exit Demographics Matrix**
   - Rows: Various demographics
   - Values:
     - Loss count
     - % of total losses
     - Attrition rate for that demographic

---

### 5.10 Page 10: Hiring Pipeline vs Attrition

**Purpose:** Hiring effectiveness

#### Visuals:

1. **Hire vs Loss Comparison (Line Chart)**
   - X-Axis: Snapshot Date
   - Y-Axis: Total Hires, Total Losses
   - Gap: Net Change

2. **Replacement Pipeline (Gauge)**
   - Value: Replacement Pipeline Ratio
   - Target: 1.0
   - Range: 0-2.0

3. **New Hire Retention (Funnel)**
   - Stages: Hired, Retained 6mo, Retained 12mo, Retained 24mo
   - Shows new hire retention journey

4. **Hiring Demand Forecast (Column + Line)**
   - X-Axis: Time period
   - Column: Hiring Demand (12mo)
   - Line: Actual hires trend
   - Gap visualization

---

### 5.11 Page 11: Executive Briefing (One-Pager)

**Purpose:** Single-page executive summary (print-ready)

**Fixed Layout - No scrolling:**

**Top Section: Overall Health (3 large KPIs)**
1. Attrition Rate (with status icon)
2. Retention Rate (with status icon)
3. Net Headcount Change

**Middle Left: Top 3 Risks**
- Automatically show top 3 orgs by Attrition Risk Score
- Show org name, risk score, and key driver

**Middle Right: Key Trend**
- Small line chart showing 6-month attrition trend
- Arrow showing direction

**Bottom Section: 12-Month Outlook**
- Projected Headcount Change
- Hiring Demand
- Risk Level (text + color)

**Color Scheme:** Professional (Navy #003B5C, Gray #7F8C8D, Red #E74C3C, Green #27AE60)

---

### 5.12 Page 12: Action Dashboard

**Purpose:** Track improvement initiatives

#### Visuals:

1. **High-Risk Organizations (Table)**
   - Org
   - Risk Score
   - Key Metric to Improve
   - Recommended Action (manual field)
   - Owner (manual field)
   - Status (manual field)

2. **Progress Tracker (Line Chart)**
   - X-Axis: Month
   - Y-Axis: Risk Score
   - Multiple lines for tracked orgs
   - Shows improvement over time

3. **Action Status Summary (Cards)**
   - Count of High Risk Orgs
   - Count of Actions In Progress
   - Count of Improved Orgs (decrease in risk)

---

## 6. Validation & Testing

### 6.1 Test Scenarios

#### Test Scenario 1: Headcount Balance

**Test:** Verify that headcount changes balance correctly.

**Formula:**
```
OnHand End = OnHand Start - Total Losses + Adjusted Total Hires
```

**Expected Result:** Formula should hold true (within ±1 due to rounding)

**Test Data:**
- Select date range: January 2024 to December 2024
- Check: OnHand End vs calculated value

---

#### Test Scenario 2: Loss Classification

**Test:** Verify Voluntary + Non-Voluntary = Total Losses

**Formula:**
```
Voluntary Losses + Non-Voluntary Losses = Total Losses
```

**Expected Result:** Exact match (no rounding difference)

**Test Data:**
- Various date ranges
- Various org filters

---

#### Test Scenario 3: Retention Math

**Test:** Verify Attrition Rate + Retention Rate ≈ 100%

**Formula:**
```
Attrition Rate + Retention Rate ≈ 1.00 (within 2%)
```

**Expected Result:** Sum between 98% and 102%

**Note:** Won't be exact 100% due to:
- Different denominators (Average On-Hand vs OnHand Start)
- Internal movements not counted as losses

---

#### Test Scenario 4: Student/Intern Exclusion

**Test:** Verify Student/Intern exclusion logic

**Steps:**
1. Count all employees where Student/Intern is NOT blank
2. Count those same employees where PCN contains "RG" or "VT"
3. Verify excluded count = (1) - (2)

**Expected Result:** Measures should exclude students/interns except RG/VT

---

#### Test Scenario 5: Closest Date Snapping

**Test:** Verify between-date slicer finds nearest snapshot

**Steps:**
1. Select a date with no snapshot (e.g., June 15, 2024)
2. Check Start Snapshot Date measure
3. Verify it returns closest date >= selected date

**Expected Result:** Returns actual snapshot date, not selected date

---

#### Test Scenario 6: Tenure Cohort Distribution

**Test:** Verify tenure cohorts sum to 100%

**Formula:**
```
SUM(
    Tenure 0-2 Years %,
    Tenure 2-5 Years %,
    Tenure 5-10 Years %,
    Tenure 10-20 Years %,
    Tenure 20+ Years %
) = 100%
```

**Expected Result:** Sum = 100% (within rounding)

---

### 6.2 Data Quality Checks

Run these checks regularly:

#### Check 1: Missing Snapshot Dates
```dax
Missing Dates Check =
VAR ExpectedDates =
    GENERATE(
        CALENDAR([Start Date], [End Date]),
        ROW("Expected", "Monthly")
    )
VAR ActualDates = VALUES(Employees[Snapshot Date])
RETURN
COUNTROWS(
    EXCEPT(ExpectedDates, ActualDates)
)
```

**Expected:** 0 (all expected dates present)

---

#### Check 2: Duplicate EDIPIs in Same Snapshot
```dax
Duplicate EDIPI Check =
SUMX(
    VALUES(Employees[Snapshot Date]),
    VAR CurrentDate = Employees[Snapshot Date]
    VAR EDIPIs =
        CALCULATETABLE(
            Employees,
            Employees[Snapshot Date] = CurrentDate
        )
    VAR UniqueCount = DISTINCTCOUNT(Employees[DoD ID (EDIPI)])
    VAR TotalCount = COUNTROWS(EDIPIs)
    RETURN TotalCount - UniqueCount
)
```

**Expected:** 0 (no duplicates)

---

#### Check 3: Actions Without Valid Org Mapping
```dax
Unmapped Actions Check =
COUNTROWS(
    FILTER(
        'KW DISA Personnel Actions',
        ISBLANK('KW DISA Personnel Actions'[From Org]) ||
        ISBLANK('KW DISA Personnel Actions'[To Org])
    )
)
```

**Expected:** Low count (investigate any unmapped)

---

### 6.3 Performance Optimization

If measures are slow:

1. **Check Date Filter Context**
   - Ensure Date_Actions table is used for filtering
   - Avoid filtering on unrelated date fields

2. **Optimize FILTER Functions**
   - Use TREATAS instead of FILTER when possible
   - Consider calculated columns for complex logic

3. **Review Relationship Paths**
   - Confirm no circular dependencies
   - Verify inactive relationships are intentional

4. **Add Aggregations (if dataset is large)**
   - Create aggregation tables for common metrics
   - Use Power BI aggregations feature

---

## 7. Implementation Roadmap

### 7.1 Four-Week Implementation Plan

#### **Week 1: Foundation + Phase 1** (32-40 hours)

**Day 1: Data Model Review (6-8 hours)** ✅ COMPLETE
- Review semantic model
- Verify relationships
- Test data refresh
- Document any data quality issues

**Day 2: Core Measures + Validation (8 hours)**
- Create all 19 Phase 1 measures in _Measures table
- Test each measure with multiple date ranges
- Run Test Scenarios 1-5
- Document any calculation errors

**Day 3: Dashboard Building - Pages 1-3 (8-10 hours)**
- Build Page 1: Executive Overview
  - Create slicers
  - Add 4 KPI cards
  - Build 2 trend charts
  - Add 3 breakdown visuals
- Build Page 2: Attrition Analysis (partial)

**Day 4: Dashboard Completion - Pages 1-3 (8-10 hours)**
- Complete Page 2: Attrition Analysis
- Build Page 3: Retention Analysis
- Format all visuals
- Add tooltips and drill-throughs

**Day 5: Polish & Review (2-4 hours)**
- Review Pages 1-3 with stakeholder
- Fix formatting issues
- Add report theme
- Document Page 1-3 usage

**Week 1 Deliverables:**
- ✅ Data model validated
- ✅ 19 Phase 1 measures created and tested
- ✅ Pages 1-3 complete and functional
- ✅ Phase 1 validation passed

---

#### **Week 2: Phase 2 & Phase 3 Analytics** (35-40 hours)

**Day 1: Demographics & Tenure Measures (6-7 hours)**
- Create 15 measures (4.1.1 through 4.1.15)
- Test tenure calculations
- Verify cohort distribution sums to 100%

**Day 2: Trending & Comparison Measures (6-7 hours)**
- Create 12 measures (4.2.1 through 4.2.12)
- Test rolling averages
- Verify velocity and acceleration calculations

**Day 3: Movement & Career Patterns + Phase 3 Start (7-8 hours)**
- Create 8 Phase 2 measures (4.3.1 through 4.3.8)
- Test promotion logic
- **Phase 3:** Create transparency measures (2 measures)
- **Phase 3:** Create internal transfer measures (8 measures)
- Test internal transfer logic with CROSSFILTER/USERELATIONSHIP

**Day 4: Predictive & Risk Analysis + Phase 3 Completion (7-8 hours)**
- Create 10 measures (4.4.1 through 4.4.10)
- Test risk score algorithm
- **Phase 3:** Create rate-based metrics (4 measures)
- **Phase 3:** Create Top N parameter
- **Phase 3:** Create Top N calculated columns (3 columns)
- Test Top N Sankey optimization

**Day 5: Dashboard Pages (8-9 hours)**
- Build Page 4: Demographics Deep Dive
- Build Page 5: Movement Patterns
- **Build Internal Movement Page (Phase 3)**
  - Add transparency cards
  - Add KPI cards (10 total)
  - Build Sankey diagram with Top N optimization
  - Add Top Gainers/Losers tables
- Build Page 6: Predictive Insights (partial)

**Week 2 Deliverables:**
- ✅ All 45 Phase 2 measures created
- ✅ All 14 Phase 3 measures created
- ✅ Top N parameter and 3 calculated columns created
- ✅ Internal Movement page complete
- ✅ Phase 2 & 3 validation tests passed
- ✅ Pages 4-6 started or complete

---

#### **Week 3: Specialized Pages** (25-30 hours)

**Day 1: Complete Page 6-7 (6-8 hours)**
- Complete Page 6: Predictive Insights
- Build Page 7: Comparative Analytics
- Test all Phase 2 visuals

**Day 2: Drill-Through Page (5-6 hours)**
- Build Page 8: Organization Detail
- Create Org Rank measure
- Configure drill-through
- Test drill-through from multiple pages

**Day 3: Specialized Analysis Pages (6-8 hours)**
- Build Page 9: Exit Analysis
- Build Page 10: Hiring Pipeline vs Attrition
- Add advanced tooltips

**Day 4: Executive & Action Pages (4-6 hours)**
- Build Page 11: Executive Briefing (one-pager)
- Build Page 12: Action Dashboard
- Format for printing (Page 11)

**Day 5: Testing & Refinement (4-6 hours)**
- Complete end-to-end testing
- Run all validation scenarios
- Performance testing
- Fix any bugs

**Week 3 Deliverables:**
- ✅ All 12 pages complete
- ✅ Drill-through configured
- ✅ Executive briefing print-ready
- ✅ All tests passed

---

#### **Week 4: Deployment & Training** (20-25 hours)

**Day 1: Documentation (4-5 hours)**
- Complete user guide
- Document measure definitions
- Create quick reference card
- Record short demo videos (5min, 10min)

**Day 2: UAT Session (4-5 hours)**
- Conduct User Acceptance Testing with 3-5 users
- Document feedback
- Create issue log
- Prioritize fixes

**Day 3: Bug Fixes & Polish (6-8 hours)**
- Fix issues from UAT
- Final formatting polish
- Add bookmarks for common views
- Optimize performance

**Day 4: Training Preparation (3-4 hours)**
- Prepare training materials
- Create hands-on exercises
- Set up training environment
- Send training invitations

**Day 5: Launch Day (3-4 hours)**
- Publish to Power BI Service
- Configure refresh schedule
- Set up security (RLS if needed)
- Send launch announcement

**Week 4 Deliverables:**
- ✅ Documentation complete
- ✅ UAT passed
- ✅ Dashboard published
- ✅ Training scheduled

---

### 7.2 Post-Launch Activities

**Week 5: Soft Launch**
- Monday: Analyst training (90min)
- Wednesday: Manager training (60min)
- Friday: Executive presentation (30min)
- Daily: Office hours (1hr)

**Week 6: Full Launch**
- Monday: Agency-wide announcement
- Tuesday-Friday: Drop-in office hours (2hrs/day)
- End of week: Collect feedback survey

**Weeks 7-10: Support & Iteration**
- Week 7: Address common questions, create FAQ
- Week 8: Minor enhancements based on feedback
- Week 9: Add requested features (if feasible)
- Week 10: Performance review, optimization

---

### 7.3 Success Metrics

**Technical Metrics:**
- Data refresh success rate >99%
- Report load time <5 seconds
- Query response time <3 seconds
- Zero critical bugs after Week 6

**Adoption Metrics:**
- 80% of managers access dashboard in first month
- 50% of analysts use weekly
- 100% of executives briefed

**Business Impact Metrics:**
- Reduction in time to produce attrition reports (manual → automated)
- Increase in data-driven retention discussions
- Faster identification of high-risk organizations

---

## 8. Presentation & Rollout Strategy

### 8.1 Stakeholder Analysis

| Stakeholder Group | Primary Interest | Success Criteria | Communication Approach |
|---|---|---|---|
| **Executives (Directors+)** | Strategic insights, high-level trends, risk identification | Can make informed decisions in <5 minutes | Dashboard demo, one-pager briefing, quarterly reviews |
| **HR Managers** | Organization-specific metrics, benchmarking, action plans | Can identify issues and track improvements | Hands-on training, weekly review meetings |
| **Analysts** | Data accuracy, methodology, detailed breakdowns | Can answer ad-hoc questions and validate results | Technical deep-dive, documentation, ongoing support |
| **IT/Data Team** | Data refresh, performance, security | System runs smoothly with minimal intervention | Technical handoff, troubleshooting guide |

---

### 8.2 Presentation Formats

#### **Format 1: Executive Presentation (30 minutes)**

**Structure:**
1. **Problem Statement** (3 min)
   - Current challenges with manual reporting
   - Need for real-time visibility into attrition

2. **Solution Overview** (5 min)
   - What the dashboard does
   - Key features (between-date analysis, risk scoring, projections)

3. **Live Demo** (15 min)
   - Page 1: Executive Overview
   - Page 11: Executive Briefing (one-pager)
   - Page 8: Organization Detail (drill-through example)
   - Show filtering and interaction

4. **Insights & Recommendations** (5 min)
   - Top 3 findings from current data
   - Recommended focus areas

5. **Q&A** (2 min)

**Slide Deck:**
- Slide 1: Title
- Slide 2: The Problem
- Slide 3: The Solution
- Slide 4: Key Features
- Slides 5-7: Screenshot of 3 key pages
- Slide 8: Sample Insights
- Slide 9: Next Steps

---

#### **Format 2: Manager Deep Dive (60 minutes)**

**Structure:**
1. **Introduction** (5 min)
   - Dashboard purpose and goals
   - How it will help managers

2. **Guided Tour** (20 min)
   - Walk through all 12 pages
   - Explain each visual
   - Show common use cases

3. **Hands-On Practice** (25 min)
   - Participants use dashboard
   - Exercise 1: Find your org's attrition rate
   - Exercise 2: Compare to prior period
   - Exercise 3: Drill into detail page
   - Exercise 4: Identify top risk areas

4. **Interpreting Results** (8 min)
   - How to read conditional formatting
   - What thresholds mean
   - When to take action

5. **Q&A** (2 min)

**Materials:**
- Hands-on exercise worksheet
- Quick reference guide
- Link to dashboard

---

#### **Format 3: Analyst Training (90 minutes)**

**Structure:**
1. **Technical Overview** (15 min)
   - Data model architecture
   - Measure logic overview
   - Refresh schedule

2. **Measure Deep Dive** (30 min)
   - Attrition calculation methodology
   - Retention calculation methodology
   - Between-date slicer logic
   - Student/Intern exclusion rules
   - Voluntary vs Non-Voluntary classification

3. **Advanced Features** (20 min)
   - Risk scoring algorithm
   - Projection calculations
   - Rolling averages
   - Velocity and acceleration

4. **Validation & QA** (15 min)
   - How to validate results
   - Common test scenarios
   - Where to find documentation

5. **Hands-On Practice** (8 min)
   - Build a custom report
   - Explain results to a manager

6. **Q&A** (2 min)

**Materials:**
- Technical documentation (this guide)
- Measure definitions reference
- Validation checklist
- Practice dataset

---

### 8.3 Demo Scripts

#### **5-Minute Executive Demo**

**Script:**

> [0:00-0:30] "This is the DISA Workforce Analytics Dashboard. Everything you see updates automatically each week with the latest employee data."

> [0:30-1:30] "Top row shows overall workforce health: Attrition is 13.2%, which is red because it's above our 12% threshold. Retention is 86.8%, yellow. Net change is -45 employees, down from last quarter. Risk score is 68 out of 100—we need attention."

> [1:30-2:30] "This chart shows the trend. Red line is attrition—it's been climbing. The gray dashed line is the rolling average, which smooths out noise. You can see we're above our historical average."

> [2:30-3:30] "Bottom section breaks it down. BD organization has the highest attrition at 18.5%. Let me click on BD to drill into their detail page. [Click] Here's BD's complete profile: 156 employees, 18.5% attrition rate, trending up 3.2 percentage points. Average tenure is dropping. Risk score is 85—critical level."

> [3:30-4:30] "Down here, 12-month projection. If current trends continue, BD will lose approximately 29 more employees, bringing them down to 127 people. That's a 18.5% reduction in workforce size."

> [4:30-5:00] "This dashboard updates every Monday morning. You can filter by organization, date range, or drill into any area. Questions?"

---

#### **10-Minute Manager Demo**

**Script:**

> [0:00-1:00] "Welcome. This dashboard gives you real-time visibility into attrition and retention across your organization. Let me show you how to use it."

> [1:00-2:00] "First, select your time period using this slicer. I'll choose the last 6 months. Notice all the metrics update automatically. These four cards at the top show your key health indicators."

> [2:00-3:30] "Attrition rate is 13.2%—red means it's above benchmark. Click the card, and you can see this compares to 11.8% last period, so it's up 1.4 percentage points. The retention rate is the flip side: 86.8% of employees who were here at the start are still here."

> [3:30-5:00] "This bar chart ranks all organizations by attrition rate. BD is highest at 18.5%. If you want more detail on BD, right-click and select 'Drill through to Organization Detail.' [Show drill-through] This page shows BD's complete story: headcount over time, tenure breakdown, voluntary vs non-voluntary splits."

> [5:00-6:30] "Use the tabs at the bottom to navigate. Page 2 is attrition analysis—deeper breakdowns by tenure, org, and type. Page 3 is retention analysis—who's staying and why. Page 6 is predictive—it projects what will happen in the next 12 months based on current trends."

> [6:30-8:00] "Page 11 is designed for executives—it's a one-page summary you can print or screenshot. Shows overall health, top 3 risks, and 12-month outlook. This updates automatically, so you can pull this every Monday for your weekly briefing."

> [8:00-9:30] "Common questions: 'How do I export data?' Right-click any visual, select 'Export data.' 'How do I save my filters?' Use bookmarks. 'When does this refresh?' Every Monday at 6 AM. 'Who do I contact for help?' Email workforce-analytics@disa.mil."

> [9:30-10:00] "Now it's your turn. Everyone open the dashboard and find your organization's attrition rate. Questions as we go?"

---

### 8.4 Communication Plan

#### **Pre-Launch (Week 4)**

**Email 1: Pre-Announcement** (Send Monday)
```
Subject: Coming Soon: DISA Workforce Analytics Dashboard

Colleagues,

I'm excited to announce the upcoming launch of the DISA Workforce
Analytics Dashboard—a new tool that will transform how we understand
and manage our workforce.

What it does:
- Real-time attrition and retention metrics
- Between-period comparisons
- Risk identification and projections
- Drill-down to organization level

Launch Date: [Date]

Training sessions will be scheduled for the week following launch.
More details coming soon.

[Your Name]
```

**Email 2: Training Invitation** (Send Wednesday)
```
Subject: Workforce Analytics Dashboard Training - Register Now

The DISA Workforce Analytics Dashboard launches next week. Please
register for one of the following training sessions:

- Executive Briefing: [Date/Time] - 30 minutes
  Register: [Link]

- Manager Training: [Date/Time] - 60 minutes
  Register: [Link]

- Analyst Deep Dive: [Date/Time] - 90 minutes
  Register: [Link]

Can't attend live? All sessions will be recorded and posted to
[SharePoint/Teams location].

Questions? Email workforce-analytics@disa.mil

[Your Name]
```

---

#### **Launch Week (Week 5)**

**Email 3: Launch Announcement** (Send Monday morning)
```
Subject: LAUNCHED: DISA Workforce Analytics Dashboard

The DISA Workforce Analytics Dashboard is now live!

Access: [Power BI Link]

What's New:
✓ Automated attrition and retention metrics
✓ Real-time data (refreshed every Monday)
✓ 12 interactive pages
✓ Risk scoring and projections

Training:
- Watch 5-minute demo: [Video Link]
- Review quick start guide: [PDF Link]
- Attend live training: [Schedule]

Support:
- Office hours: Tuesday-Friday, 2-3 PM (Teams link: [Link])
- Email: workforce-analytics@disa.mil
- Documentation: [SharePoint Link]

[Your Name]
```

**Slack/Teams Message** (Post Monday morning)
```
🎉 Workforce Analytics Dashboard is LIVE!

Access here: [Link]

New to the dashboard? Watch the 5-min demo: [Link]

Questions? Join office hours this week (Tue-Fri, 2-3 PM)

#WorkforceAnalytics #DataDriven
```

---

#### **Post-Launch (Weeks 6-10)**

**Email 4: Week 2 Check-In** (Send Monday of Week 6)
```
Subject: Workforce Analytics Dashboard - Week 1 Recap

Thank you to everyone who attended training last week!

Week 1 Highlights:
- 150+ users accessed the dashboard
- 45 attended training sessions
- Top question: "How do I export data?" (Answer: Right-click visual)

New This Week:
- Added FAQ document: [Link]
- Recorded training sessions now available: [Link]

Reminder: Office hours continue this week (Tue-Fri, 2-3 PM)

[Your Name]
```

**Email 5: Success Story** (Send Week 8)
```
Subject: Dashboard Impact: BD Organization Case Study

Quick win to share:

BD Organization used the Workforce Analytics Dashboard to identify
high attrition in their 2-5 year tenure cohort (28% attrition vs
13% overall).

They implemented targeted retention strategies:
- Mentorship program for mid-level staff
- Career development workshops
- Stay interviews with at-risk employees

Result: 2-month trend shows attrition declining from 18.5% to 15.2%.

Dashboard features that helped:
- Tenure cohort breakdown (Page 4)
- Drill-through org detail (Page 8)
- Monthly tracking (Page 1 trend chart)

Want to replicate this success? Contact workforce-analytics@disa.mil

[Your Name]
```

---

### 8.5 Change Management Strategies

#### **Addressing Resistance**

**Resistance Type 1: "The old way works fine"**

**Response:**
- Show time savings: Manual report = 8 hours, Dashboard = 2 minutes
- Demonstrate new insights not available in manual reports (risk scores, projections)
- Offer side-by-side validation: "Let's compare dashboard to your manual report"

**Resistance Type 2: "I don't trust the data"**

**Response:**
- Walk through data lineage and validation tests
- Show exact calculation formulas
- Offer to validate against known baseline (pick a historical period they trust)
- Provide documentation on data sources and transformations

**Resistance Type 3: "Too complex for me"**

**Response:**
- Start with Page 11 only (one-pager)
- Offer 1-on-1 training
- Create role-based quick start guides
- Assign a "dashboard buddy" (peer mentor)

**Resistance Type 4: "Worried about accountability"**

**Response:**
- Emphasize dashboard is a tool, not a judgment
- Frame as "early warning system" to help managers succeed
- Show how it identifies problems before they become crises
- Highlight support resources available

---

#### **Building Momentum**

**Quick Wins:**
1. Identify 2-3 early adopter managers
2. Help them find one actionable insight
3. Publicize their success (with permission)
4. Create case studies

**Gamification:**
- "Dashboard Champion" recognition for most improved org
- Monthly "Insight of the Month" contest
- Leaderboard for most-engaged users (anonymous)

**Continuous Improvement:**
- Monthly "Dashboard Office Hours" for feedback
- Quarterly enhancement releases
- Annual user survey
- Public roadmap for requested features

---

### 8.6 Ongoing Support Structure

#### **Tiered Support Model**

**Tier 1: Self-Service**
- FAQ document
- Video library (5-min demos)
- Quick reference cards
- Tooltips in dashboard

**Tier 2: Peer Support**
- Dashboard Champions network (trained power users in each org)
- Teams channel for questions
- Monthly user group meeting

**Tier 3: Help Desk**
- Email: workforce-analytics@disa.mil
- Response SLA: 24 hours
- Handles: Login issues, data questions, bug reports

**Tier 4: Development Team**
- Monthly office hours
- Handles: Enhancement requests, custom analyses, technical issues
- Escalation from Tier 3

---

#### **Monthly Cadence**

**Week 1:**
- Data refresh (Monday 6 AM)
- Automated email with key metrics summary
- Check for anomalies

**Week 2:**
- Office hours session (Tuesday 2-3 PM)
- Review outstanding support tickets

**Week 3:**
- User group meeting (Wednesday 10-11 AM)
- Share tips & tricks
- Preview upcoming enhancements

**Week 4:**
- Review feedback and analytics
- Plan next month's improvements
- Update documentation

---

## 9. Quick Reference

### 9.1 Measure Quick Reference

| Measure | Formula Summary | Format | Use Case |
|---|---|---|---|
| **OnHand Start** | Count employees at start snapshot (exclude students/interns) | Whole number | Baseline headcount |
| **OnHand End** | Count employees at end snapshot (exclude students/interns) | Whole number | Current headcount |
| **Average On-Hand** | (OnHand Start + OnHand End) / 2 | Decimal | Attrition rate denominator |
| **Total Losses** | Count actions where Compare From/To Org = "FALSE" | Whole number | Total attrition |
| **Attrition Rate** | Total Losses / Average On-Hand | Percentage | Primary attrition metric |
| **Retention Rate** | Remain Count / OnHand Start | Percentage | Primary retention metric |
| **Remain Count** | Count EDIPIs present at both start and end | Whole number | Retention calculation |
| **Voluntary Losses** | Losses where Vol/Non-Vol = "Voluntary" | Whole number | Voluntary attrition |
| **Non-Voluntary Losses** | Losses where Vol/Non-Vol = "Non-Voluntary" | Whole number | Involuntary attrition |
| **New Hire Attrition Rate** | New Hire Losses / Avg New Hire Count | Percentage | Early-career attrition risk |
| **Attrition Risk Score** | Composite: Rate + Trend + New Hire (0-100) | Whole number | Overall risk assessment |
| **Projected 12mo Losses** | Current Rate * Current Headcount | Whole number | Workforce planning |
| **Rolling 3-Period Avg** | Average of last 3 periods' attrition rates | Percentage | Trend smoothing |

---

### 9.2 Common Use Cases

**Use Case 1: Monthly Executive Briefing**
- Open Page 11 (Executive Briefing)
- Select current month date range
- Screenshot or print
- Present in leadership meeting

**Use Case 2: Organization Health Check**
- Go to Page 1
- Filter to specific org
- Review 4 KPI cards
- Check trend chart
- Right-click org in bar chart → Drill through to Page 8

**Use Case 3: Identify High-Risk Areas**
- Go to Page 6 (Predictive Insights)
- Review Risk Score Matrix
- Sort by Attrition Risk Score descending
- Investigate top 3 orgs
- Drill through for detail

**Use Case 4: Year-over-Year Comparison**
- Go to Page 7 (Comparative Analytics)
- Review YoY Comparison chart
- Identify orgs with largest changes
- Investigate causes

**Use Case 5: New Hire Retention Analysis**
- Go to Page 4 (Demographics)
- Review "New Hire Analysis" visual
- Check New Hire Attrition Rate
- Compare to overall attrition
- Identify if onboarding improvements needed

---

### 9.3 Troubleshooting Guide

**Problem:** Measures returning blank

**Solution:**
- Check date slicer selection
- Verify data refresh completed successfully
- Check if selected org has data for that period
- Clear all filters and try again

---

**Problem:** Headcount balance doesn't match

**Solution:**
- Verify Student/Intern exclusion is working
- Check for duplicate EDIPIs in snapshot
- Run data quality checks (Section 6.2)
- Compare to source data

---

**Problem:** Risk scores seem wrong

**Solution:**
- Check thresholds in Attrition Risk Score measure
- Verify trend direction is calculating correctly
- Validate New Hire Attrition Rate is not blank
- Review component scores individually

---

**Problem:** Dashboard loads slowly

**Solution:**
- Check if multiple large visuals on one page
- Reduce number of data points in chart (use Top N)
- Consider adding aggregations
- Check Power BI Service capacity

---

**Problem:** Can't drill through to Page 8

**Solution:**
- Verify drill-through is configured (Field: Employees[Org])
- Check that a single org is selected (drill-through requires single value)
- Ensure Page 8 is not hidden

---

### 9.4 Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| Ctrl + / | Open filter pane |
| Ctrl + Shift + F | Focus mode on visual |
| Ctrl + Click | Multi-select in slicer |
| Shift + Click | Range select in slicer |
| Alt + Shift + F11 | Export data |
| Ctrl + B | Create bookmark |

---

### 9.5 Best Practices

**Data Interpretation:**
1. Always check date range first
2. Compare to prior period for context
3. Look for trends, not just snapshots
4. Verify headcount balance before trusting other metrics
5. Investigate outliers (don't just accept unusual values)

**Dashboard Usage:**
1. Start broad (Page 1), then drill down (Page 8)
2. Use filters sparingly (they can hide context)
3. Screenshot key insights for presentations
4. Create bookmarks for common views
5. Refresh browser if dashboard seems stale

**Storytelling with Data:**
1. Lead with the "So what?" (insight, not just number)
2. Provide context (vs benchmark, vs prior period, vs other orgs)
3. Show trends over time (not just current snapshot)
4. Suggest action (don't just present problem)
5. Follow up on previous recommendations

---

### 9.6 Contact Information

**Dashboard Support:**
- Email: workforce-analytics@disa.mil
- Office Hours: Tuesday-Friday, 2-3 PM
- Teams Channel: [Link]

**Data Quality Issues:**
- Email: hr-data@disa.mil
- Include: Screenshot, date range, expected vs actual result

**Training Requests:**
- Email: workforce-analytics@disa.mil
- Subject: "Training Request: [Your Org]"

**Enhancement Requests:**
- Submit via: [Form Link]
- Include: What you need, why you need it, who else would benefit

---

## Appendix A: Glossary

**Attrition:** Loss of an employee from the organization (includes separations and transfers out)

**Attrition Rate:** Total Losses divided by Average On-Hand Count (expressed as percentage)

**Between-Date Slicer:** Date range selector that finds closest available snapshot dates

**Closest Date Snapping:** Logic that finds nearest snapshot date when selected date has no data

**Compare From/To Org:** Field indicating whether employee stayed in same org ("TRUE") or left ("FALSE")

**EDIPI:** Electronic Data Interchange Personal Identifier (unique DoD ID)

**Net Headcount Change:** OnHand End - OnHand Start (can be positive or negative)

**NOA:** Nature of Action (SF-50 personnel action code)

**Non-Voluntary:** Attrition classified as involuntary (NOAs 330, 355, 357, 385)

**On-Hand:** Count of employees in a snapshot (denominator for most calculations)

**Remain Count:** Employees present at both start and end periods (retention calculation)

**Retention Rate:** Remain Count divided by OnHand Start (expressed as percentage)

**Risk Score:** Composite metric (0-100) combining current rate, trend, and new hire attrition

**Snapshot Date:** Date when employee data was captured (typically monthly)

**Student/Intern:** Employee with occupation series ending in "99" (excluded unless PCN contains RG or VT)

**Tenure:** Length of service (calculated from Dt Arrived Personnel Office to snapshot date)

**Voluntary:** Attrition classified as voluntary (all NOAs except 330, 355, 357, 385)

---

## Appendix B: Data Dictionary

### Employees Table Fields

| Field Name | Data Type | Description | Example |
|---|---|---|---|
| DoD ID (EDIPI) | Text | Unique employee identifier | "1234567890" |
| Snapshot Date | Date | Date of data snapshot | 2024-03-31 |
| Dt Arrived Personnel Office | Date | Arrival date (for tenure) | 2018-06-15 |
| Org | Text | Organization name (mapped) | "Business Development" |
| Center | Text | Center name (mapped) | "HQ" |
| Student/Intern | Text | Flag if occupation series ends in 99 | "Student/Intern" or blank |
| Manpower PCN | Text | Position Control Number | "AB12345RG" |
| Occ Series Current | Text | Occupation series code | "2210" |
| Grade or Level | Text | Pay grade | "13" |

### Personnel Actions Table Fields

| Field Name | Data Type | Description | Example |
|---|---|---|---|
| EFFECTIVE_DATE_4 | Date | Effective date of action | 2024-03-15 |
| NOA | Text | Nature of Action code | "352" |
| NOA Category | Text | Categorized NOA | "T352" |
| Vol/Non-Vol | Text | Voluntary or Non-Voluntary | "Voluntary" |
| Compare From/To Org | Text | Same org flag | "TRUE" or "FALSE" |
| From Org | Text | Origin organization | "IT Services" |
| To Org | Text | Destination organization | "Cybersecurity" |

---

## Appendix C: Calculation Methodology

### Attrition Rate Calculation (Detailed)

**Step 1:** Identify the analysis period
- Start Snapshot Date (d0)
- End Snapshot Date (d1)

**Step 2:** Calculate denominator (Average On-Hand)
```
OnHand Start = Count of employees where:
    - Snapshot Date = d0
    - Student/Intern is blank (OR Manpower PCN contains "RG" or "VT")

OnHand End = Count of employees where:
    - Snapshot Date = d1
    - Student/Intern is blank (OR Manpower PCN contains "RG" or "VT")

Average On-Hand = (OnHand Start + OnHand End) / 2
```

**Step 3:** Calculate numerator (Total Losses)
```
Total Losses = Count of personnel actions where:
    - EFFECTIVE_DATE_4 >= d0 AND <= d1
    - Compare From/To Org = "FALSE"
    - Student/Intern is blank (OR MOST_RECENT_EMPL_MANPOWER_PCN contains "RG" or "VT")
```

**Step 4:** Calculate rate
```
Attrition Rate = Total Losses / Average On-Hand
```

**Example:**
- OnHand Start: 500 employees
- OnHand End: 485 employees
- Average On-Hand: (500 + 485) / 2 = 492.5
- Total Losses: 65 actions
- Attrition Rate: 65 / 492.5 = 0.132 = 13.2%

---

### Retention Rate Calculation (Detailed)

**Step 1:** Identify EDIPIs at Start
```
StartEDIPIs = List of DoD ID (EDIPI) where:
    - Snapshot Date = d0
    - Student/Intern is blank
```

**Step 2:** Identify EDIPIs at End
```
EndEDIPIs = List of DoD ID (EDIPI) where:
    - Snapshot Date = d1
    - Student/Intern is blank
```

**Step 3:** Find intersection
```
Remain Count = Count of EDIPIs present in BOTH StartEDIPIs AND EndEDIPIs
```

**Step 4:** Calculate rate
```
Retention Rate = Remain Count / OnHand Start
```

**Example:**
- OnHand Start: 500 employees
- OnHand End: 485 employees
- Remain Count: 445 employees (present at both periods)
- Retention Rate: 445 / 500 = 0.89 = 89.0%

**Note:** Retention Rate + Attrition Rate ≠ exactly 100% because:
- Attrition uses Average On-Hand (not OnHand Start)
- Internal movements don't count as losses but affect retention
- New hires during period affect OnHand End but not retention

---

## Appendix D: Version History

| Version | Date | Changes | Author |
|---|---|---|---|
| 0.1 | 2025-01-15 | Initial draft | Implementation Team |
| 0.5 | 2025-01-22 | Added Phase 2 measures | Implementation Team |
| 0.9 | 2025-01-29 | Added all dashboard designs | Implementation Team |
| 1.0 | 2025-02-05 | Complete implementation guide | Implementation Team |

---

## Appendix E: Future Enhancements

**Planned for V1.1 (Q2 2025):**
- Add pay plan analysis
- Include hiring pipeline data (requires USA Staffing integration)
- Add supervisor/non-supervisor breakdowns
- Mobile-optimized views

**Planned for V1.2 (Q3 2025):**
- Predictive modeling using R/Python visuals
- Automated email alerts for high-risk orgs
- Integration with performance review data
- What-if scenario planning tool

**Planned for V2.0 (Q4 2025):**
- Real-time data (daily refresh)
- Row-level security by organization
- Custom user-defined metrics
- API for external system integration

---

## Document End

**For questions or support:**
- Email: workforce-analytics@disa.mil
- Teams: [Workforce Analytics Channel]
- SharePoint: [Documentation Library]

**Last Updated:** February 2025
