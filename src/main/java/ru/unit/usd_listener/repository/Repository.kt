package ru.unit.usd_listener.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Repository {
    private val client = HttpClient(Android) {
        install(HttpTimeout) {
            connectTimeoutMillis = 30000
            requestTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }
    }

    private val urlGetListUsd = "https://www.cbr.ru/scripts/XML_dynamic.asp?date_req1=%s&date_req2=%s&VAL_NM_RQ=R01235"
    private val dateFormatterUrl = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateFormatterFrom = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val dateFormatterTo = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    suspend fun getValuesWithin30Days(): List<DayUsdValue> {
        val currentDate = LocalDateTime.now().format(dateFormatterUrl)
        val date30DaysBefore = LocalDateTime.now().minusDays(30).format(dateFormatterUrl)
        val response: HttpResponse = client.get(urlGetListUsd.format(date30DaysBefore, currentDate))
        val xml: String = response.receive()
        return parseXmlDayUsd(xml)
    }

    private fun parseXmlDayUsd(xml: String): List<DayUsdValue> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()

        parser.setInput(xml.reader())

        val result = mutableListOf<DayUsdValue>()
        var event = parser.eventType
        var lastTagName = ""
        var date = ""
        var value = ""
        while (event != XmlPullParser.END_DOCUMENT) {
            when(event) {
                XmlPullParser.START_TAG -> {
                    lastTagName = parser.name
                    if(lastTagName == "Record") {
                        date = parser.getAttributeValue(0) // date in Record
                    }
                }
                XmlPullParser.TEXT ->
                    if(lastTagName == "Value") {
                        value = parser.text
                    }
                XmlPullParser.END_TAG ->
                    if(parser.name == "Record") {
                        result.add(DayUsdValue(dateFormatterTo.format(dateFormatterFrom.parse(date)), date, value.replace(',', '.').toFloat()))
                    }
            }

            event = parser.next()
        }

        return result
    }

    data class DayUsdValue(val date: String, val simpleDate: String, val value: Float)

}