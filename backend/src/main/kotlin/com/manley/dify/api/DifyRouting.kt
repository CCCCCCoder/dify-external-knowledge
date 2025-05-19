package com.manley.dify.api

import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.DifyRouting() {
    route("/dify"){
        get("/") {
            call.respondText("Hello from Dify API!")
        }
    }

}