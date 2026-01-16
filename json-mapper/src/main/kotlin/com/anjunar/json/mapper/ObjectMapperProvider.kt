package com.anjunar.json.mapper

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

object ObjectMapperProvider {

    val mapper : JsonMapper =
        JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .changeDefaultPropertyInclusion {
                JsonInclude.Value.construct(
                    JsonInclude.Include.NON_EMPTY,
                    JsonInclude.Include.NON_EMPTY
                )
            }
            .build()


}