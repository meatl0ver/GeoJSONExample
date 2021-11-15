package raui.imashev.geojsonexample.pojo

import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "geometry")
data class Geometry(
    @SerializedName("TYPE")
    @Expose
    val coordinates: List<List<List<List<Double>>>>,
    val type: String
)