package com.github.divideby0.spotfire.web

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.github.divideby0.spotfire.utils.KmsService
import com.serverless.ApiGatewayResponse
import com.wrapper.spotify.SpotifyApi
import java.net.URI

class SpotifyLoginCallbackHandler : RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {
    val clientId = KmsService.getDecryptedKmsEnvvar("SPOTIFY_CLIENT_ID_ENC")
    val clientSecret = KmsService.getDecryptedKmsEnvvar("SPOTIFY_CLIENT_SECRET_ENC")
    val redirectUri = System.getenv("SPOTIFY_REDIRECT_URI")

    override fun handleRequest(event: APIGatewayProxyRequestEvent, context: Context?): ApiGatewayResponse {
        val code = event.queryStringParameters["code"]

        val api = SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRedirectUri(URI(redirectUri))
            .build()

        val creds = api.authorizationCode(code).build().execute()

        val currentUser = SpotifyApi.Builder()
            .setAccessToken(creds.accessToken)
            .setRefreshToken(creds.refreshToken)
            .build()
            .currentUsersProfile
            .build()
            .execute()

        return ApiGatewayResponse.build {
            statusCode = 200
            rawBody = mapOf(
                "accessToken" to creds.accessToken,
                "refreshToken" to creds.refreshToken,
                "expires" to creds.expiresIn,
                "scope" to creds.scope
            ).toString()
        }
    }
}