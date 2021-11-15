package raui.imashev.geojsonexample.pojo


data class Feature(
    val geometry: Geometry,
    val properties: Properties,
    val type: String
)