package fi.vm.dpm.diff.cli.integration

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
        (450, 'Dimension', 1, NULL, NULL, NULL, NULL);


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
        (450, 4, 'TDS Dimension (label pl)', 'label');


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


    COMMIT;
    """.trimLineStartsAndConsequentBlankLines()
