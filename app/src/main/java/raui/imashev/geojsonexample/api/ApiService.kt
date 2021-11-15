package raui.imashev.geojsonexample.api

import raui.imashev.geojsonexample.pojo.Geo
import retrofit2.http.GET
import io.reactivex.Single

interface ApiService {
    @GET("russia.geo.json")
    fun getData(): Single<Geo>
}