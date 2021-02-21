package fi.vm.dpm.diff.cli.integration.comparedpm

import ext.kotlin.trimLineStartsAndConsequentBlankLines

fun compareDpmSetupSql() =
    """
    BEGIN;


    INSERT INTO 'mLanguage' (
        LanguageID,
        IsoCode
    )
    VALUES
      (1, 'fi'),
      (2, 'sv'),
      (3, 'en'),
      (4, 'pl');


    INSERT INTO 'mOwner' (
        OwnerID,
        OwnerName,
        OwnerNamespace,
        OwnerLocation,
        OwnerPrefix,
        OwnerCopyright,
        ParentOwnerID,
        ConceptID
    )
    VALUES
        (1, 'VK-FRTT/dpm-diff', 'https://github.com/VK-FRTT/dpm-diff', 'https://github.com/VK-FRTT/dpm-diff', 'dd', '2021', NULL, NULL);


    INSERT INTO 'mConcept' (
        ConceptID,
        ConceptType,
        OwnerID,
        CreationDate,
        ModificationDate,
        FromDate,
        ToDate
        )
    VALUES
        (1, 'Domain', 1, NULL, NULL, NULL, NULL),
        (2, 'Domain', 1, NULL, NULL, NULL, NULL),
        (50, 'Domain', 1, NULL, NULL, NULL, NULL),
        (100, 'Domain', 1, NULL, NULL, NULL, NULL),
        (150, 'Member', 1, NULL, NULL, NULL, NULL),
        (151, 'Member', 1, NULL, NULL, NULL, NULL),
        (200, 'Member', 1, NULL, NULL, NULL, NULL),
        (300, 'Hierarchy', 1, NULL, NULL, NULL, NULL),
        (301, 'Hierarchy', 1, NULL, NULL, NULL, NULL),
        (350, 'HierarchyNode', 1, NULL, NULL, NULL, NULL),
        (351, 'HierarchyNode', 1, NULL, NULL, NULL, NULL),
        (400, 'Dimension', 1, NULL, NULL, NULL, NULL),
        (450, 'Dimension', 1, NULL, NULL, NULL, NULL),
        (500, 'Framework', 1, NULL, NULL, NULL, NULL),
        (501, 'Framework', 1, NULL, NULL, NULL, NULL),
        (600, 'Taxonomy', 1, NULL, NULL, NULL, NULL),
        (601, 'Taxonomy', 1, NULL, NULL, NULL, NULL),
        (700, 'Module', 1, NULL, NULL, NULL, NULL),
        (800, 'Table', 1, NULL, NULL, NULL, NULL),
        (900, 'TemplateOrTable', 1, NULL, NULL, NULL, NULL),
        (1000, 'Axis', 1, NULL, NULL, NULL, NULL),
        (1100, 'AxisOrdinate', 1, NULL, NULL, NULL, NULL),
        (1101, 'AxisOrdinate', 1, NULL, NULL, NULL, NULL);


    INSERT INTO 'mConceptTranslation' (
        ConceptID,
        LanguageID,
        Text,
        Role
        )
    VALUES
        (1, 1, 'Explicit domain A (label fi)', 'label'),
        (1, 2, 'Explicit domain A (label sv)', 'label'),
        (1, 3, 'Explicit domain A (label en)', 'label'),
        (1, 4, 'Explicit domain A (label pl)', 'label'),

        (2, 1, 'Explicit domain B (label fi)', 'label'),
        (2, 2, 'Explicit domain B (label sv)', 'label'),
        (2, 3, 'Explicit domain B (label en)', 'label'),
        (2, 4, 'Explicit domain B (label pl)', 'label'),

        (50, 1, 'Typed domain S (label fi)', 'label'),
        (50, 2, 'Typed domain S (label sv)', 'label'),
        (50, 3, 'Typed domain S (label en)', 'label'),
        (50, 4, 'Typed domain S (label pl)', 'label'),

        (150, 1, 'EDA Member 1 (label fi)', 'label'),
        (150, 2, 'EDA Member 1 (label sv)', 'label'),
        (150, 3, 'EDA Member 1 (label en)', 'label'),
        (150, 4, 'EDA Member 1 (label pl)', 'label'),

        (151, 1, 'EDA Member 2 (label fi)', 'label'),
        (151, 2, 'EDA Member 2 (label sv)', 'label'),
        (151, 3, 'EDA Member 2 (label en)', 'label'),
        (151, 4, 'EDA Member 2 (label pl)', 'label'),

        (200, 1, 'MET Member 1 (label fi)', 'label'),
        (200, 2, 'MET Member 1 (label sv)', 'label'),
        (200, 3, 'MET Member 1 (label en)', 'label'),
        (200, 4, 'MET Member 1 (label pl)', 'label'),

        (300, 1, 'EDA Hierarchy 1 (label fi)', 'label'),
        (300, 2, 'EDA Hierarchy 1 (label sv)', 'label'),
        (300, 3, 'EDA Hierarchy 1 (label en)', 'label'),
        (300, 4, 'EDA Hierarchy 1 (label pl)', 'label'),

        (301, 1, 'EDA Hierarchy 2 (label fi)', 'label'),
        (301, 2, 'EDA Hierarchy 2 (label sv)', 'label'),
        (301, 3, 'EDA Hierarchy 2 (label en)', 'label'),
        (301, 4, 'EDA Hierarchy 2 (label pl)', 'label'),

        (350, 1, 'EDA HierarchyNode 1 (label fi)', 'label'),
        (350, 2, 'EDA HierarchyNode 1 (label sv)', 'label'),
        (350, 3, 'EDA HierarchyNode 1 (label en)', 'label'),
        (350, 4, 'EDA HierarchyNode 1 (label pl)', 'label'),

        (351, 1, 'EDA HierarchyNode 2 (label fi)', 'label'),
        (351, 2, 'EDA HierarchyNode 2 (label sv)', 'label'),
        (351, 3, 'EDA HierarchyNode 2 (label en)', 'label'),
        (351, 4, 'EDA HierarchyNode 2 (label pl)', 'label'),

        (400, 1, 'EDA Dimension (label fi)', 'label'),
        (400, 2, 'EDA Dimension (label sv)', 'label'),
        (400, 3, 'EDA Dimension (label en)', 'label'),
        (400, 4, 'EDA Dimension (label pl)', 'label'),

        (450, 1, 'TDS Dimension (label fi)', 'label'),
        (450, 2, 'TDS Dimension (label sv)', 'label'),
        (450, 3, 'TDS Dimension (label en)', 'label'),
        (450, 4, 'TDS Dimension (label pl)', 'label'),

        (500, 1, 'RFA framework (label fi)', 'label'),
        (500, 2, 'RFA framework (label sv)', 'label'),
        (500, 3, 'RFA framework (label en)', 'label'),
        (500, 4, 'RFA framework (label pl)', 'label'),

        (501, 1, 'RFB framework (label fi)', 'label'),
        (501, 2, 'RFB framework (label sv)', 'label'),
        (501, 3, 'RFB framework (label en)', 'label'),
        (501, 4, 'RFB framework (label pl)', 'label'),

        (600, 1, 'TXA taxonomy (label fi)', 'label'),
        (600, 2, 'TXA taxonomy (label sv)', 'label'),
        (600, 3, 'TXA taxonomy (label en)', 'label'),
        (600, 4, 'TXA taxonomy (label pl)', 'label'),

        (601, 1, 'TXB taxonomy (label fi)', 'label'),
        (601, 2, 'TXB taxonomy (label sv)', 'label'),
        (601, 3, 'TXB taxonomy (label en)', 'label'),
        (601, 4, 'TXB taxonomy (label pl)', 'label'),

        (700, 1, 'MDA module (label fi)', 'label'),
        (700, 2, 'MDA module (label sv)', 'label'),
        (700, 3, 'MDA module (label en)', 'label'),
        (700, 4, 'MDA module (label pl)', 'label'),

        (800, 1, 'TBA table (label fi)', 'label'),
        (800, 2, 'TBA table (label sv)', 'label'),
        (800, 3, 'TBA table (label en)', 'label'),
        (800, 4, 'TBA table (label pl)', 'label'),

        (900, 1, 'TTA template or table (label fi)', 'label'),
        (900, 2, 'TTA template or table (label sv)', 'label'),
        (900, 3, 'TTA template or table (label en)', 'label'),
        (900, 4, 'TTA template or table (label pl)', 'label'),

        (1000, 1, 'AXA axis (label fi)', 'label'),
        (1000, 2, 'AXA axis (label sv)', 'label'),
        (1000, 3, 'AXA axis (label en)', 'label'),
        (1000, 4, 'AXA axis (label pl)', 'label'),

        (1100, 1, 'AOA axis ordinate (label fi)', 'label'),
        (1100, 2, 'AOA axis ordinate (label sv)', 'label'),
        (1100, 3, 'AOA axis ordinate (label en)', 'label'),
        (1100, 4, 'AOA axis ordinate (label pl)', 'label'),

        (1101, 1, 'AOB axis ordinate (label fi)', 'label'),
        (1101, 2, 'AOB axis ordinate (label sv)', 'label'),
        (1101, 3, 'AOB axis ordinate (label en)', 'label'),
        (1101, 4, 'AOB axis ordinate (label pl)', 'label');


    INSERT INTO 'mDomain' (
        DomainID,
        DomainCode,
        DomainLabel,
        DomainDescription,
        DomainXBRLCode,
        DataType,
        IsTypedDomain,
        ConceptID
        )
    VALUES
        (1, 'EDA', 'Explicit domain A', NULL, NULL, NULL, 0, 1),
        (2, 'EDB', 'Explicit domain B', NULL, NULL, NULL, 0, 2),
        (50, 'TDS', 'Typed domain S', NULL, NULL, 'String', 1, 50),
        (100, 'MET', NULL, NULL, 'MET', NULL, 0, 100),
        (9999, '9999', 'Open', NULL, NULL, NULL, 0, NULL);


    INSERT INTO 'mMember' (
        MemberID,
        DomainID,
        MemberCode,
        MemberLabel,
        MemberXBRLCode,
        IsDefaultMember,
        ConceptID
        )
    VALUES
        (150, 1, 'EDA-M1', 'EDA Member 1', NULL, 0, 150),
        (151, 1, 'EDA-M2', 'EDA Member 2', NULL, 0, 151),
        (200, 1, 'MET-M1', 'Metric member', NULL, NULL, 200),
        (9999, 9999, NULL, 'Open', NULL, NULL, NULL);


    INSERT INTO 'mMetric' (
        MetricID,
        CorrespondingMemberID,
        DataType,
        FlowType,
        BalanceType,
        ReferencedDomainID,
        ReferencedHierarchyID,
        HierarchyStartingMemberID,
        IsStartingMemberIncluded
        )
    VALUES
        (250, 200, 'Boolean', 'Flow', 'Debit', 1, 300, 150, 0);


    INSERT INTO 'mHierarchy' (
        HierarchyID,
        HierarchyCode,
        HierarchyLabel,
        DomainID,
        HierarchyDescription,
        ConceptID
        )
    VALUES
        (300, 'EDA-H1', 'EDA Hierarchy 1', 1, NULL, 300),
        (301, 'EDA-H2', 'EDA Hierarchy 2', 1, NULL, 301);


    INSERT INTO 'mHierarchyNode' (
        HierarchyID,
        MemberID,
        IsAbstract,
        ComparisonOperator,
        UnaryOperator,
        'Order',
        Level,
        ParentMemberID,
        HierarchyNodeLabel,
        ConceptID,
        Path
        )
    VALUES
        (300, 150, 0, '>', '+', 1, 1, NULL, 'Node 1', 350, NULL),
        (300, 151, 0, '>', '+', 2, 2, 151, 'Node 2', 351, NULL);


    INSERT INTO 'mDimension' (
        DimensionID,
        DimensionCode,
        DimensionLabel,
        DimensionDescription,
        DimensionXBRLCode,
        DomainID,
        IsTypedDimension,
        ConceptID
        )
    VALUES
        (400, 'EDA-DIM', 'EDA dimension', NULL, NULL, 1, 0, 400),
        (450, 'TDS-DIM', 'TDS dimension', NULL, NULL, 50, 1, 450);


    INSERT INTO 'mReportingFramework' (
        FrameworkID,
        FrameworkCode,
        FrameworkLabel,
        ConceptID
        )
    VALUES
        (500, 'RFA', 'RFA framework', 500),
        (501, 'RFB', 'RFB framework', 501);


    INSERT INTO 'mTaxonomy' (
        TaxonomyID,
        FrameworkID,
        TaxonomyCode,
        TaxonomyLabel,
        ConceptID
        )
    VALUES
        (600, 500, 'TXA', 'TXA taxonomy', 600),
        (601, 500, 'TXB', 'TXB taxonomy', 601);


    INSERT INTO 'mModule' (
        ModuleID,
        TaxonomyID,
        ModuleCode,
        ModuleLabel,
        ConceptID
        )
    VALUES
        (700, 600, 'MDA', 'MDA module', 700);


    INSERT INTO 'mTable' (
        TableID,
        TableCode,
        TableLabel,
        XbrlFilingIndicatorCode,
        ConceptID
        )
    VALUES
        (800, 'TBA', 'TBA table', 'filing-indicator', 800);


    INSERT INTO 'mTemplateOrTable' (
        TemplateOrTableID,
        TaxonomyID,
        TemplateOrTableCode,
        TemplateOrTableLabel,
        TemplateOrTableType,
        ConceptID
        )
    VALUES
        (900, 600, 'TTA', 'TTA template or table', 'BusinessTable', 900);


    INSERT INTO 'mTaxonomyTable' (
        TaxonomyID,
        TableID,
        AnnotatedTableID,
        IsSimplyReuse
        )
    VALUES
        (600, 800, 900, 0);


    INSERT INTO 'mAxis' (
        AxisID,
        AxisOrientation,
        AxisLabel,
        IsOpenAxis,
        ConceptID
        )
    VALUES
        (1000, 'X', 'AXA axis', 0, 1000);


    INSERT INTO 'mTableAxis' (
        AxisID,
        TableID,
        'Order'
        )
    VALUES
        (1000, 800, 1);


    INSERT INTO 'mAxisOrdinate' (
        OrdinateID,
        AxisID,
        OrdinateLabel,
        OrdinateCode,
        IsDisplayBeforeChildren,
        IsAbstractHeader,
        IsRowKey,
        Level,
        'Order',
        TypeOfKey,
        ParentOrdinateID,
        ConceptID
        )
    VALUES
        (1100, 1000, 'AOA axis ordinate', 'AOA', 0, 0, 0, 5, 15, 'type-of-key-A', NULL, 1100),
        (1101, 1000, 'AOB axis ordinate', 'AOB', 0, 0, 0, 10, 20, 'type-of-key-B', 1100, 1101);


    INSERT INTO 'mOrdinateCategorisation' (
        OrdinateID,
        DimensionID,
        MemberID,
        DimensionMemberSignature,
        Source,
        DPS
        )
    VALUES
        (1100, 400, 150, 'signature-A', 'source-A', 'dps-A');

    COMMIT;
    """.trimLineStartsAndConsequentBlankLines()
