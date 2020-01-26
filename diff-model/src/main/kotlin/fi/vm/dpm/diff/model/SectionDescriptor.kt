package fi.vm.dpm.diff.model

data class SectionDescriptor(
    val sectionShortTitle: String,
    val sectionTitle: String,
    val sectionDescription: String,
    val sectionFields: List<FieldDescriptor>
)
