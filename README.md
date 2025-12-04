Below is what you asked for:
	•	One M replacement for the Employees partition (adds Snapshot Date correctly).
	•	All the new calculated columns and measures for:
	•	Employees (retention + tenure)
	•	PersonnelActions (attrition, internal/external, movement shares, risk score)
	•	All in TMDL syntax, with all percentages formatted as "0.0%".

You will:
	1.	Replace the partition Employees = m source = block with the new M code.
	2.	Paste the Employees columns/measures block inside table Employees.
	3.	Paste the PersonnelActions columns/measures block inside table PersonnelActions.
	4.	In the Model view, set PersonnelActions[EFFECTIVE_DATE_4] to Date (if it’s still text).
	5.	Create relationships:
	•	Employees[ORG First 2] → OrgMAPPING[Org Code]
	•	PersonnelActions[From ORG 2 Digit] → OrgMAPPING[Org Code].

⸻

1. Employees – M code (partition) with Snapshot Date

Replace your existing partition Employees = m block with this:

        partition Employees = m
            mode: import
            source =
                    let
                        Source =
                            SharePoint.Files(
                                "https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/",
                                [ ApiVersion = 15 ]
                            ),

                        // Keep only the folder with snapshot files
                        #"Filtered Rows" =
                            Table.SelectRows(
                                Source,
                                each [Folder Path]
                                    = "https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/Shared Documents/Attrition-Retention Automation/TEST/NewTest/"
                            ),

                        // Only DISA snapshot files (e.g. "DISA Empl Data 01 10 24.xlsx")
                        #"Filtered Rows1" =
                            Table.SelectRows(
                                #"Filtered Rows",
                                each Text.StartsWith( [Name], "DISA" )
                            ),

                        // Remove hidden files BEFORE dropping Attributes
                        #"Filtered Hidden Files1" =
                            Table.SelectRows(
                                #"Filtered Rows1",
                                each [Attributes]?[Hidden]? <> true
                            ),

                        // Add Snapshot Date from file name: "DISA Empl Data dd mm yy.xlsx"
                        #"Added Snapshot Date" =
                            Table.AddColumn(
                                #"Filtered Hidden Files1",
                                "Snapshot Date",
                                each
                                    let
                                        name = [Name],
                                        baseName = Text.BeforeDelimiter( name, "." ),
                                        // Expected: "DISA Empl Data dd mm yy"
                                        datePart = Text.AfterDelimiter( baseName, "DISA Empl Data " ),
                                        parts = Text.Split( datePart, " " ),
                                        d = Number.FromText( parts{0} ),
                                        m = Number.FromText( parts{1} ),
                                        yShort = Number.FromText( parts{2} ),
                                        y =
                                            if yShort < 50
                                            then 2000 + yShort
                                            else 1900 + yShort
                                    in
                                        #date( y, m, d ),
                                type date
                            ),

                        // We don't need these metadata columns anymore
                        #"Removed Columns" =
                            Table.RemoveColumns(
                                #"Added Snapshot Date",
                                { "Extension", "Date accessed", "Date modified", "Date created", "Attributes", "Folder Path" }
                            ),

                        // Apply existing Transform File to Content
                        #"Invoke Custom Function1" =
                            Table.AddColumn(
                                #"Removed Columns",
                                "Transform File",
                                each #"Transform File"( [Content] )
                            ),

                        #"Removed Other Columns1" =
                            Table.SelectColumns(
                                #"Invoke Custom Function1",
                                { "Snapshot Date", "Transform File" }
                            ),

                        #"Expanded Table Column1" =
                            Table.ExpandTableColumn(
                                #"Removed Other Columns1",
                                "Transform File",
                                Table.ColumnNames( #"Transform File"( #"Sample File" ) )
                            )
                    in
                        #"Expanded Table Column1"

            annotation PBI_NavigationStepName = Navigation

            annotation PBI_ResultType = Table


⸻

2. Employees – new calculated columns & measures (TMDL)

Paste these inside table Employees after the existing physical columns (before partition Employees = m is fine).

            // ============================================
            // Created Columns (Employees)
            // ============================================

            column 'Snapshot Date'
                dataType: dateTime
                summarizeBy: none
                sourceColumn: Snapshot Date

            column 'ORG First 2'
                dataType: string
                summarizeBy: none
                expression: =
                    IF (
                        NOT ISBLANK ( 'Employees'['Org Structure ID'] ),
                        LEFT ( 'Employees'['Org Structure ID'], 2 )
                    )

            column 'Student/Intern'
                dataType: int64
                summarizeBy: none
                expression: =
                    VAR TitleUpper =
                        UPPER ( 'Employees'[Title] )
                    VAR Series =
                        'Employees'['Occ Series Current']
                    VAR PCN =
                        'Employees'['Manpower PCN']
                    VAR IsStudentTitle =
                        CONTAINSSTRING ( TitleUpper, "STUDENT" )
                    VAR IsInternTitle =
                        CONTAINSSTRING ( TitleUpper, "INTERN" )
                    VAR Is99Series =
                        NOT ISBLANK ( Series )
                            && RIGHT ( Series, 2 ) = "99"
                    VAR IsRGVT =
                        NOT ISBLANK ( PCN )
                            && (
                                CONTAINSSTRING ( PCN, "RG" )
                                    || CONTAINSSTRING ( PCN, "VT" )
                            )
                    RETURN
                        // 1 = student/intern, 0 = regular employee
                        IF (
                            ( IsStudentTitle || IsInternTitle || Is99Series )
                                && NOT IsRGVT,
                            1,
                            0
                        )

            column 'Tenure Years'
                dataType: double
                summarizeBy: none
                expression: =
                    VAR StartDate =
                        'Employees'['Dt Arrived Personnel Office']
                    VAR SnapDate =
                        'Employees'['Snapshot Date']
                    RETURN
                        IF (
                            NOT ISBLANK ( StartDate )
                                && NOT ISBLANK ( SnapDate ),
                            DATEDIFF ( StartDate, SnapDate, YEAR )
                        )

            column 'Tenure Band'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR Tenure =
                        'Employees'['Tenure Years']
                    RETURN
                        SWITCH (
                            TRUE (),
                            Tenure < 1, "< 1 year",
                            Tenure < 3, "1–3 years",
                            Tenure < 5, "3–5 years",
                            Tenure < 10, "5–10 years",
                            Tenure < 20, "10–20 years",
                            "20+ years"
                        )

            // ============================================
            // Measures (Employees) - Retention & Headcount
            // ============================================

            measure 'Start Snapshot Date'
                dataType: dateTime
                formatString: "General Date"
                expression: =
                    MIN ( 'Employees'['Snapshot Date'] )

            measure 'End Snapshot Date'
                dataType: dateTime
                formatString: "General Date"
                expression: =
                    MAX ( 'Employees'['Snapshot Date'] )

            measure 'On-hand count'
                dataType: int64
                formatString: "#,0"
                expression: =
                    CALCULATE (
                        DISTINCTCOUNT ( 'Employees'[DoD ID (EDIPI)] ),
                        'Employees'['Student/Intern'] = 0
                    )

            measure 'OnHand Start'
                dataType: int64
                formatString: "#,0"
                expression: =
                    VAR d0 =
                        [Start Snapshot Date]
                    RETURN
                        IF (
                            NOT ISBLANK ( d0 ),
                            CALCULATE (
                                DISTINCTCOUNT ( 'Employees'[DoD ID (EDIPI)] ),
                                'Employees'['Snapshot Date'] = d0,
                                'Employees'['Student/Intern'] = 0
                            )
                        )

            measure 'OnHand End'
                dataType: int64
                formatString: "#,0"
                expression: =
                    VAR d1 =
                        [End Snapshot Date]
                    RETURN
                        IF (
                            NOT ISBLANK ( d1 ),
                            CALCULATE (
                                DISTINCTCOUNT ( 'Employees'[DoD ID (EDIPI)] ),
                                'Employees'['Snapshot Date'] = d1,
                                'Employees'['Student/Intern'] = 0
                            )
                        )

            measure 'Average On-hand count'
                dataType: double
                formatString: "#,0.0"
                expression: =
                    DIVIDE ( [OnHand Start] + [OnHand End], 2.0 )

            measure 'Remain Count'
                dataType: int64
                formatString: "#,0"
                expression: =
                    VAR d0 =
                        [Start Snapshot Date]
                    VAR d1 =
                        [End Snapshot Date]
                    RETURN
                        IF (
                            NOT ISBLANK ( d0 )
                                && NOT ISBLANK ( d1 ),
                            VAR StartSet =
                                CALCULATETABLE (
                                    VALUES ( 'Employees'[DoD ID (EDIPI)] ),
                                    'Employees'['Snapshot Date'] = d0,
                                    'Employees'['Student/Intern'] = 0
                                )
                            VAR EndSet =
                                CALCULATETABLE (
                                    VALUES ( 'Employees'[DoD ID (EDIPI)] ),
                                    'Employees'['Snapshot Date'] = d1,
                                    'Employees'['Student/Intern'] = 0
                                )
                            VAR CommonIDs =
                                INTERSECT ( StartSet, EndSet )
                            RETURN
                                COUNTROWS ( CommonIDs )
                        )

            measure 'Retention Rate'
                dataType: double
                formatString: "0.0%"
                expression: =
                    DIVIDE ( [Remain Count], [OnHand Start] )

            // Simple advanced indicator: New-hire concentration
            measure 'New Hire On-hand Start (<2y)'
                dataType: int64
                formatString: "#,0"
                expression: =
                    VAR d0 =
                        [Start Snapshot Date]
                    RETURN
                        IF (
                            NOT ISBLANK ( d0 ),
                            CALCULATE (
                                DISTINCTCOUNT ( 'Employees'[DoD ID (EDIPI)] ),
                                'Employees'['Snapshot Date'] = d0,
                                'Employees'['Student/Intern'] = 0,
                                'Employees'['Tenure Years'] < 2
                            )
                        )

            measure 'New Hire Share Start (<2y)'
                dataType: double
                formatString: "0.0%"
                expression: =
                    DIVIDE ( [New Hire On-hand Start (<2y)], [OnHand Start] )


⸻

3. PersonnelActions – new calculated columns & measures (TMDL)

Paste these inside table PersonnelActions after the existing physical columns (before partition PersonnelActions = m).

Before using date filters in the measures below, set PersonnelActions[EFFECTIVE_DATE_4] to Date in the model (or change dataType in TMDL from string to dateTime and let the engine convert).

            // ============================================
            // Created Columns (PersonnelActions)
            // ============================================

            column 'From ORG Trim'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR line =
                        'PersonnelActions'[FROM_POSITION_ORG_LINE6_14]
                    VAR afterDash =
                        IF (
                            NOT ISBLANK ( line )
                                && FIND ( "-", line, 1, 0 ) > 0,
                            TRIM ( RIGHT ( line, LEN ( line ) - FIND ( "-", line, 1, 0 ) ) ),
                            TRIM ( line )
                        )
                    RETURN
                        afterDash

            column 'To ORG Trim'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR line =
                        'PersonnelActions'[TO_POSITION_ORG_LINE6_22]
                    VAR afterDash =
                        IF (
                            NOT ISBLANK ( line )
                                && FIND ( "-", line, 1, 0 ) > 0,
                            TRIM ( RIGHT ( line, LEN ( line ) - FIND ( "-", line, 1, 0 ) ) ),
                            TRIM ( line )
                        )
                    RETURN
                        afterDash

            column 'From ORG 2 Digit'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR trimmed =
                        'PersonnelActions'['From ORG Trim']
                    RETURN
                        IF ( NOT ISBLANK ( trimmed ), LEFT ( trimmed, 2 ) )

            column 'To ORG 2 Digit'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR trimmed =
                        'PersonnelActions'['To ORG Trim']
                    RETURN
                        IF ( NOT ISBLANK ( trimmed ), LEFT ( trimmed, 2 ) )

            column 'From ORG'
                dataType: string
                summarizeBy: none
                expression: =
                    RELATED ( OrgMAPPING[Org] )

            column 'From Center'
                dataType: string
                summarizeBy: none
                expression: =
                    RELATED ( OrgMAPPING[Center] )

            column 'To ORG'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR Code =
                        'PersonnelActions'['To ORG 2 Digit']
                    RETURN
                        CALCULATE (
                            MAX ( OrgMAPPING[Org] ),
                            OrgMAPPING[Org Code] = Code
                        )

            column 'To Center'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR Code =
                        'PersonnelActions'['To ORG 2 Digit']
                    RETURN
                        CALCULATE (
                            MAX ( OrgMAPPING[Center] ),
                            OrgMAPPING[Org Code] = Code
                        )

            column 'Compare From and To Organization'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR fromOrg =
                        'PersonnelActions'['From ORG']
                    VAR toOrg =
                        'PersonnelActions'['To ORG']
                    VAR NOA =
                        'PersonnelActions'[FIRST_NOA_CODE_5A]
                    VAR NOA3 =
                        LEFT ( NOA, 3 )
                    VAR ForceDifferent =
                        NOA3 = "300"
                            || NOA = "T352"
                            || NOA = "CAO"
                    RETURN
                        IF (
                            ISBLANK ( fromOrg ) || ISBLANK( toOrg ),
                            BLANK (),
                            IF (
                                ForceDifferent,
                                "DIFFERENT",
                                IF ( fromOrg = toOrg, "SAME", "DIFFERENT" )
                            )
                        )

            column 'Compare Job Series Changes'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR fromSeries =
                        'PersonnelActions'[FROM_OCC_CODE_9]
                    VAR toSeries =
                        'PersonnelActions'[TO_OCC_CODE_17]
                    VAR NOA =
                        'PersonnelActions'[FIRST_NOA_CODE_5A]
                    VAR NOA3 =
                        LEFT ( NOA, 3 )
                    VAR ForceDifferent =
                        NOA3 = "300"
                            || NOA = "T352"
                            || NOA = "CAO"
                    RETURN
                        IF (
                            ISBLANK ( fromSeries ) || ISBLANK ( toSeries ),
                            BLANK (),
                            IF (
                                ForceDifferent,
                                "DIFFERENT",
                                IF ( fromSeries = toSeries, "SAME", "DIFFERENT" )
                            )
                        )

            column 'Student/Intern'
                dataType: int64
                summarizeBy: none
                expression: =
                    VAR TitleUpper =
                        UPPER ( 'PersonnelActions'[MOST_RECENT_EMPL_POSN_TITLE] )
                    VAR Series =
                        'PersonnelActions'[MOST_RECENT_EMPL_SERIES]
                    VAR PCN =
                        'PersonnelActions'[MOST_RECENT_EMPL_MANPOWER_PCN]
                    VAR IsStudentTitle =
                        CONTAINSSTRING ( TitleUpper, "STUDENT" )
                    VAR IsInternTitle =
                        CONTAINSSTRING ( TitleUpper, "INTERN" )
                    VAR Is99Series =
                        NOT ISBLANK ( Series )
                            && RIGHT ( Series, 2 ) = "99"
                    VAR IsRGVT =
                        NOT ISBLANK ( PCN )
                            && (
                                CONTAINSSTRING ( PCN, "RG" )
                                    || CONTAINSSTRING ( PCN, "VT" )
                            )
                    RETURN
                        IF (
                            ( IsStudentTitle || IsInternTitle || Is99Series )
                                && NOT IsRGVT,
                            1,
                            0
                        )

            column 'Internal/External Movement'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR NOA =
                        'PersonnelActions'[FIRST_NOA_CODE_5A]
                    VAR NOA3 =
                        LEFT ( NOA, 3 )
                    RETURN
                        IF (
                            NOA3 = "300"
                                || NOA = "CAO"
                                || NOA = "T352",
                            "External",
                            IF (
                                NOA IN { "501", "570", "702", "721" },
                                "Internal",
                                BLANK ()
                            )
                        )

            column 'Departure Reason'
                dataType: string
                summarizeBy: none
                expression: =
                    VAR lac2 =
                        'PersonnelActions'[FIRST_ACTION_LA_CODE2_5E]
                    RETURN
                        SWITCH (
                            TRUE (),
                            lac2 = "ADR", "DoD-DRP2",
                            lac2 = "RZM", "DoD-DRP1",
                            lac2 = "AZM", "VERA/VSIP",
                            'PersonnelActions'[FIRST_NOA_DESC_5B]
                        )

            // ============================================
            // Measures (PersonnelActions) - Losses & Attrition
            // ============================================

            measure Losses
                dataType: int64
                formatString: "#,0"
                expression: =
                    VAR d0 =
                        [Start Snapshot Date]
                    VAR d1 =
                        [End Snapshot Date]
                    RETURN
                        IF (
                            NOT ISBLANK ( d0 )
                                && NOT ISBLANK ( d1 ),
                            CALCULATE (
                                COUNTROWS ( 'PersonnelActions' ),
                                'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                                'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                                VAR NOA =
                                    'PersonnelActions'[FIRST_NOA_CODE_5A]
                                VAR NOA3 =
                                    LEFT ( NOA, 3 )
                                RETURN
                                    NOA3 = "300"
                                        || NOA IN { "T352", "501", "570", "702", "721", "CAO" },
                                'PersonnelActions'['Compare From and To Organization'] = "DIFFERENT",
                                'PersonnelActions'['Student/Intern'] = 0
                            )
                        )

            measure 'Agency Losses'
                dataType: int64
                formatString: "#,0"
                expression: =
                    VAR d0 =
                        [Start Snapshot Date]
                    VAR d1 =
                        [End Snapshot Date]
                    RETURN
                        IF (
                            NOT ISBLANK ( d0 )
                                && NOT ISBLANK ( d1 ),
                            CALCULATE (
                                COUNTROWS ( 'PersonnelActions' ),
                                'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                                'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                                VAR NOA =
                                    'PersonnelActions'[FIRST_NOA_CODE_5A]
                                VAR NOA3 =
                                    LEFT ( NOA, 3 )
                                RETURN
                                    NOA3 = "300"
                                        || NOA = "T352"
                                        || NOA = "CAO",
                                'PersonnelActions'['Student/Intern'] = 0
                            )
                        )

            measure 'Attrition Rate'
                dataType: double
                formatString: "0.0%"
                expression: =
                    DIVIDE ( [Losses], 'Employees'['Average On-hand count'] )

            measure 'Agency Attrition Rate'
                dataType: double
                formatString: "0.0%"
                expression: =
                    DIVIDE ( [Agency Losses], 'Employees'['Average On-hand count'] )

            measure 'Non-voluntary Losses'
                dataType: int64
                formatString: "#,0"
                expression: =
                    VAR d0 =
                        [Start Snapshot Date]
                    VAR d1 =
                        [End Snapshot Date]
                    RETURN
                        IF (
                            NOT ISBLANK ( d0 )
                                && NOT ISBLANK ( d1 ),
                            CALCULATE (
                                COUNTROWS ( 'PersonnelActions' ),
                                'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                                'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                                'PersonnelActions'[FIRST_NOA_CODE_5A] IN { "330", "355", "357", "385" },
                                'PersonnelActions'['Student/Intern'] = 0
                            )
                        )

            measure 'Non-voluntary Attrition'
                dataType: double
                formatString: "0.0%"
                expression: =
                    DIVIDE ( [Non-voluntary Losses], 'Employees'['Average On-hand count'] )

            measure 'Voluntary Attrition'
                dataType: double
                formatString: "0.0%"
                expression: =
                    [Attrition Rate] - [Non-voluntary Attrition]

            measure 'Internal Moves'
                dataType: int64
                formatString: "#,0"
                expression: =
                    VAR d0 =
                        [Start Snapshot Date]
                    VAR d1 =
                        [End Snapshot Date]
                    RETURN
                        IF (
                            NOT ISBLANK ( d0 )
                                && NOT ISBLANK ( d1 ),
                            CALCULATE (
                                COUNTROWS ( 'PersonnelActions' ),
                                'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                                'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                                'PersonnelActions'[FIRST_NOA_CODE_5A] IN { "501", "570", "702", "721" },
                                'PersonnelActions'['Compare From and To Organization'] = "DIFFERENT",
                                'PersonnelActions'['Student/Intern'] = 0
                            )
                        )

            measure 'External Losses'
                dataType: int64
                formatString: "#,0"
                expression: =
                    VAR d0 =
                        [Start Snapshot Date]
                    VAR d1 =
                        [End Snapshot Date]
                    RETURN
                        IF (
                            NOT ISBLANK ( d0 )
                                && NOT ISBLANK ( d1 ),
                            CALCULATE (
                                COUNTROWS ( 'PersonnelActions' ),
                                'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                                'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                                VAR NOA =
                                    'PersonnelActions'[FIRST_NOA_CODE_5A]
                                VAR NOA3 =
                                    LEFT ( NOA, 3 )
                                RETURN
                                    NOA3 = "300"
                                        || NOA = "T352"
                                        || NOA = "CAO",
                                'PersonnelActions'['Student/Intern'] = 0
                            )
                        )

            measure 'Total Movement Events'
                dataType: int64
                formatString: "#,0"
                expression: =
                    [Internal Moves] + [External Losses]

            measure 'Internal Movement Share'
                dataType: double
                formatString: "0.0%"
                expression: =
                    DIVIDE ( [Internal Moves], [Total Movement Events] )

            measure 'External Movement Share'
                dataType: double
                formatString: "0.0%"
                expression: =
                    DIVIDE ( [External Losses], [Total Movement Events] )

            measure 'Org Risk Score'
                dataType: double
                formatString: "0.0"
                expression: =
                    VAR attr =
                        [Attrition Rate]
                    VAR extAttr =
                        [Agency Attrition Rate]
                    VAR ret =
                        [Retention Rate]
                    RETURN
                        // Simple composite: higher attr, higher external attr, lower retention = higher risk
                        ( attr * 0.5 )
                            + ( extAttr * 0.3 )
                            + ( ( 1 - ret ) * 0.2 )


⸻
