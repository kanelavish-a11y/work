
    column 'Pers Additional Work Role 2 Desc'
        dataType: string
        summarizeBy: none
        sourceColumn: Pers Additional Work Role 2 Desc



    column 'Snapshot Date'
        dataType: dateTime
        formatString: Long Date
        summarizeBy: none
        sourceColumn: Snapshot Date

    column 'ORG First 2' =
        IF (
            NOT ISBLANK ( 'Employees'[Org Structure ID] ),
            LEFT ( 'Employees'[Org Structure ID], 2 )
        )
        dataType: string
        summarizeBy: none

    column 'Student/Intern' =
        VAR TitleUpper =
            UPPER ( 'Employees'[Title] )
        VAR Series =
            'Employees'[Occ Series Current]
        VAR PCN =
            'Employees'[Manpower PCN]
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
        dataType: int64
        summarizeBy: none

    column 'Tenure Years' =
        VAR StartDate =
            'Employees'[Dt Arrived Personnel Office]
        VAR SnapDate =
            'Employees'[Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( StartDate )
                    && NOT ISBLANK ( SnapDate ),
                DATEDIFF ( StartDate, SnapDate, YEAR )
            )
        dataType: double
        summarizeBy: none

    column 'Tenure Band' =
        VAR Tenure =
            'Employees'[Tenure Years]
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
        dataType: string
        summarizeBy: none

    // ==== NEW: measures on Employees ====

    measure 'Start Snapshot Date' =
        MIN ( 'Employees'[Snapshot Date] )
        dataType: dateTime
        formatString: "General Date"

    measure 'End Snapshot Date' =
        MAX ( 'Employees'[Snapshot Date] )
        dataType: dateTime
        formatString: "General Date"

    measure 'On-hand count' =
        CALCULATE (
            DISTINCTCOUNT ( 'Employees'[DoD ID (EDIPI)] ),
            'Employees'[Student/Intern] = 0
        )
        dataType: int64
        formatString: "#,0"

    measure 'OnHand Start' =
        VAR d0 = [Start Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( d0 ),
                CALCULATE (
                    DISTINCTCOUNT ( 'Employees'[DoD ID (EDIPI)] ),
                    'Employees'[Snapshot Date] = d0,
                    'Employees'[Student/Intern] = 0
                )
            )
        dataType: int64
        formatString: "#,0"

    measure 'OnHand End' =
        VAR d1 = [End Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( d1 ),
                CALCULATE (
                    DISTINCTCOUNT ( 'Employees'[DoD ID (EDIPI)] ),
                    'Employees'[Snapshot Date] = d1,
                    'Employees'[Student/Intern] = 0
                )
            )
        dataType: int64
        formatString: "#,0"

    measure 'Average On-hand count' =
        DIVIDE ( [OnHand Start] + [OnHand End], 2.0 )
        dataType: double
        formatString: "#,0.0"

    measure 'Remain Count' =
        VAR d0 = [Start Snapshot Date]
        VAR d1 = [End Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( d0 )
                    && NOT ISBLANK ( d1 ),
                VAR StartSet =
                    CALCULATETABLE (
                        VALUES ( 'Employees'[DoD ID (EDIPI)] ),
                        'Employees'[Snapshot Date] = d0,
                        'Employees'[Student/Intern] = 0
                    )
                VAR EndSet =
                    CALCULATETABLE (
                        VALUES ( 'Employees'[DoD ID (EDIPI)] ),
                        'Employees'[Snapshot Date] = d1,
                        'Employees'[Student/Intern] = 0
                    )
                VAR CommonIDs =
                    INTERSECT ( StartSet, EndSet )
                RETURN
                    COUNTROWS ( CommonIDs )
            )
        dataType: int64
        formatString: "#,0"

    measure 'Retention Rate' =
        DIVIDE ( [Remain Count], [OnHand Start] )
        dataType: double
        formatString: "0.0%"

    measure 'New Hire On-hand Start (<2y)' =
        VAR d0 = [Start Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( d0 ),
                CALCULATE (
                    DISTINCTCOUNT ( 'Employees'[DoD ID (EDIPI)] ),
                    'Employees'[Snapshot Date] = d0,
                    'Employees'[Student/Intern] = 0,
                    'Employees'[Tenure Years] < 2
                )
            )
        dataType: int64
        formatString: "#,0"

    measure 'New Hire Share Start (<2y)' =
        DIVIDE ( [New Hire On-hand Start (<2y)], [OnHand Start] )
        dataType: double
        formatString: "0.0%"

