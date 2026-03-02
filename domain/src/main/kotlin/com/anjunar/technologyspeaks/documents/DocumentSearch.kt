package com.anjunar.technologyspeaks.documents

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch
import com.anjunar.technologyspeaks.hibernate.search.Context
import com.anjunar.technologyspeaks.hibernate.search.PredicateProvider
import com.anjunar.technologyspeaks.hibernate.search.annotations.RestPredicate
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestParam

class DocumentSearch(
    @JsonbProperty
    @RequestParam
    @RestPredicate(DocumentPredicate::class)
    val name : String? = null,
    sort: MutableList<String> = mutableListOf(),
    index: Int = 0,
    limit: Int = 5
) : AbstractSearch(sort, index, limit) {

    companion object {
        @Component
        class DocumentPredicate : PredicateProvider<String, Document> {
            override fun build(context: Context<String, Document>) {
                val (rawValue, _, builder, predicates, root, _, selection, name, parameters) = context
                val value = rawValue.trim()
                if (value.isEmpty()) return

                val parameterName = "${name}_query"
                val parameter = builder.parameter(String::class.java, parameterName)
                parameters[parameterName] = value

                val similarity = builder.function(
                    "similarity",
                    Double::class.java,
                    builder.lower(root.get("title")),
                    builder.lower(parameter)
                )

                selection.add(similarity)
                predicates.add(builder.greaterThanOrEqualTo(similarity, builder.literal(0.1)))

            }
        }
    }

}
