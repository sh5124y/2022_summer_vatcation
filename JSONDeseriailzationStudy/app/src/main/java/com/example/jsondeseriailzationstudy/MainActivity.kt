package com.example.jsondeseriailzationstudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@JsonDeserialize(using = ComplexJSONDataDeserializer::class)
data class ComplexJSONData(
    val innerData: String,
    val data1: Int,
    val data2: String,
    val list: List<Int>
)
class ComplexJSONDataDeserializer : StdDeserializer<ComplexJSONData>(
    ComplexJSONData::class.java
) {
    override fun deserialize(p: JsonParser?,
                             ctxt: DeserializationContext?): ComplexJSONData {
        val node = p?.codec?.readTree<JsonNode>(p)

        val nestedNode = node?.get("nested")
        val innerDataValue = nestedNode?.get("inner_data")?.asText()

        val innerNestedNode = nestedNode?.get("inner_nested")
        val innerNestedData1 = innerNestedNode?.get("data1")?.asInt()
        val innerNestedData2 = innerNestedNode?.get("data2")?.asText()

        val list = mutableListOf<Int>()
        innerNestedNode?.get("list")?.elements()?.forEach{
            list.add(it.asInt())
        }

        return ComplexJSONData(
            innerDataValue!!,
            innerNestedData1!!,
            innerNestedData2!!,
            list
        )
    }
}

data class MyJSONDataClass(
    val data1: Int,
    val data2: String,
    val list: List<Int>)

data class MyJSONNestedDataClass(val nested: MyJSONDataClass)

data class Addreses(
    val city: String,
    val lat: Double,
    val lon: Double)
data class Person(
    val name: String,
    val age :Int,
    val favorites: List<String>,
    val address: Addreses)



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var mapper = jacksonObjectMapper()
        val jsonString = """
            {
                "data1":1234,
                "data2":"Hello",
                "list":[1,2,3]
            }
        """.trimIndent()

        val d1 = mapper?.readValue<MyJSONDataClass>(jsonString)
        Log.d("mytag", d1.toString())

        val jsonString2 = """
            {
                "nested":{
                    "data1":1234,
                    "data2":"Hello",
                    "list":[1,2,3]
                    
                }
            }
        """

        val d2 = mapper?.readValue<MyJSONNestedDataClass>(jsonString2)
        Log.d("mytag", d2.toString())

        val personString = """
            {
                "name" : "John",
                "age" : 20,
                "favorites" : ["study","game"],
                "address":{
                    "city": 0.0,
                    "lat": 0.0,
                    "lon": 1.0
                }
            }
        """

        val person = mapper?.readValue<Person>(personString)
        Log.d("mytag", person.toString())

        val complexJsonString = """
            {
                "nested": {
                    "inner_data" : "Hello from inner",
                    "inner_nested":{
                        "data1" :1234,
                        "data2" : "Hello",
                         "list":[1,2,3]
                    }
                 }
            }
        """
        val complex = mapper?.readValue<ComplexJSONData>(complexJsonString)
        Log.d("mytag", complex.toString())
    }
}