Then your existing:

    partition Employees = m
        ...

stays as-is below this block.

⸻

2. PersonnelActions – all calc columns and measures (no expression:)

Same idea: replace everything between the last physical column and partition PersonnelActions = m with this:

    // ---- existing physical columns above ----
    column DT_ARR_SVCG_CCPO
        dataType: string
        summarizeBy: none
        sourceColumn: DT_ARR_SVCG_CCPO

    // ==== NEW: calc columns ====

    column 'From ORG Trim' =
        VAR line = 'PersonnelActions'[FROM_POSITION_ORG_LINE6_14]
        VAR afterDash =
            IF (
                NOT ISBLANK ( line )
                    && FIND ( "-", line, 1, 0 ) > 0,
                TRIM ( RIGHT ( line, LEN ( line ) - FIND ( "-", line, 1, 0 ) ) ),
                TRIM ( line )
            )
        RETURN
            afterDash
        dataType: string
        summarizeBy: none

    column 'To ORG Trim' =
        VAR line = 'PersonnelActions'[TO_POSITION_ORG_LINE6_22]
        VAR afterDash =
            IF (
                NOT ISBLANK ( line )
                    && FIND ( "-", line, 1, 0 ) > 0,
                TRIM ( RIGHT ( line, LEN ( line ) - FIND ( "-", line, 1, 0 ) ) ),
                TRIM ( line )
            )
        RETURN
            afterDash
        dataType: string
        summarizeBy: none

    column 'From ORG 2 Digit' =
        VAR trimmed = 'PersonnelActions'[From ORG Trim]
        RETURN IF ( NOT ISBLANK ( trimmed ), LEFT ( trimmed, 2 ) )
        dataType: string
        summarizeBy: none

    column 'To ORG 2 Digit' =
        VAR trimmed = 'PersonnelActions'[To ORG Trim]
        RETURN IF ( NOT ISBLANK ( trimmed ), LEFT ( trimmed, 2 ) )
        dataType: string
        summarizeBy: none

    column 'From ORG' =
        RELATED ( OrgMAPPING[Org] )
        dataType: string
        summarizeBy: none

    column 'From Center' =
        RELATED ( OrgMAPPING[Center] )
        dataType: string
        summarizeBy: none

    column 'To ORG' =
        VAR Code = 'PersonnelActions'[To ORG 2 Digit]
        RETURN
            CALCULATE (
                MAX ( OrgMAPPING[Org] ),
                OrgMAPPING[Org Code] = Code
            )
        dataType: string
        summarizeBy: none

    column 'To Center' =
        VAR Code = 'PersonnelActions'[To ORG 2 Digit]
        RETURN
            CALCULATE (
                MAX ( OrgMAPPING[Center] ),
                OrgMAPPING[Org Code] = Code
            )
        dataType: string
        summarizeBy: none

    column 'Compare From and To Organization' =
        VAR fromOrg = 'PersonnelActions'[From ORG]
        VAR toOrg   = 'PersonnelActions'[To ORG]
        VAR NOA     = 'PersonnelActions'[FIRST_NOA_CODE_5A]
        VAR NOA3    = LEFT ( NOA, 3 )
        VAR ForceDifferent =
            NOA3 = "300"
                || NOA = "T352"
                || NOA = "CAO"
        RETURN
            IF (
                ISBLANK ( fromOrg ) || ISBLANK ( toOrg ),
                BLANK (),
                IF (
                    ForceDifferent,
                    "DIFFERENT",
                    IF ( fromOrg = toOrg, "SAME", "DIFFERENT" )
                )
            )
        dataType: string
        summarizeBy: none

    column 'Compare Job Series Changes' =
        VAR fromSeries = 'PersonnelActions'[FROM_OCC_CODE_9]
        VAR toSeries   = 'PersonnelActions'[TO_OCC_CODE_17]
        VAR NOA        = 'PersonnelActions'[FIRST_NOA_CODE_5A]
        VAR NOA3       = LEFT ( NOA, 3 )
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
        dataType: string
        summarizeBy: none

    column 'Student/Intern' =
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
        dataType: int64
        summarizeBy: none

    column 'Internal/External Movement' =
        VAR NOA  = 'PersonnelActions'[FIRST_NOA_CODE_5A]
        VAR NOA3 = LEFT ( NOA, 3 )
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
        dataType: string
        summarizeBy: none

    column 'Departure Reason' =
        VAR lac2 = 'PersonnelActions'[FIRST_ACTION_LA_CODE2_5E]
        RETURN
            SWITCH (
                TRUE (),
                lac2 = "ADR", "DoD-DRP2",
                lac2 = "RZM", "DoD-DRP1",
                lac2 = "AZM", "VERA/VSIP",
                'PersonnelActions'[FIRST_NOA_DESC_5B]
            )
        dataType: string
        summarizeBy: none

    // ==== NEW: measures on PersonnelActions ====

    measure Losses =
        VAR d0 = 'Employees'[Start Snapshot Date]
        VAR d1 = 'Employees'[End Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( d0 )
                    && NOT ISBLANK ( d1 ),
                CALCULATE (
                    COUNTROWS ( 'PersonnelActions' ),
                    'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                    'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                    VAR NOA = 'PersonnelActions'[FIRST_NOA_CODE_5A]
                    VAR NOA3 = LEFT ( NOA, 3 )
                    RETURN
                        NOA3 = "300"
                            || NOA IN { "T352", "501", "570", "702", "721", "CAO" },
                    'PersonnelActions'[Compare From and To Organization] = "DIFFERENT",
                    'PersonnelActions'[Student/Intern] = 0
                )
            )
        dataType: int64
        formatString: "#,0"

    measure 'Agency Losses' =
        VAR d0 = 'Employees'[Start Snapshot Date]
        VAR d1 = 'Employees'[End Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( d0 )
                    && NOT ISBLANK ( d1 ),
                CALCULATE (
                    COUNTROWS ( 'PersonnelActions' ),
                    'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                    'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                    VAR NOA = 'PersonnelActions'[FIRST_NOA_CODE_5A]
                    VAR NOA3 = LEFT ( NOA, 3 )
                    RETURN
                        NOA3 = "300"
                            || NOA = "T352"
                            || NOA = "CAO",
                    'PersonnelActions'[Student/Intern] = 0
                )
            )
        dataType: int64
        formatString: "#,0"

    measure 'Attrition Rate' =
        DIVIDE ( [Losses], 'Employees'[Average On-hand count] )
        dataType: double
        formatString: "0.0%"

    measure 'Agency Attrition Rate' =
        DIVIDE ( [Agency Losses], 'Employees'[Average On-hand count] )
        dataType: double
        formatString: "0.0%"

    measure 'Non-voluntary Losses' =
        VAR d0 = 'Employees'[Start Snapshot Date]
        VAR d1 = 'Employees'[End Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( d0 )
                    && NOT ISBLANK ( d1 ),
                CALCULATE (
                    COUNTROWS ( 'PersonnelActions' ),
                    'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                    'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                    'PersonnelActions'[FIRST_NOA_CODE_5A] IN { "330", "355", "357", "385" },
                    'PersonnelActions'[Student/Intern] = 0
                )
            )
        dataType: int64
        formatString: "#,0"

    measure 'Non-voluntary Attrition' =
        DIVIDE ( [Non-voluntary Losses], 'Employees'[Average On-hand count] )
        dataType: double
        formatString: "0.0%"

    measure 'Voluntary Attrition' =
        [Attrition Rate] - [Non-voluntary Attrition]
        dataType: double
        formatString: "0.0%"

    measure 'Internal Moves' =
        VAR d0 = 'Employees'[Start Snapshot Date]
        VAR d1 = 'Employees'[End Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( d0 )
                    && NOT ISBLANK ( d1 ),
                CALCULATE (
                    COUNTROWS ( 'PersonnelActions' ),
                    'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                    'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                    'PersonnelActions'[FIRST_NOA_CODE_5A] IN { "501", "570", "702", "721" },
                    'PersonnelActions'[Compare From and To Organization] = "DIFFERENT",
                    'PersonnelActions'[Student/Intern] = 0
                )
            )
        dataType: int64
        formatString: "#,0"

    measure 'External Losses' =
        VAR d0 = 'Employees'[Start Snapshot Date]
        VAR d1 = 'Employees'[End Snapshot Date]
        RETURN
            IF (
                NOT ISBLANK ( d0 )
                    && NOT ISBLANK ( d1 ),
                CALCULATE (
                    COUNTROWS ( 'PersonnelActions' ),
                    'PersonnelActions'[EFFECTIVE_DATE_4] >= d0,
                    'PersonnelActions'[EFFECTIVE_DATE_4] <= d1,
                    VAR NOA = 'PersonnelActions'[FIRST_NOA_CODE_5A]
                    VAR NOA3 = LEFT ( NOA, 3 )
                    RETURN
                        NOA3 = "300"
                            || NOA = "
