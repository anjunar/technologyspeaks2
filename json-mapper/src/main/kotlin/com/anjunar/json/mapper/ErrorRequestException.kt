package com.anjunar.json.mapper

class ErrorRequestException(val errors : List<ErrorRequest>) : RuntimeException()