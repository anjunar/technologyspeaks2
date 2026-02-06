package com.anjunar.technologyspeaks.shared.editor

data class Node(val type : String,val content : List<Node>?,val attrs : Map<String, Any?>?,val text : String?,val marks : List<Node>?)