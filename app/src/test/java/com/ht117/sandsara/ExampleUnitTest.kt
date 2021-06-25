package com.ht117.sandsara

import com.ht117.sandsara.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    val text = "{\n" +
            "    \"records\": [\n" +
            "        {\n" +
            "            \"id\": \"recRQ2W1Man4H8QRI\",\n" +
            "            \"fields\": {\n" +
            "                \"version\": \"1.0.0\",\n" +
            "                \"file\": [\n" +
            "                    {\n" +
            "                        \"id\": \"attFXUNJAtSWbmYuB\",\n" +
            "                        \"url\": \"https://dl.airtable.com/.attachments/8158373ea1ee49577994ca18a9a93f68/4b5cf817/firmware-1.0.2-4.bin\",\n" +
            "                        \"filename\": \"firmware-1.0.2-4.bin\",\n" +
            "                        \"size\": 1110544,\n" +
            "                        \"type\": \"application/octet-stream\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            \"createdTime\": \"2020-12-07T21:53:49.000Z\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"recUMPBChr3Uqb7eG\",\n" +
            "            \"fields\": {},\n" +
            "            \"createdTime\": \"2020-12-07T21:53:49.000Z\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"reczcEcmCfFQR1c16\",\n" +
            "            \"fields\": {},\n" +
            "            \"createdTime\": \"2020-12-07T21:53:49.000Z\"\n" +
            "        }\n" +
            "    ]\n" +
            "}"

    @Test
    fun split() {
        val json = Json {
            isLenient = true
        }
        val response = json.decodeFromString<BaseFirmwareResponse>(text)
        println(response)
    }

    @Test
    fun testMergeFlow() {
        runBlocking {
            val flowOne = flow {
                delay(900)
                emit(1)
                delay(200)
                emit(2)
                delay(900)
                emit(3)
            }.flowOn(Dispatchers.IO)
            val flowTwo = flow {
                delay(900)
                emit(4)
                emit(5)
                emit(6)
            }.flowOn(Dispatchers.IO)
            val flowThree = flow {
                emit(7)
                emit(8)
                emit(9)
            }.flowOn(Dispatchers.IO)

            val mutiple = flowOf(flowOne, flowTwo, flowThree)

            launch(Dispatchers.Default) {
                mutiple.flattenConcat().collect {
                    println(Thread.currentThread().name)
                    println(it)
                }
            }
        }
    }

    @Test
    fun byte() {
        val str = "0,127,255"
        str.toByteArray(Charsets.US_ASCII).forEach {
            print(it.toString(16))
        }
    }

    @Test
    fun testGradient() {
        val gradient = Gradient(hslColors = listOf(
            HslColor(0, 255, 0, 0),
            HslColor(127, 0, 0, 255),
            HslColor(255, 255, 255, 0)
        ))

        println(255.toUByte().toString(16))
        gradient.toBytes().forEach {
            print("${it.toString(16)}-")
        }
    }

    @Test
    fun query() {
        val key = "abc"
        val keys = key.split(" ")
        keys.forEach { println(it) }
    }

    @Test
    fun sublist() {
        val list = listOf(0,1,2,3,4,5,6,7,8,9)
        val playingPos = 4

        val point = (playingPos + 1) % list.size
        if (point == 0) {
            println(list)
        } else {
            val first = list.subList(point, list.size)
            val second = list.subList(0, point)
            println(first.plus(second))
        }
    }

    @Test
    fun testQuery() {
        val key = "dee"

        val final = buildFormula(key)

        println(final)
    }
}