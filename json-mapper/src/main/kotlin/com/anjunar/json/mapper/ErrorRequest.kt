package com.anjunar.json.mapper

import jakarta.json.bind.annotation.JsonbProperty

class ErrorRequest(@JsonbProperty val path: List<Any>, @JsonbProperty val message: String)