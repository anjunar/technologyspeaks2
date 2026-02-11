package com.anjunar.technologyspeaks.shared.editor

import com.anjunar.json.mapper.provider.DTO
import jakarta.json.bind.annotation.JsonbProperty

class Node : DTO {
    @JsonbProperty
    var type : String? = null
    @JsonbProperty
    var content : MutableList<Node> = mutableListOf()
    @JsonbProperty
    var attrs : MutableMap<String, Any?> = mutableMapOf()
    @JsonbProperty
    var text : String? = null
    @JsonbProperty
    var marks : MutableList<Node> = mutableListOf()
}