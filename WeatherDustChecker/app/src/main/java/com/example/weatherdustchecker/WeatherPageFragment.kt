package com.example.weatherdustchecker

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.service.controls.templates.TemperatureControlTemplate
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL

@JsonDeserialize(using = MyDeserializer::class)
data class OpenWeatherAPIJSONResponse(val temp: Double, val id:Int)

class MyDeserializer : StdDeserializer<OpenWeatherAPIJSONResponse>(
    OpenWeatherAPIJSONResponse::class.java
) {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): OpenWeatherAPIJSONResponse {
        val node = p?.codec?.readTree<JsonNode>(p)
        val weather = node?.get("weather")
        val firstWeather = weather?.elements()?.next()
        val id = firstWeather?.get("id")?.asInt()
        val main = node?.get("main")
        val temp = main?.get("temp")?.asDouble()

        return OpenWeatherAPIJSONResponse(temp!!, id!!)
    }
}

class WeatherPageFragment : Fragment() {
    lateinit var statusText :TextView
    lateinit var temperatureText :TextView
    lateinit var weatherImage :ImageView
    var APP_ID = "0f3ff41f65fd5d5c4cd6098b20374099"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater
            .inflate(R.layout.weather_page_fragment,
            container, false
            )
        //arguments 값 참조해서 가져오고 해당하는 뷰에 출력해주기
        statusText = view.findViewById<TextView>(R.id.weather_status_text)
        temperatureText = view.findViewById<TextView>(R.id.weather_temp_text)
        weatherImage = view.findViewById<ImageView>(R.id.weather_icon)


//        statusText.text = arguments?.getString("status")
//        temperatureText.text = arguments?.getDouble("temperature").toString()
//
//        //imageview 가져와서 sun 이미지 출력하기
//        weathericon.setImageResource(arguments?.getInt("res_id")!!)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lat = arguments?.getDouble("lat")
        val lon = arguments?.getDouble("lon")
        var url = "http://api.openweathermap.org/data/2.5/weather?units=metric&appid=${APP_ID}&lat=${lat}&lon=${lon}"

        APICall(object :APICall.APICallback{
            override fun onComplete(result: String) {
                Log.d("mytag",result)
                var mapper = jacksonObjectMapper()
                var data = mapper?.readValue<OpenWeatherAPIJSONResponse>(result)

                temperatureText.text = data.temp.toString()

                var id = data.id.toString()
                if(id != null) {
                    statusText.text = when {
                        id.startsWith("2") -> {
                            weatherImage.setImageResource(R.drawable.flash)
                            "천둥, 번개"
                        }
                        id.startsWith("3") -> {
                            weatherImage.setImageResource(R.drawable.rain)
                            "이슬비"
                        }
                        id.startsWith("5") -> {
                            weatherImage.setImageResource(R.drawable.rain)
                            "비"
                        }
                        id.startsWith("6") -> {
                            weatherImage.setImageResource(R.drawable.snow)
                            "눈"
                        }
                        id.startsWith("7") -> {
                            weatherImage.setImageResource(R.drawable.cloudy)
                            "흐림"
                        }
                        id.equals("800") -> {
                            weatherImage.setImageResource(R.drawable.sun)
                            "화창"
                        }
                        id.startsWith("8") -> {
                            weatherImage.setImageResource(R.drawable.cloud)
                            "구름 낌"
                        }
                        else -> "알 수 없음"
                    }
                }
            }
        }).execute(URL(url))
    }

    //newInstance 만들어서 fragment에서 뭐 할지 만들어주기
    companion object{
        fun newInstance(lat:Double, lon:Double):WeatherPageFragment{
            val fragment = WeatherPageFragment()

            val args = Bundle()
            args.putDouble("lat", lat)
            args.putDouble("lon", lon)
            fragment.arguments = args

            return fragment
        }
    }

}