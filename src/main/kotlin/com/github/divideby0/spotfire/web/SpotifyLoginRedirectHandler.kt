package com.github.divideby0.spotfire.web

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.github.divideby0.spotfire.utils.KmsService
import com.serverless.ApiGatewayResponse
import java.net.URLEncoder

class SpotifyLoginRedirectHandler : RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {
    val spotifyBaseUrl = "https://accounts.spotify.com/authorize"
    val clientId = KmsService.getDecryptedKmsEnvvar("SPOTIFY_CLIENT_ID_ENC")
    val redirectUri = System.getenv("SPOTIFY_REDIRECT_URI")

    val scopes = listOf(
        "playlist-modify-public",
        "playlist-modify-private",
        "playlist-read-collaborative",
        "playlist-read-private"
    ).joinToString(" ")

    override fun handleRequest(event: APIGatewayProxyRequestEvent, context: Context?): ApiGatewayResponse {
        val host = event.headers["Host"]

        val paramsStr = mapOf(
            "response_type" to "code",
            "client_id" to clientId,
            "scope" to scopes,
            "redirect_uri" to redirectUri
        ).entries.joinToString("&") { "${it.key}=${it.value}" }

        return ApiGatewayResponse.build {
            statusCode = 302
            headers = mapOf("Location" to "$spotifyBaseUrl?$paramsStr")
        }
    }

}