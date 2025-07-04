data class SaveDeviceDetailsRequest(
    val customer_unique_id: String,
    val serial_number: String,
    val firmware_version: String
)

data class MachineTestStatusRequest(
    val customer_unique_id: String,
    val bloodPressureModule: String,
    val cholesterolUricAcidModule: String,
    val glucometerModule: String,
    val hemoglobinModule: String,
    val pulseOximetryModule: String,
    val rdtModule: String,
    val ecgModule: String
)

data class GenericResponse(
    val status: Boolean,
    val message: String
)
