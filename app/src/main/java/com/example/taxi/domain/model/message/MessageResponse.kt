package com.example.taxi.domain.model.message

data class MessageResponse(

    val data: List<MessageItem>
)
data class MessageItem(
    var message: String,
    val time: String
)
