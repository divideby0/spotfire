package com.github.divideby0.spotfire.spotify

import com.google.common.util.concurrent.RateLimiter
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.wrapper.spotify.HttpDeleteBody
import com.wrapper.spotify.IHttpManager
import com.wrapper.spotify.exceptions.detailed.*
import org.apache.commons.lang3.RandomUtils
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus.*
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.logging.Logger

// Pooling and rate-limited, but no caching
class SpotfireHttpManager(
    val totalConnections: Int = 200,
    val requestsPerSecond: Double = 10.0
) : IHttpManager {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val cm = PoolingHttpClientConnectionManager()
    private val httpClient: CloseableHttpClient
    private val rateLimiter = RateLimiter.create(requestsPerSecond)

    init {
        cm.maxTotal = totalConnections
        cm.defaultMaxPerRoute = totalConnections
        httpClient = HttpClients.custom().setConnectionManager(cm).build()
    }

    override fun get(uri: URI?, headers: Array<out Header>?): String? {
        assert(!uri?.toString().isNullOrEmpty())
        val req = HttpGet()
        req.uri = uri
        req.setHeaders(headers)
        return executeAndGetResponseBody(req)
    }

    override fun put(uri: URI?, headers: Array<out Header>?, entity: HttpEntity?): String? {
        assert(!uri?.toString().isNullOrEmpty())
        val req = HttpPut()
        req.uri = uri
        req.setHeaders(headers)
        req.entity = entity
        return executeAndGetResponseBody(req)
    }

    override fun post(uri: URI?, headers: Array<out Header>?, entity: HttpEntity?): String? {
        assert(!uri?.toString().isNullOrEmpty())
        val req = HttpPost()
        req.uri = uri
        req.setHeaders(headers)
        req.entity = entity
        return executeAndGetResponseBody(req)
    }

    override fun delete(uri: URI?, headers: Array<out Header>?, entity: HttpEntity?): String? {
        assert(!uri?.toString().isNullOrEmpty())
        val req = HttpDeleteBody()
        req.uri = uri
        req.setHeaders(headers)
        req.entity = entity
        return executeAndGetResponseBody(req)
    }

    private fun executeAndGetResponseBody(req: HttpRequestBase): String? {
        rateLimiter.acquire()
        val resp = httpClient.execute(req)
        val statusLine = resp.statusLine
        val responseBody = if(resp.entity != null) {
            EntityUtils.toString(resp.entity, "UTF-8")
        } else {
            null
        }
        var errorMessage = statusLine.reasonPhrase;
        if (responseBody.isNullOrEmpty()) {
            try {
                val jsonObject = JsonParser().parse(responseBody).asJsonObject

                if (jsonObject.has("error")) {
                    if (jsonObject.has("error_description")) {
                        errorMessage = jsonObject.get("error_description").asString;
                    } else if (jsonObject.get("error").isJsonObject && jsonObject.getAsJsonObject("error").has("message")) {
                        errorMessage = jsonObject.getAsJsonObject("error").get("message").asString;
                    }
                }
            } catch (e: JsonSyntaxException) {
                // Not necessary
            }
        }

        req.releaseConnection()

        val exception = when(statusLine.statusCode) {
            SC_BAD_REQUEST -> BadRequestException(errorMessage)
            SC_UNAUTHORIZED -> UnauthorizedException(errorMessage)
            SC_FORBIDDEN -> ForbiddenException(errorMessage)
            SC_NOT_FOUND -> NotFoundException(errorMessage)
            SC_INTERNAL_SERVER_ERROR -> InternalServerErrorException(errorMessage)
            SC_BAD_GATEWAY -> BadGatewayException(errorMessage)
            SC_SERVICE_UNAVAILABLE -> ServiceUnavailableException(errorMessage)
            429 -> {
                val headerValue = resp.getFirstHeader("Retry-After")?.value
                when(headerValue) {
                    null -> TooManyRequestsException(errorMessage)
                    else -> TooManyRequestsException(errorMessage, Integer.parseInt(headerValue))
                }
            }
            in (200..299) -> null
            else -> IllegalStateException("Unknown status code $statusLine - $errorMessage")
        }
        if(exception is TooManyRequestsException) {
            val sleepMs = (exception.retryAfter + 1) * 1000
            synchronized(rateLimiter) {
                rateLimiter.rate = rateLimiter.rate*0.9
                log.warn("Received too many requests exception on ${req.uri}, retrying after ${sleepMs}ms and setting rate to ${rateLimiter.rate}/sec")
            }
            Thread.sleep(sleepMs.toLong())
            return executeAndGetResponseBody(req)
        } else if(exception != null) {
            throw exception
        }
        return responseBody
    }

}