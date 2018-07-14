package com.github.divideby0.spotfire.domain

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import org.apache.commons.codec.digest.DigestUtils

@DynamoDBTable(tableName = "SpotifyUser")
data class SpotifyUser(
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "ById", attributeName = "UserId")
    val id: String,

    @DynamoDBAttribute(attributeName = "RefreshToken")
    val refreshToken: String,

    @DynamoDBHashKey(attributeName = "RefreshTokenHash")
    val hash: String = DigestUtils.sha256Hex("$id$refreshToken"),

    @DynamoDBAttribute(attributeName = "DisplayName")
    val displayName: String
)