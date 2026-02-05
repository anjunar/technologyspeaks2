package com.anjunar.technologyspeaks

import com.anjunar.technologyspeaks.rest.EntityConverter
import com.anjunar.technologyspeaks.rest.JsonHttpMessageConverter
import com.anjunar.technologyspeaks.rest.MapperHttpMessageConverter
import com.anjunar.technologyspeaks.security.SecurityInterceptor
import jakarta.persistence.EntityManagerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.format.FormatterRegistry
import org.springframework.http.converter.HttpMessageConverters
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebConfig(val securityInterceptor: SecurityInterceptor, val entityConverter: EntityConverter) : WebMvcConfigurer {

    override fun configureMessageConverters(builder: HttpMessageConverters.ServerBuilder) {
        builder.addCustomConverter(MapperHttpMessageConverter())
        builder.addCustomConverter(JsonHttpMessageConverter())
        builder.build()
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(entityConverter)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(securityInterceptor)
            .addPathPatterns("/**")
            .order(Ordered.HIGHEST_PRECEDENCE + 10)
    }

    @Bean
    fun openEntityManagerInViewFilter(): FilterRegistrationBean<OpenEntityManagerInViewFilter> {
        val filter = OpenEntityManagerInViewFilter()

        filter.setEntityManagerFactoryBeanName("entityManagerFactory")

        val registration = FilterRegistrationBean(filter)
        registration.order = 0
        registration.addUrlPatterns("/service/*")
        return registration
    }

}
