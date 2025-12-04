createOrReplace

	model Model
		culture: en-US
		defaultPowerBIDataSourceVersion: powerBI_V3
		sourceQueryCulture: en-US
		dataAccessOptions
			legacyRedirects
			returnErrorValuesAsNull

		table Employees
			lineageTag: fa6f3eb5-eb6f-405f-85e3-bf4d04a47560

			column SSAN_EMPL_CON_NR
				dataType: string
				lineageTag: 878e8cfa-1914-4ac2-8e9a-13c6c201b5ee
				summarizeBy: none
				sourceColumn: SSAN_EMPL_CON_NR

				annotation SummarizationSetBy = Automatic

			column 'DoD ID (EDIPI)'
				dataType: string
				lineageTag: 4faef5d1-b8f1-4bb4-8314-540b47c1c910
				summarizeBy: none
				sourceColumn: DoD ID (EDIPI)

				annotation SummarizationSetBy = Automatic

			column 'Employee Num'
				dataType: string
				lineageTag: 3b3975e1-73d9-47ac-b437-5b51c9ca3e8d
				summarizeBy: none
				sourceColumn: Employee Num

				annotation SummarizationSetBy = Automatic

			column 'Position ID'
				dataType: string
				lineageTag: b1e3a0b4-c55e-4caf-a212-30786006e936
				summarizeBy: none
				sourceColumn: Position ID

				annotation SummarizationSetBy = Automatic

			column CPCN
				dataType: string
				lineageTag: e43bf4b9-50c4-4989-9d70-dcfd8b42e98c
				summarizeBy: none
				sourceColumn: CPCN

				annotation SummarizationSetBy = Automatic

			column 'Dt of Birth'
				dataType: string
				lineageTag: 7028b128-b9d5-47d2-9667-4c58090a112a
				summarizeBy: none
				sourceColumn: Dt of Birth

				annotation SummarizationSetBy = Automatic

			column Sex
				dataType: string
				lineageTag: 899768e9-a0ea-44b8-9d01-9189c7fae0be
				summarizeBy: none
				sourceColumn: Sex

				annotation SummarizationSetBy = Automatic

			column 'Step or Rate'
				dataType: string
				lineageTag: da3a6e13-00fa-4b49-aa33-dabd3019420f
				summarizeBy: none
				sourceColumn: Step or Rate

				annotation SummarizationSetBy = Automatic

			column 'SCD Leave'
				dataType: string
				lineageTag: 4953cdd5-76f3-4af6-b157-ce569f68e228
				summarizeBy: none
				sourceColumn: SCD Leave

				annotation SummarizationSetBy = Automatic

			column 'SCD Civilian'
				dataType: string
				lineageTag: 05150135-e094-4848-80ac-bbddd4c89ff5
				summarizeBy: none
				sourceColumn: SCD Civilian

				annotation SummarizationSetBy = Automatic

			column 'SCD RIF'
				dataType: string
				lineageTag: 74e75819-c8ca-4b5b-b792-95a28c27759e
				summarizeBy: none
				sourceColumn: SCD RIF

				annotation SummarizationSetBy = Automatic

			column SALARY_HR
				dataType: string
				lineageTag: abc755c1-efea-4ec6-9492-f69c126fb6aa
				summarizeBy: none
				sourceColumn: SALARY_HR

				annotation SummarizationSetBy = Automatic

			column 'Pay Plan Current'
				dataType: string
				lineageTag: ef302507-f3f1-46c9-831b-93cf901b5bde
				summarizeBy: none
				sourceColumn: Pay Plan Current

				annotation SummarizationSetBy = Automatic

			column 'Occ Series Current'
				dataType: string
				lineageTag: b62502e1-afc9-4b0e-838f-86934b759fd3
				summarizeBy: none
				sourceColumn: Occ Series Current

				annotation SummarizationSetBy = Automatic

			column NAME-FULL
				dataType: string
				lineageTag: e884ef2c-b566-417e-8fb8-8b666ed52f57
				summarizeBy: none
				sourceColumn: NAME-FULL

				annotation SummarizationSetBy = Automatic

			column 'Telework Indicator'
				dataType: string
				lineageTag: 5b786785-06b5-4d4e-b5e6-d059ca0789b1
				summarizeBy: none
				sourceColumn: Telework Indicator

				annotation SummarizationSetBy = Automatic

			column 'Telework Indicator Desc'
				dataType: string
				lineageTag: bca8f43f-9870-424b-abc9-100cac5c23f0
				summarizeBy: none
				sourceColumn: Telework Indicator Desc

				annotation SummarizationSetBy = Automatic

			column 'Telework Eligibility'
				dataType: string
				lineageTag: 99f4676d-c4d1-4d6d-8ff0-51749619856b
				summarizeBy: none
				sourceColumn: Telework Eligibility

				annotation SummarizationSetBy = Automatic

			column 'Telework Eligibility Desc'
				dataType: string
				lineageTag: 413185fe-3371-4589-bc26-119306952f39
				summarizeBy: none
				sourceColumn: Telework Eligibility Desc

				annotation SummarizationSetBy = Automatic

			column 'Handicap Code'
				dataType: string
				lineageTag: db7ca35c-ad77-4269-8113-7813dcb390c0
				summarizeBy: none
				sourceColumn: Handicap Code

				annotation SummarizationSetBy = Automatic

			column 'Grade or Level'
				dataType: string
				lineageTag: 3b4bc102-6659-4481-b23d-da29bdb182c6
				summarizeBy: none
				sourceColumn: Grade or Level

				annotation SummarizationSetBy = Automatic

			column TARGET_GR_CIV
				dataType: string
				lineageTag: 72f5bd81-65da-4c22-8a2d-110fdc5cbd1a
				summarizeBy: none
				sourceColumn: TARGET_GR_CIV

				annotation SummarizationSetBy = Automatic

			column 'WGI Last Equivalent Increase'
				dataType: string
				lineageTag: 996c60dd-13cc-402d-8043-a9cfa9537194
				summarizeBy: none
				sourceColumn: WGI Last Equivalent Increase

				annotation SummarizationSetBy = Automatic

			column 'Dt Last Promotion'
				dataType: string
				lineageTag: 8134f1aa-f8db-43ea-996a-f603bbd33aef
				summarizeBy: none
				sourceColumn: Dt Last Promotion

				annotation SummarizationSetBy = Automatic

			column Tenure
				dataType: string
				lineageTag: 47b2cfe5-f512-413a-b382-195524b84cf2
				summarizeBy: none
				sourceColumn: Tenure

				annotation SummarizationSetBy = Automatic

			column 'Country World Citizenship'
				dataType: string
				lineageTag: 7f27610c-29b7-46c9-96fc-a0b1b8b60163
				summarizeBy: none
				sourceColumn: Country World Citizenship

				annotation SummarizationSetBy = Automatic

			column CCPO_ID
				dataType: string
				lineageTag: 9d6f3412-6cd1-48f6-9629-3129b9bad724
				summarizeBy: none
				sourceColumn: CCPO_ID

				annotation SummarizationSetBy = Automatic

			column 'Competitive Level'
				dataType: string
				lineageTag: 640d1f27-6647-4a49-9814-d0c5a1358b73
				summarizeBy: none
				sourceColumn: Competitive Level

				annotation SummarizationSetBy = Automatic

			column Location
				dataType: string
				lineageTag: b7546bf8-fc58-4d43-a211-a9aac9c11b05
				summarizeBy: none
				sourceColumn: Location

				annotation SummarizationSetBy = Automatic

			column 'Manpower PCN'
				dataType: string
				lineageTag: b65856d6-c944-4f88-8474-728e2ef5109d
				summarizeBy: none
				sourceColumn: Manpower PCN

				annotation SummarizationSetBy = Automatic

			column 'PAS Code'
				dataType: string
				lineageTag: b37a8902-0a6d-48a6-bf44-ad4de0b2d704
				summarizeBy: none
				sourceColumn: PAS Code

				annotation SummarizationSetBy = Automatic

			column PAS_AUTH
				dataType: string
				lineageTag: 80169164-c14b-4cfb-afc2-a130ee1e0da8
				summarizeBy: none
				sourceColumn: PAS_AUTH

				annotation SummarizationSetBy = Automatic

			column 'Personnel Office ID'
				dataType: string
				lineageTag: faf4ae5b-9df7-42cd-8db4-5648a86885e5
				summarizeBy: none
				sourceColumn: Personnel Office ID

				annotation SummarizationSetBy = Automatic

			column 'Org Structure ID'
				dataType: string
				lineageTag: ce238c9a-cace-4d95-b961-6f1235829e6d
				summarizeBy: none
				sourceColumn: Org Structure ID

				annotation SummarizationSetBy = Automatic

			column Title
				dataType: string
				lineageTag: 2a633ac9-bd70-4f30-b640-24011f7b1244
				summarizeBy: none
				sourceColumn: Title

				annotation SummarizationSetBy = Automatic

			column 'Psn Sensitivity'
				dataType: string
				lineageTag: 75e459be-511d-4054-8e95-da02fd5707c8
				summarizeBy: none
				sourceColumn: Psn Sensitivity

				annotation SummarizationSetBy = Automatic

			column 'Unit ID Code'
				dataType: string
				lineageTag: 6183d015-830d-4aa7-ba26-42577e3055bb
				summarizeBy: none
				sourceColumn: Unit ID Code

				annotation SummarizationSetBy = Automatic

			column 'Basic Salary Rate'
				dataType: string
				lineageTag: 94fe824e-5144-4541-b64f-5daa40c9b9fc
				summarizeBy: none
				sourceColumn: Basic Salary Rate

				annotation SummarizationSetBy = Automatic

			column 'Locality Pay Rate'
				dataType: string
				lineageTag: 0e53abca-6bf1-4413-bc8a-1d1e2f89ec4a
				summarizeBy: none
				sourceColumn: Locality Pay Rate

				annotation SummarizationSetBy = Automatic

			column 'Total Pay Amt'
				dataType: string
				lineageTag: 294166d3-8976-4115-8034-d87e07e3c3e3
				summarizeBy: none
				sourceColumn: Total Pay Amt

				annotation SummarizationSetBy = Automatic

			column 'Pay Rate Determinant'
				dataType: string
				lineageTag: 3840d52e-8808-4fa0-945f-8613697518d2
				summarizeBy: none
				sourceColumn: Pay Rate Determinant

				annotation SummarizationSetBy = Automatic

			column 'Bargaining Unit Status'
				dataType: string
				lineageTag: 53282b91-f9af-47c6-9ad6-e03749f2bf5b
				summarizeBy: none
				sourceColumn: Bargaining Unit Status

				annotation SummarizationSetBy = Automatic

			column 'Payroll Office ID'
				dataType: string
				lineageTag: 9c2df239-b9ac-486e-8ff2-52cbd20b1a4c
				summarizeBy: none
				sourceColumn: Payroll Office ID

				annotation SummarizationSetBy = Automatic

			column 'Pay Basis'
				dataType: string
				lineageTag: ba03acbc-aeb5-42b0-84a5-93854c23923e
				summarizeBy: none
				sourceColumn: Pay Basis

				annotation SummarizationSetBy = Automatic

			column 'Pay Table ID'
				dataType: string
				lineageTag: 576bbf4f-7c58-4e7b-b8f8-5afb0b20e896
				summarizeBy: none
				sourceColumn: Pay Table ID

				annotation SummarizationSetBy = Automatic

			column 'Work Schedule'
				dataType: string
				lineageTag: fffa5182-53c0-450c-83db-674010ee1e1a
				summarizeBy: none
				sourceColumn: Work Schedule

				annotation SummarizationSetBy = Automatic

			column 'Reserve Category'
				dataType: string
				lineageTag: 64655111-e852-4501-8ba8-643c2b551005
				summarizeBy: none
				sourceColumn: Reserve Category

				annotation SummarizationSetBy = Automatic

			column 'Veterans Preference'
				dataType: string
				lineageTag: 47f1dcb3-d131-4d74-b6c0-7cae29127900
				summarizeBy: none
				sourceColumn: Veterans Preference

				annotation SummarizationSetBy = Automatic

			column 'Veterans Status'
				dataType: string
				lineageTag: eadb9e11-c16c-4c7a-948c-8c5ed28244fa
				summarizeBy: none
				sourceColumn: Veterans Status

				annotation SummarizationSetBy = Automatic

			column 'Veteran Status Desc'
				dataType: string
				lineageTag: 1f82897b-4555-4bc9-96de-9e2f14b6be35
				summarizeBy: none
				sourceColumn: Veteran Status Desc

				annotation SummarizationSetBy = Automatic

			column 'Dt Appraisal  Effective'
				dataType: string
				lineageTag: 3f322818-f086-4d6d-8146-d77ff35e394a
				summarizeBy: none
				sourceColumn: Dt Appraisal  Effective

				annotation SummarizationSetBy = Automatic

			column 'Rating of Record'
				dataType: string
				lineageTag: 2b520760-dfc4-483d-831f-a2cc130303f2
				summarizeBy: none
				sourceColumn: Rating of Record

				annotation SummarizationSetBy = Automatic

			column 'Appraisal Type'
				dataType: string
				lineageTag: 26557148-3961-4303-ab7c-2bd4c16bc328
				summarizeBy: none
				sourceColumn: Appraisal Type

				annotation SummarizationSetBy = Automatic

			column 'Supervisory Status'
				dataType: string
				lineageTag: 4c391024-0249-46c1-a269-13111bb6aed7
				summarizeBy: none
				sourceColumn: Supervisory Status

				annotation SummarizationSetBy = Automatic

			column 'Dt Assigned Current Agency'
				dataType: string
				lineageTag: 0d5fbcf5-3ba6-413d-b936-e08a3b192450
				summarizeBy: none
				sourceColumn: Dt Assigned Current Agency

				annotation SummarizationSetBy = Automatic

			column 'Retirement Plan'
				dataType: string
				lineageTag: 9dba6398-3478-44c0-adf7-69a5ce9c9a3c
				summarizeBy: none
				sourceColumn: Retirement Plan

				annotation SummarizationSetBy = Automatic

			column 'Appointment Type'
				dataType: string
				lineageTag: e5e063e8-4f91-4cec-83f4-83f3e6fcf3a7
				summarizeBy: none
				sourceColumn: Appointment Type

				annotation SummarizationSetBy = Automatic

			column 'Current Appointment Auth 1'
				dataType: string
				lineageTag: f5b10266-de95-4bad-9a23-6101ecfda9df
				summarizeBy: none
				sourceColumn: Current Appointment Auth 1

				annotation SummarizationSetBy = Automatic

			column 'Current Appointment Auth 2'
				dataType: string
				lineageTag: 1fc7e9ea-1505-4129-b5b2-fa943d6f9a6e
				summarizeBy: none
				sourceColumn: Current Appointment Auth 2

				annotation SummarizationSetBy = Automatic

			column 'Duty Status'
				dataType: string
				lineageTag: 98904098-37bd-4098-9203-c32d2e296b05
				summarizeBy: none
				sourceColumn: Duty Status

				annotation SummarizationSetBy = Automatic

			column 'Dt Arrived Personnel Office'
				dataType: string
				lineageTag: ee9ca195-e674-42a7-a1be-df0435f31f7e
				summarizeBy: none
				sourceColumn: Dt Arrived Personnel Office

				annotation SummarizationSetBy = Automatic

			column 'Psn Seq'
				dataType: string
				lineageTag: a4966a0a-4937-4a34-9557-75e1aa82483b
				summarizeBy: none
				sourceColumn: Psn Seq

				annotation SummarizationSetBy = Automatic

			column 'WGI Dt Due'
				dataType: string
				lineageTag: 400f13f4-b58d-4d25-aa72-a652e9b06d55
				summarizeBy: none
				sourceColumn: WGI Dt Due

				annotation SummarizationSetBy = Automatic

			column FUNDING_CATEGORIES
				dataType: string
				lineageTag: 02ee5855-4f79-49ae-a06a-85f1fc31c7d7
				summarizeBy: none
				sourceColumn: FUNDING_CATEGORIES

				annotation SummarizationSetBy = Automatic

			column PAYROLL_COST_CODE
				dataType: string
				lineageTag: 8b251fcd-040b-4609-8d32-a33fcc5ace52
				summarizeBy: none
				sourceColumn: PAYROLL_COST_CODE

				annotation SummarizationSetBy = Automatic

			column 'Occupation Category Code'
				dataType: string
				lineageTag: 12474e65-d9d1-419b-b02a-e3ba49471fab
				summarizeBy: none
				sourceColumn: Occupation Category Code

				annotation SummarizationSetBy = Automatic

			column 'FLSA Category'
				dataType: string
				lineageTag: 10de6b25-79a1-4d7f-8de9-ff1a7111b2d7
				summarizeBy: none
				sourceColumn: FLSA Category

				annotation SummarizationSetBy = Automatic

			column 'Ethnicity and Race Full Desc'
				dataType: string
				lineageTag: 3a49741b-634e-40fc-9d67-fe1101d424f2
				summarizeBy: none
				sourceColumn: Ethnicity and Race Full Desc

				annotation SummarizationSetBy = Automatic

			column 'Ethnicity and Race Full'
				dataType: string
				lineageTag: 3847925a-df98-4675-98e9-2077fa384c1b
				summarizeBy: none
				sourceColumn: Ethnicity and Race Full

				annotation SummarizationSetBy = Automatic

			column 'IA Category'
				dataType: string
				lineageTag: cf31511e-1f08-462c-a62d-e6c000226cfa
				summarizeBy: none
				sourceColumn: IA Category

				annotation SummarizationSetBy = Automatic

			column IA_CATEGORY_TEXT
				dataType: string
				lineageTag: faf5be09-d2e6-4823-8d0e-69d299669ab5
				summarizeBy: none
				sourceColumn: IA_CATEGORY_TEXT

				annotation SummarizationSetBy = Automatic

			column 'IA Level'
				dataType: string
				lineageTag: 735cfb7b-e906-4df6-8b15-49ac93ea39e0
				summarizeBy: none
				sourceColumn: IA Level

				annotation SummarizationSetBy = Automatic

			column IA_LEVEL_TEXT
				dataType: string
				lineageTag: 6c4897f5-d88a-4061-8516-765138ce0e87
				summarizeBy: none
				sourceColumn: IA_LEVEL_TEXT

				annotation SummarizationSetBy = Automatic

			column 'IA Duty'
				dataType: string
				lineageTag: 142fcafb-8644-464b-8515-05254ed9ac9d
				summarizeBy: none
				sourceColumn: IA Duty

				annotation SummarizationSetBy = Automatic

			column IA_DUTY_TEXT
				dataType: string
				lineageTag: e5556262-45d1-4ece-97b7-6e09b81b2c45
				summarizeBy: none
				sourceColumn: IA_DUTY_TEXT

				annotation SummarizationSetBy = Automatic

			column 'Psn Specialty Code'
				dataType: string
				lineageTag: 10a9b7b7-5dfb-49bf-b75f-f5a27c766b11
				summarizeBy: none
				sourceColumn: Psn Specialty Code

				annotation SummarizationSetBy = Automatic

			column 'IA Workforce Category'
				dataType: string
				lineageTag: 0cd69c0b-c348-403b-ab10-c11fe54d8e7d
				summarizeBy: none
				sourceColumn: IA Workforce Category

				annotation SummarizationSetBy = Automatic

			column 'IA Specialty Type'
				dataType: string
				lineageTag: fb62cc72-21ad-4387-9932-a67e5538ef6f
				summarizeBy: none
				sourceColumn: IA Specialty Type

				annotation SummarizationSetBy = Automatic

			column 'IA Workforce Level'
				dataType: string
				lineageTag: 2ddbed0a-ad88-41e8-a891-2e4a09aa0391
				summarizeBy: none
				sourceColumn: IA Workforce Level

				annotation SummarizationSetBy = Automatic

			column 'Dt Appt Ltr Signed'
				dataType: string
				lineageTag: 89028768-a9d2-41ea-adb4-94239f3110d0
				summarizeBy: none
				sourceColumn: Dt Appt Ltr Signed

				annotation SummarizationSetBy = Automatic

			column 'Cumulative Sustainment Tng Hrs'
				dataType: string
				lineageTag: a3068296-4230-4b7c-a716-76de7e66c7ce
				summarizeBy: none
				sourceColumn: Cumulative Sustainment Tng Hrs

				annotation SummarizationSetBy = Automatic

			column 'In Sourced Indicator'
				dataType: string
				lineageTag: 2bdb723a-c139-4d24-aa5a-6acc82953ca7
				summarizeBy: none
				sourceColumn: In Sourced Indicator

				annotation SummarizationSetBy = Automatic

			column 'In Sourced Date'
				dataType: string
				lineageTag: fcc5e019-b8cc-4777-af15-5fcb9589802a
				summarizeBy: none
				sourceColumn: In Sourced Date

				annotation SummarizationSetBy = Automatic

			column 'Orig In Source Seq Num'
				dataType: string
				lineageTag: 8d2ec0de-a71a-4a35-9022-ccad3e402e3a
				summarizeBy: none
				sourceColumn: Orig In Source Seq Num

				annotation SummarizationSetBy = Automatic

			column 'Billet OUID'
				dataType: string
				lineageTag: b9fce815-1686-43f5-8f9e-f010ace0e4e0
				summarizeBy: none
				sourceColumn: Billet OUID

				annotation SummarizationSetBy = Automatic

			column 'Billet OUID Start Date'
				dataType: string
				lineageTag: 896ebd60-9cfe-4ba3-a765-6ac1acc39558
				summarizeBy: none
				sourceColumn: Billet OUID Start Date

				annotation SummarizationSetBy = Automatic

			column 'Pers Primary Work Role'
				dataType: string
				lineageTag: a2b3c158-1db0-4859-be13-b4f2f6417216
				summarizeBy: none
				sourceColumn: Pers Primary Work Role

				annotation SummarizationSetBy = Automatic

			column 'Pers Primary Work Role Desc'
				dataType: string
				lineageTag: 9ed6a1c4-c0c8-47eb-8897-50b324adb4e7
				summarizeBy: none
				sourceColumn: Pers Primary Work Role Desc

				annotation SummarizationSetBy = Automatic

			column 'Pers Additional Work Role 1'
				dataType: string
				lineageTag: 1a9287f0-f98b-4099-aa7c-b15dbf32a20f
				summarizeBy: none
				sourceColumn: Pers Additional Work Role 1

				annotation SummarizationSetBy = Automatic

			column 'Pers Additional Work Role 1 Desc'
				dataType: string
				lineageTag: 89a1c265-98b8-4ccc-95b0-519954b8c888
				summarizeBy: none
				sourceColumn: Pers Additional Work Role 1 Desc

				annotation SummarizationSetBy = Automatic

			column 'Pers Additional Work Role 2'
				dataType: string
				lineageTag: 79fb6a73-3e76-4ae5-9256-43ead6757d8c
				summarizeBy: none
				sourceColumn: Pers Additional Work Role 2

				annotation SummarizationSetBy = Automatic

			column 'Pers Additional Work Role 2 Desc'
				dataType: string
				lineageTag: 52e0b743-9210-43cc-9738-29d38c04615f
				summarizeBy: none
				sourceColumn: Pers Additional Work Role 2 Desc

				annotation SummarizationSetBy = Automatic

			// ============================================
			// Created Columns (Employees)
			// ============================================

			column 'Snapshot Date'
				dataType: dateTime
				summarizeBy: none
				sourceColumn: Snapshot Date

				annotation SummarizationSetBy = Automatic

			column 'ORG First 2'
				dataType: string
				summarizeBy: none
				expression: =
					IF (
					    NOT ISBLANK ( 'Employees'['Org Structure ID'] ),
					    LEFT ( 'Employees'['Org Structure ID'], 2 )
					)

				annotation SummarizationSetBy = Automatic

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
					    IF (
					        ( IsStudentTitle || IsInternTitle || Is99Series )
					            && NOT IsRGVT,
					        1,
					        0
					    )

				annotation SummarizationSetBy = Automatic

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

				annotation SummarizationSetBy = Automatic

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

				annotation SummarizationSetBy = Automatic

			// ============================================
			// Measures (Employees) - Retention & Headcount
			// ============================================

			measure 'Start Snapshot Date' =
					MIN ( 'Employees'['Snapshot Date'] )
				formatString: General Date

			measure 'End Snapshot Date' =
					MAX ( 'Employees'['Snapshot Date'] )
				formatString: General Date

			measure 'On-hand count' =
					CALCULATE (
					    DISTINCTCOUNT ( 'Employees'[DoD ID (EDIPI)] ),
					    'Employees'['Student/Intern'] = 0
					)
				formatString: #,0

			measure 'OnHand Start' =
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
				formatString: #,0

			measure 'OnHand End' =
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
				formatString: #,0

			measure 'Average On-hand count' =
					DIVIDE ( [OnHand Start] + [OnHand End], 2.0 )
				formatString: #,0.0

			measure 'Remain Count' =
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
				formatString: #,0

			measure 'Retention Rate' =
					DIVIDE ( [Remain Count], [OnHand Start] )
				formatString: 0.0%

			measure 'New Hire On-hand Start (<2y)' =
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
				formatString: #,0

			measure 'New Hire Share Start (<2y)' =
					DIVIDE ( [New Hire On-hand Start (<2y)], [OnHand Start] )
				formatString: 0.0%

			partition Employees = m
				mode: import
				source =
						let
						    Source =
						        SharePoint.Files(
						            "https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/",
						            [ ApiVersion = 15 ]
						        ),

						    #"Filtered Rows" =
						        Table.SelectRows(
						            Source,
						            each [Folder Path]
						                = "https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/Shared Documents/Attrition-Retention Automation/TEST/NewTest/"
						        ),

						    #"Filtered Rows1" =
						        Table.SelectRows(
						            #"Filtered Rows",
						            each Text.StartsWith( [Name], "DISA" )
						        ),

						    #"Filtered Hidden Files1" =
						        Table.SelectRows(
						            #"Filtered Rows1",
						            each [Attributes]?[Hidden]? <> true
						        ),

						    #"Added Snapshot Date" =
						        Table.AddColumn(
						            #"Filtered Hidden Files1",
						            "Snapshot Date",
						            each
						                let
						                    name = [Name],
						                    baseName = Text.BeforeDelimiter( name, "." ),
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

						    #"Removed Columns" =
						        Table.RemoveColumns(
						            #"Added Snapshot Date",
						            { "Extension", "Date accessed", "Date modified", "Date created", "Attributes", "Folder Path" }
						        ),

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

		table PersonnelActions
			lineageTag: 7e2dedf1-255d-4074-823d-f7d9965626ca

			column EFFECTIVE_DATE_4
				dataType: dateTime
				lineageTag: f3504ef3-8573-44fb-977d-12b9256739b1
				summarizeBy: none
				sourceColumn: EFFECTIVE_DATE_4

				annotation SummarizationSetBy = Automatic

			column FIRST_NOA_CODE_5A
				dataType: string
				lineageTag: 7bbf6e4c-dece-4ffe-b327-16e06bb2adf2
				summarizeBy: none
				sourceColumn: FIRST_NOA_CODE_5A

				annotation SummarizationSetBy = Automatic

			column FIRST_NOA_DESC_5B
				dataType: string
				lineageTag: 18b51488-067d-4b14-8094-7099a74a4f38
				summarizeBy: none
				sourceColumn: FIRST_NOA_DESC_5B

				annotation SummarizationSetBy = Automatic

			column SECOND_NOA_CODE_6A
				dataType: string
				lineageTag: 0491ac78-b525-4c0c-9711-0b7355f9a802
				summarizeBy: none
				sourceColumn: SECOND_NOA_CODE_6A

				annotation SummarizationSetBy = Automatic

			column SECOND_NOA_DESC_6B
				dataType: string
				lineageTag: 25a8cf4d-f17c-48d0-ac2c-aced23c830da
				summarizeBy: none
				sourceColumn: SECOND_NOA_DESC_6B

				annotation SummarizationSetBy = Automatic

			column SF50_APPROVAL_DATE_49
				dataType: string
				lineageTag: 32875081-6b45-49da-9639-e9bb25f231c5
				summarizeBy: none
				sourceColumn: SF50_APPROVAL_DATE_49

				annotation SummarizationSetBy = Automatic

			column FROM_POSITION_ORG_LINE1_14
				dataType: string
				lineageTag: fa56583e-b3dc-46fb-ac77-8f616ef81245
				summarizeBy: none
				sourceColumn: FROM_POSITION_ORG_LINE1_14

				annotation SummarizationSetBy = Automatic

			column FROM_POSITION_ORG_LINE2_14
				dataType: string
				lineageTag: 62c182b8-0c10-4f57-9abf-0863399d68c8
				summarizeBy: none
				sourceColumn: FROM_POSITION_ORG_LINE2_14

				annotation SummarizationSetBy = Automatic

			column FROM_POSITION_ORG_LINE3_14
				dataType: string
				lineageTag: 6402df21-e54c-40e2-9fdc-4c18327bee78
				summarizeBy: none
				sourceColumn: FROM_POSITION_ORG_LINE3_14

				annotation SummarizationSetBy = Automatic

			column FROM_POSITION_ORG_LINE4_14
				dataType: string
				lineageTag: 0b595066-7f26-45a4-833b-c66eda148149
				summarizeBy: none
				sourceColumn: FROM_POSITION_ORG_LINE4_14

				annotation SummarizationSetBy = Automatic

			column FROM_POSITION_ORG_LINE5_14
				dataType: string
				lineageTag: 99f34b6b-9cb6-4a23-a63f-096b27393ece
				summarizeBy: none
				sourceColumn: FROM_POSITION_ORG_LINE5_14

				annotation SummarizationSetBy = Automatic

			column FROM_POSITION_ORG_LINE6_14
				dataType: string
				lineageTag: 0453af19-4688-4674-a54e-6b97c6406117
				summarizeBy: none
				sourceColumn: FROM_POSITION_ORG_LINE6_14

				annotation SummarizationSetBy = Automatic

			column TO_POSITION_ORG_LINE1_22
				dataType: string
				lineageTag: 01a769b7-8a44-455b-b4b6-338ca9444ae9
				summarizeBy: none
				sourceColumn: TO_POSITION_ORG_LINE1_22

				annotation SummarizationSetBy = Automatic

			column TO_POSITION_ORG_LINE2_22
				dataType: string
				lineageTag: aab53b6a-b5b0-48b3-832f-7a1467049072
				summarizeBy: none
				sourceColumn: TO_POSITION_ORG_LINE2_22

				annotation SummarizationSetBy = Automatic

			column TO_POSITION_ORG_LINE3_22
				dataType: string
				lineageTag: dbffcfaa-5a18-490a-883e-94c1b0fa78bc
				summarizeBy: none
				sourceColumn: TO_POSITION_ORG_LINE3_22

				annotation SummarizationSetBy = Automatic

			column TO_POSITION_ORG_LINE4_22
				dataType: string
				lineageTag: 6595812c-5e1e-457a-b171-29bcad4db6b9
				summarizeBy: none
				sourceColumn: TO_POSITION_ORG_LINE4_22

				annotation SummarizationSetBy = Automatic

			column TO_POSITION_ORG_LINE5_22
				dataType: string
				lineageTag: 7cb9dcf8-3117-4e33-9efc-ef71c25feddc
				summarizeBy: none
				sourceColumn: TO_POSITION_ORG_LINE5_22

				annotation SummarizationSetBy = Automatic

			column TO_POSITION_ORG_LINE6_22
				dataType: string
				lineageTag: 5a418af9-b8d7-4dd1-aaa3-e959dab7477e
				summarizeBy: none
				sourceColumn: TO_POSITION_ORG_LINE6_22

				annotation SummarizationSetBy = Automatic

			column DUTY_STATION_DESC_39
				dataType: string
				lineageTag: 84e0ef34-5668-4bfb-aed6-7d285f87ced1
				summarizeBy: none
				sourceColumn: DUTY_STATION_DESC_39

				annotation SummarizationSetBy = Automatic

			column NAME_PERS_1
				dataType: string
				lineageTag: 823588c8-2f7f-4080-bce8-3bec6a26b300
				summarizeBy: none
				sourceColumn: NAME_PERS_1

				annotation SummarizationSetBy = Automatic

			column FROM_PAY_PLAN_8
				dataType: string
				lineageTag: 133118e9-74f7-4bbd-b606-83e014c94d80
				summarizeBy: none
				sourceColumn: FROM_PAY_PLAN_8

				annotation SummarizationSetBy = Automatic

			column FROM_OCC_CODE_9
				dataType: string
				lineageTag: 245d5b05-f851-4648-9ec6-27996d9e84f9
				summarizeBy: none
				sourceColumn: FROM_OCC_CODE_9

				annotation SummarizationSetBy = Automatic

			column FROM_GRADE_OR_LEVEL_10
				dataType: string
				lineageTag: a2c45a62-440f-49fc-8b72-78578a8f86ee
				summarizeBy: none
				sourceColumn: FROM_GRADE_OR_LEVEL_10

				annotation SummarizationSetBy = Automatic

			column FROM_STEP_OR_RATE_11
				dataType: string
				lineageTag: 12349c63-a88c-43f7-afe1-a7704d60f968
				summarizeBy: none
				sourceColumn: FROM_STEP_OR_RATE_11

				annotation SummarizationSetBy = Automatic

			column FROM_BASIC_PAY_12A
				dataType: string
				lineageTag: 33d3e320-f99e-4c86-8d87-bb84dc2882d5
				summarizeBy: none
				sourceColumn: FROM_BASIC_PAY_12A

				annotation SummarizationSetBy = Automatic

			column FROM_ADJ_BASIC_PAY_12C
				dataType: string
				lineageTag: fe16d171-5b05-48d0-9004-96b8d5629229
				summarizeBy: none
				sourceColumn: FROM_ADJ_BASIC_PAY_12C

				annotation SummarizationSetBy = Automatic

			column FROM_OTHER_PAY_AMOUNT_12D
				dataType: string
				lineageTag: 014889ea-8dbb-403d-97a1-89cb57d42d7e
				summarizeBy: none
				sourceColumn: FROM_OTHER_PAY_AMOUNT_12D

				annotation SummarizationSetBy = Automatic

			column FROM_PAY_BASIS_13
				dataType: string
				lineageTag: fefea3f2-d464-4763-8c42-9ee9efe295eb
				summarizeBy: none
				sourceColumn: FROM_PAY_BASIS_13

				annotation SummarizationSetBy = Automatic

			column TO_PAY_PLAN_16
				dataType: string
				lineageTag: d00998f7-6e42-4bb5-bcd0-87ccfdf2ead6
				summarizeBy: none
				sourceColumn: TO_PAY_PLAN_16

				annotation SummarizationSetBy = Automatic

			column TO_OCC_CODE_17
				dataType: string
				lineageTag: d9664c12-ccc1-40db-b89b-86f3338bb1ea
				summarizeBy: none
				sourceColumn: TO_OCC_CODE_17

				annotation SummarizationSetBy = Automatic

			column TO_GRADE_OR_LEVEL_18
				dataType: string
				lineageTag: f473e33b-5c83-4451-994f-e7b9276ec8cb
				summarizeBy: none
				sourceColumn: TO_GRADE_OR_LEVEL_18

				annotation SummarizationSetBy = Automatic

			column TO_STEP_OR_RATE_19
				dataType: string
				lineageTag: e4282b82-81be-42cc-8e1c-6f29541eba8b
				summarizeBy: none
				sourceColumn: TO_STEP_OR_RATE_19

				annotation SummarizationSetBy = Automatic

			column TO_OTHER_PAY_AMOUNT_20D
				dataType: string
				lineageTag: 96fc76f4-f88e-4359-a5d1-a6ec4642e61d
				summarizeBy: none
				sourceColumn: TO_OTHER_PAY_AMOUNT_20D

				annotation SummarizationSetBy = Automatic

			column TO_PAY_BASIS_21
				dataType: string
				lineageTag: b4ce5c6e-a386-42ee-8528-1b4db26625e0
				summarizeBy: none
				sourceColumn: TO_PAY_BASIS_21

				annotation SummarizationSetBy = Automatic

			column REQUEST_NUMBER
				dataType: string
				lineageTag: 60a0aefe-9d73-40b4-a8f3-fe77a3acc76a
				summarizeBy: none
				sourceColumn: REQUEST_NUMBER

				annotation SummarizationSetBy = Automatic

			column FIRST_ACTION_LA_CODE1_5C
				dataType: string
				lineageTag: 55d93984-0ac8-4a67-90d9-555a5da8b3de
				summarizeBy: none
				sourceColumn: FIRST_ACTION_LA_CODE1_5C

				annotation SummarizationSetBy = Automatic

			column FIRST_ACTION_LA_DESC1_5D
				dataType: string
				lineageTag: c78994ab-961b-4a7c-86dc-61d6f34b5572
				summarizeBy: none
				sourceColumn: FIRST_ACTION_LA_DESC1_5D

				annotation SummarizationSetBy = Automatic

			column FIRST_ACTION_LA_CODE2_5E
				dataType: string
				lineageTag: e913fa33-ca70-4351-ab58-69f059d3c4b1
				summarizeBy: none
				sourceColumn: FIRST_ACTION_LA_CODE2_5E

				annotation SummarizationSetBy = Automatic

			column FIRST_ACTION_LA_DESC2_5F
				dataType: string
				lineageTag: 8b57c3e3-e6be-470e-a521-76bef73c3977
				summarizeBy: none
				sourceColumn: FIRST_ACTION_LA_DESC2_5F

				annotation SummarizationSetBy = Automatic

			column MOST_RECENT_EMPL_SERIES
				dataType: string
				lineageTag: 5c30273e-5ca3-49cf-9023-34b12cb48793
				summarizeBy: none
				sourceColumn: MOST_RECENT_EMPL_SERIES

				annotation SummarizationSetBy = Automatic

			column MOST_RECENT_EMPL_POSN_TITLE
				dataType: string
				lineageTag: b1b40a9e-8bad-4818-bef7-703447658e18
				summarizeBy: none
				sourceColumn: MOST_RECENT_EMPL_POSN_TITLE

				annotation SummarizationSetBy = Automatic

			column MOST_RECENT_EMPL_MANPOWER_PCN
				dataType: string
				lineageTag: bbf6e1cd-d526-4c13-a0e8-26b9f7b4cd30
				summarizeBy: none
				sourceColumn: MOST_RECENT_EMPL_MANPOWER_PCN

				annotation SummarizationSetBy = Automatic

			column EOD
				dataType: string
				lineageTag: df494362-0cd8-4c1e-818e-8bca9fdccd6b
				summarizeBy: none
				sourceColumn: EOD

				annotation SummarizationSetBy = Automatic

			column DT_ARR_SVCG_CCPO
				dataType: string
				lineageTag: 35e5790c-4b13-4333-8cf7-e0e3af0f264a
				summarizeBy: none
				sourceColumn: DT_ARR_SVCG_CCPO

				annotation SummarizationSetBy = Automatic


		// ============================================
		// Created Columns (PersonnelActions)
		// ============================================

		column 'From ORG Trim'
			dataType: string
			summarizeBy: none
			expression: =
				VAR Line6 =
				    'PersonnelActions'[FROM_POSITION_ORG_LINE6_14]
				VAR AfterDash =
				    IF (
				        NOT ISBLANK ( Line6 )
				            && CONTAINSSTRING ( Line6, "-" ),
				        MID ( Line6, SEARCH ( "-", Line6 ) + 1, 99 ),
				        Line6
				    )
				RETURN
				    LEFT ( TRIM ( AfterDash ), 2 )

			annotation SummarizationSetBy = Automatic

		column 'To ORG Trim'
			dataType: string
			summarizeBy: none
			expression: =
				VAR Line6 =
				    'PersonnelActions'[TO_POSITION_ORG_LINE6_22]
				VAR AfterDash =
				    IF (
				        NOT ISBLANK ( Line6 )
				            && CONTAINSSTRING ( Line6, "-" ),
				        MID ( Line6, SEARCH ( "-", Line6 ) + 1, 99 ),
				        Line6
				    )
				RETURN
				    LEFT ( TRIM ( AfterDash ), 2 )

			annotation SummarizationSetBy = Automatic

		column 'From ORG 2 Digit'
			dataType: string
			summarizeBy: none
			expression: = 'PersonnelActions'[From ORG Trim]

			annotation SummarizationSetBy = Automatic

		column 'To ORG 2 Digit'
			dataType: string
			summarizeBy: none
			expression: = 'PersonnelActions'[To ORG Trim]

			annotation SummarizationSetBy = Automatic

		column 'From ORG'
			dataType: string
			summarizeBy: none
			expression: =
				RELATED ( OrgMAPPING[Org] )

			annotation SummarizationSetBy = Automatic

		column 'From Center'
			dataType: string
			summarizeBy: none
			expression: =
				RELATED ( OrgMAPPING[Center] )

			annotation SummarizationSetBy = Automatic

		column 'To ORG'
			dataType: string
			summarizeBy: none
			expression: =
				VAR ToCode =
				    'PersonnelActions'[To ORG 2 Digit]
				RETURN
				    IF (
				        NOT ISBLANK ( ToCode ),
				        CALCULATE (
				            MAX ( OrgMAPPING[Org] ),
				            FILTER (
				                ALL ( OrgMAPPING ),
				                OrgMAPPING['Org Code'] = ToCode
				            )
				        )
				    )

			annotation SummarizationSetBy = Automatic

		column 'To Center'
			dataType: string
			summarizeBy: none
			expression: =
				VAR ToCode =
				    'PersonnelActions'[To ORG 2 Digit]
				RETURN
				    IF (
				        NOT ISBLANK ( ToCode ),
				        CALCULATE (
				            MAX ( OrgMAPPING[Center] ),
				            FILTER (
				                ALL ( OrgMAPPING ),
				                OrgMAPPING['Org Code'] = ToCode
				            )
				        )
				    )

			annotation SummarizationSetBy = Automatic

		column 'Compare From and To Organization'
			dataType: string
			summarizeBy: none
			expression: =
				VAR FromOrg =
				    'PersonnelActions'[From ORG]
				VAR ToOrg =
				    'PersonnelActions'[To ORG]
				RETURN
				    IF (
				        ISBLANK ( FromOrg )
				            || ISBLANK ( ToOrg ),
				        "FALSE",
				        IF ( FromOrg = ToOrg, "TRUE", "FALSE" )
				    )

			annotation SummarizationSetBy = Automatic

		column 'Compare From and To Series'
			dataType: string
			summarizeBy: none
			expression: =
				VAR FromSeries =
				    'PersonnelActions'[FROM_OCC_CODE_9]
				VAR ToSeries =
				    'PersonnelActions'[TO_OCC_CODE_17]
				RETURN
				    IF (
				        ISBLANK ( FromSeries )
				            || ISBLANK ( ToSeries ),
				        "FALSE",
				        IF ( FromSeries = ToSeries, "TRUE", "FALSE" )
				    )

			annotation SummarizationSetBy = Automatic

		column 'Student/Intern'
			dataType: string
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
				        "Student/Intern",
				        ""
				    )

			annotation SummarizationSetBy = Automatic

		column 'Internal/External Movement'
			dataType: string
			summarizeBy: none
			expression: =
				VAR NOA =
				    'PersonnelActions'[FIRST_NOA_CODE_5A]
				VAR NOAPrefix =
				    LEFT ( NOA, 1 )
				VAR IsExternal =
				    ( NOAPrefix = "3" )
				        || NOA = "T352"
				        || NOA = "CAO"
				VAR IsInternal =
				    NOA = "501"
				        || NOA = "570"
				        || NOA = "702"
				        || NOA = "721"
				RETURN
				    IF ( IsExternal, "External", IF ( IsInternal, "Internal", "" ) )

			annotation SummarizationSetBy = Automatic

		column 'Departure Reason'
			dataType: string
			summarizeBy: none
			expression: =
				VAR NOA =
				    'PersonnelActions'[FIRST_NOA_CODE_5A]
				VAR IsNonVol =
				    NOA = "330"
				        || NOA = "355"
				        || NOA = "357"
				        || NOA = "385"
				RETURN
				    IF (
				        IsNonVol,
				        "Non-Voluntary",
				        IF (
				            LEFT ( NOA, 1 ) = "3"
				                || NOA = "T352"
				                || NOA = "CAO",
				            "Voluntary",
				            ""
				        )
				    )

			annotation SummarizationSetBy = Automatic

		// ============================================
		// Measures (PersonnelActions) - Attrition & Movement
		// ============================================

		measure Losses =
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
				            'PersonnelActions'[Student/Intern] <> "Student/Intern",
				            'PersonnelActions'[Compare From and To Organization] = "FALSE",
				            'PersonnelActions'[FIRST_NOA_CODE_5A]
				                IN {
				                    "300",
				                    "301",
				                    "302",
				                    "303",
				                    "304",
				                    "305",
				                    "306",
				                    "307",
				                    "308",
				                    "309",
				                    "310",
				                    "312",
				                    "313",
				                    "315",
				                    "316",
				                    "317",
				                    "318",
				                    "319",
				                    "322",
				                    "323",
				                    "326",
				                    "327",
				                    "328",
				                    "330",
				                    "331",
				                    "335",
				                    "336",
				                    "338",
				                    "342",
				                    "345",
				                    "350",
				                    "352",
				                    "353",
				                    "355",
				                    "356",
				                    "357",
				                    "360",
				                    "361",
				                    "365",
				                    "370",
				                    "371",
				                    "372",
				                    "373",
				                    "380",
				                    "385",
				                    "390",
				                    "392",
				                    "T352",
				                    "CAO",
				                    "501",
				                    "570",
				                    "702",
				                    "721"
				                }
				        )
				    )
			formatString: #,0

		measure 'Agency Losses' =
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
				            'PersonnelActions'[Student/Intern] <> "Student/Intern",
				            'PersonnelActions'[Internal/External Movement] = "External"
				        )
				    )
			formatString: #,0

		measure 'Attrition Rate' =
				DIVIDE ( [Agency Losses], [Average On-hand count] )
			formatString: 0.0%

		measure 'Agency Attrition Rate' =
				DIVIDE ( [Agency Losses], [Average On-hand count] )
			formatString: 0.0%

		measure 'Non-voluntary Losses' =
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
				            'PersonnelActions'[Student/Intern] <> "Student/Intern",
				            'PersonnelActions'[Departure Reason] = "Non-Voluntary"
				        )
				    )
			formatString: #,0

		measure 'Non-voluntary Attrition' =
				DIVIDE ( [Non-voluntary Losses], [Average On-hand count] )
			formatString: 0.0%

		measure 'Voluntary Attrition' =
				[Attrition Rate] - [Non-voluntary Attrition]
			formatString: 0.0%

		measure 'Internal Moves' =
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
				            'PersonnelActions'[Student/Intern] <> "Student/Intern",
				            'PersonnelActions'[Internal/External Movement] = "Internal",
				            'PersonnelActions'[Compare From and To Organization] = "FALSE"
				        )
				    )
			formatString: #,0

		measure 'External Losses' =
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
				            'PersonnelActions'[Student/Intern] <> "Student/Intern",
				            'PersonnelActions'[Internal/External Movement] = "External",
				            'PersonnelActions'[Compare From and To Organization] = "FALSE"
				        )
				    )
			formatString: #,0

		measure 'Total Movement Events' =
				[Internal Moves] + [External Losses]
			formatString: #,0

		measure 'Internal Movement Share' =
				DIVIDE ( [Internal Moves], [Total Movement Events] )
			formatString: 0.0%

		measure 'External Movement Share' =
				DIVIDE ( [External Losses], [Total Movement Events] )
			formatString: 0.0%

		measure 'Org Risk Score' =
				VAR AttrRate =
				    [Attrition Rate]
				VAR VolShare =
				    DIVIDE ( [Non-voluntary Losses], [Agency Losses] )
				VAR NewHireShare =
				    DIVIDE ( [New Hire On-hand Start (<2y)], [OnHand Start] )
				VAR AttrScore =
				    SWITCH (
				        TRUE (),
				        AttrRate >= 0.15, 40,
				        AttrRate >= 0.12, 30,
				        AttrRate >= 0.08, 20,
				        10
				    )
				VAR VolScore =
				    SWITCH (
				        TRUE (),
				        VolShare >= 0.5, 30,
				        VolShare >= 0.3, 20,
				        10
				    )
				VAR NewHireScore =
				    SWITCH (
				        TRUE (),
				        NewHireShare >= 0.3, 30,
				        NewHireShare >= 0.2, 20,
				        10
				    )
				RETURN
				    AttrScore + VolScore + NewHireScore
			formatString: #,0
			partition PersonnelActions = m
				mode: import
				source =
						let
						    Source = SharePoint.Files("https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/", [ApiVersion = 15]),
						    #"Filtered Rows" = Table.SelectRows(Source, each ([Folder Path] = "https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/Shared Documents/Attrition-Retention Automation/TEST/NewTest/")),
						    #"Filtered Rows1" = Table.SelectRows(#"Filtered Rows", each Text.Contains([Name], "Actions")),
						    #"Removed Columns" = Table.RemoveColumns(#"Filtered Rows1",{"Extension", "Date accessed", "Date modified", "Date created", "Attributes", "Folder Path"}),
						    #"Filtered Hidden Files1" = Table.SelectRows(#"Removed Columns", each [Attributes]?[Hidden]? <> true),
						    #"Invoke Custom Function1" = Table.AddColumn(#"Filtered Hidden Files1", "Transform File (2)", each #"Transform File (2)"([Content])),
						    #"Removed Other Columns1" = Table.SelectColumns(#"Invoke Custom Function1", {"Transform File (2)"}),
						    #"Expanded Table Column1" = Table.ExpandTableColumn(#"Removed Other Columns1", "Transform File (2)", Table.ColumnNames(#"Transform File (2)"(#"Sample File (2)")))
						in
						    #"Expanded Table Column1"

			annotation PBI_NavigationStepName = Navigation

			annotation PBI_ResultType = Table

		table OrgMAPPING
			lineageTag: f59df8ee-a0ed-45fb-b7ad-c63cde724b55

			column Org
				dataType: string
				lineageTag: bc573a2e-dc03-4483-afa8-50bdb898f5b7
				summarizeBy: none
				sourceColumn: Org

				annotation SummarizationSetBy = Automatic

			column Center
				dataType: string
				lineageTag: 5a2e6469-e367-4bac-b2f7-f69f8f913616
				summarizeBy: none
				sourceColumn: Center

				annotation SummarizationSetBy = Automatic

			column 'Org Code'
				dataType: string
				lineageTag: 12c6063a-6f8f-4a73-8657-ff81dc74d1b9
				summarizeBy: none
				sourceColumn: Org Code

				annotation SummarizationSetBy = Automatic

			column 'CAM ID'
				dataType: string
				lineageTag: f2f9acda-c816-46fd-9cee-f7838def7050
				summarizeBy: none
				sourceColumn: CAM ID

				annotation SummarizationSetBy = Automatic

			partition OrgMAPPING = m
				mode: import
				source =
						let
						    Source = SharePoint.Files("https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/", [ApiVersion = 15]),
						    #"Filtered Rows" = Table.SelectRows(Source, each ([Folder Path] = "https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/Shared Documents/Attrition-Retention Automation/TEST/NewTest/")),
						    #"Filtered Rows1" = Table.SelectRows(#"Filtered Rows", each Text.StartsWith([Name], "Org")),
						    #"Removed Other Columns" = Table.SelectColumns(#"Filtered Rows1",{"Content", "Name"}),
						    #"Filtered Hidden Files1" = Table.SelectRows(#"Removed Other Columns", each [Attributes]?[Hidden]? <> true),
						    #"Invoke Custom Function1" = Table.AddColumn(#"Filtered Hidden Files1", "Transform File (3)", each #"Transform File (3)"([Content])),
						    #"Removed Other Columns1" = Table.SelectColumns(#"Invoke Custom Function1", {"Transform File (3)"}),
						    #"Expanded Table Column1" = Table.ExpandTableColumn(#"Removed Other Columns1", "Transform File (3)", Table.ColumnNames(#"Transform File (3)"(#"Sample File (3)"))),
						    #"Changed Type" = Table.TransformColumnTypes(#"Expanded Table Column1",{{"Org", type text}, {"Center", type text}, {"Org Code", type text}, {"CAM ID", type text}})
						in
						    #"Changed Type"

			annotation PBI_NavigationStepName = Navigation

			annotation PBI_ResultType = Table

		cultureInfo en-US

			linguisticMetadata =
					{
					  "Version": "1.0.0",
					  "Language": "en-US"
					}
				contentType: json

		expression Parameter1 = #"Sample File" meta [IsParameterQuery=true, BinaryIdentifier=#"Sample File", Type="Binary", IsParameterQueryRequired=true]
			lineageTag: 5335aced-1859-4e1b-9251-f1246baa02b7
			queryGroup: 'Transform File from Employees\Helper Queries'

			annotation PBI_ResultType = Binary

		expression 'Transform Sample File' =
				let
				    Source = Excel.Workbook(Parameter1, null, true),
				    Table1_Table = Source{[Item="Table1",Kind="Table"]}[Data]
				in
				    Table1_Table
			lineageTag: 3a15d5bc-c31a-4678-ad54-87f9a720708a
			queryGroup: 'Transform File from Employees'

		expression 'Sample File' =
				let
				    Source = SharePoint.Files("https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/", [ApiVersion = 15]),
				    #"Filtered Rows" = Table.SelectRows(Source, each ([Folder Path] = "https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/Shared Documents/Attrition-Retention Automation/TEST/NewTest/")),
				    #"Filtered Rows1" = Table.SelectRows(#"Filtered Rows", each Text.StartsWith([Name], "DISA")),
				    #"Removed Columns" = Table.RemoveColumns(#"Filtered Rows1",{"Extension", "Date accessed", "Date modified", "Date created", "Attributes", "Folder Path"}),
				    Navigation1 = #"Removed Columns"{0}[Content]
				in
				    Navigation1
			lineageTag: 6338a8e8-1526-4c93-bfae-c2c160e8f0d2
			queryGroup: 'Transform File from Employees\Helper Queries'

			annotation PBI_NavigationStepName = Navigation

			annotation PBI_ResultType = Binary

		expression 'Transform File' =
				let
				    Source = (Parameter1) => let
				        Source = Excel.Workbook(Parameter1, null, true),
				        Table1_Table = Source{[Item="Table1",Kind="Table"]}[Data]
				    in
				        Table1_Table
				in
				    Source
			mAttributes: [ FunctionQueryBinding = "{""exemplarFormulaName"":""Transform Sample File""}" ]
			lineageTag: 7f2d2f07-f2db-41e3-836b-1ea26f31c538
			queryGroup: 'Transform File from Employees\Helper Queries'

			annotation PBI_ResultType = Function

		expression Parameter2 = #"Sample File (2)" meta [IsParameterQuery=true, BinaryIdentifier=#"Sample File (2)", Type="Binary", IsParameterQueryRequired=true]
			lineageTag: c970b17a-5bf0-45df-8793-c1be9f78b67f
			queryGroup: 'Transform File from PersonnelActions\Helper Queries'

			annotation PBI_ResultType = Binary

		expression 'Transform Sample File (2)' =
				let
				    Source = Excel.Workbook(Parameter2, null, true),
				    Table1_Table = Source{[Item="Table1",Kind="Table"]}[Data]
				in
				    Table1_Table
			lineageTag: 8b58789c-6fbd-47d8-b27c-1fd5c1e91489
			queryGroup: 'Transform File from PersonnelActions'

			annotation PBI_ResultType = Table

		expression 'Sample File (2)' =
				let
				    Source = SharePoint.Files("https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/", [ApiVersion = 15]),
				    #"Filtered Rows" = Table.SelectRows(Source, each ([Folder Path] = "https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/Shared Documents/Attrition-Retention Automation/TEST/NewTest/")),
				    #"Filtered Rows1" = Table.SelectRows(#"Filtered Rows", each Text.Contains([Name], "Actions")),
				    #"Removed Columns" = Table.RemoveColumns(#"Filtered Rows1",{"Extension", "Date accessed", "Date modified", "Date created", "Attributes", "Folder Path"}),
				    Navigation1 = #"Removed Columns"{0}[Content]
				in
				    Navigation1
			lineageTag: 7378f930-f419-4fbd-a305-a1d732f67974
			queryGroup: 'Transform File from PersonnelActions\Helper Queries'

			annotation PBI_NavigationStepName = Navigation

			annotation PBI_ResultType = Binary

		expression 'Transform File (2)' =
				let
				    Source = (Parameter2) => let
				        Source = Excel.Workbook(Parameter2, null, true),
				        Table1_Table = Source{[Item="Table1",Kind="Table"]}[Data]
				    in
				        Table1_Table
				in
				    Source
			mAttributes: [ FunctionQueryBinding = "{""exemplarFormulaName"":""Transform Sample File (2)""}" ]
			lineageTag: 37e5e54d-61fa-4a5a-b55a-d7379d91d6dc
			queryGroup: 'Transform File from PersonnelActions\Helper Queries'

			annotation PBI_ResultType = Function

		expression Parameter3 = #"Sample File (3)" meta [IsParameterQuery=true, BinaryIdentifier=#"Sample File (3)", Type="Binary", IsParameterQueryRequired=true]
			lineageTag: c52b68ec-240d-43fa-81a4-9d06b1f9e9e2
			queryGroup: 'Transform File from OrgMAPPING\Helper Queries'

			annotation PBI_ResultType = Binary

		expression 'Transform Sample File (3)' =
				let
				    Source = Excel.Workbook(Parameter3, null, true),
				    Table1_Table = Source{[Item="Table1",Kind="Table"]}[Data]
				in
				    Table1_Table
			lineageTag: dbd72a65-9956-41f3-8e69-2f0e30e1410d
			queryGroup: 'Transform File from OrgMAPPING'

			annotation PBI_ResultType = Table

		expression 'Sample File (3)' =
				let
				    Source = SharePoint.Files("https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/", [ApiVersion = 15]),
				    #"Filtered Rows" = Table.SelectRows(Source, each ([Folder Path] = "https://dod365.sharepoint-mil.us/teams/DISA-AttritionRetentionAutomation/Shared Documents/Attrition-Retention Automation/TEST/NewTest/")),
				    #"Filtered Rows1" = Table.SelectRows(#"Filtered Rows", each Text.StartsWith([Name], "Org")),
				    #"Removed Other Columns" = Table.SelectColumns(#"Filtered Rows1",{"Content", "Name"}),
				    Navigation1 = #"Removed Other Columns"{0}[Content]
				in
				    Navigation1
			lineageTag: 197fabed-309e-4c0b-8c54-b8f44b71d8cd
			queryGroup: 'Transform File from OrgMAPPING\Helper Queries'

			annotation PBI_NavigationStepName = Navigation

			annotation PBI_ResultType = Binary

		expression 'Transform File (3)' =
				let
				    Source = (Parameter3) => let
				        Source = Excel.Workbook(Parameter3, null, true),
				        Table1_Table = Source{[Item="Table1",Kind="Table"]}[Data]
				    in
				        Table1_Table
				in
				    Source
			mAttributes: [ FunctionQueryBinding = "{""exemplarFormulaName"":""Transform Sample File (3)""}" ]
			lineageTag: 178a3f38-36eb-41fc-939d-baec833a5211
			queryGroup: 'Transform File from OrgMAPPING\Helper Queries'

			annotation PBI_ResultType = Function

		queryGroup 'Transform File from Employees'

			annotation PBI_QueryGroupOrder = 0

		queryGroup 'Transform File from Employees\Helper Queries'

			annotation PBI_QueryGroupOrder = 0

		queryGroup 'Transform File from PersonnelActions'

			annotation PBI_QueryGroupOrder = 2

		queryGroup 'Transform File from PersonnelActions\Helper Queries'

			annotation PBI_QueryGroupOrder = 0

		queryGroup 'Transform File from OrgMAPPING'

			annotation PBI_QueryGroupOrder = 4

		queryGroup 'Transform File from OrgMAPPING\Helper Queries'

			annotation PBI_QueryGroupOrder = 0

		annotation __PBI_TimeIntelligenceEnabled = 1

		annotation PBI_QueryOrder = ["Employees","PersonnelActions","OrgMAPPING","Parameter1","Transform Sample File","Sample File","Transform File","Parameter2","Transform Sample File (2)","Sample File (2)","Transform File (2)","Parameter3","Transform Sample File (3)","Sample File (3)","Transform File (3)"]

		annotation PBI_ProTooling = ["TMDLView_Desktop","DevMode"]
