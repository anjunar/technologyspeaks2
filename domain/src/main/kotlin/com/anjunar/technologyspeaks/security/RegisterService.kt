package com.anjunar.technologyspeaks.security

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class RegisterService(val mailSender: JavaMailSender, val templateEngine: TemplateEngine) {

    fun register(to: String, code: String, nickName: String) {
        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

        val context = Context()
        context.setVariable("code", code)
        context.setVariable("nickName", nickName)

        val renderedTemplate = templateEngine.process("RegisterTemplate.html", context)

        helper.setTo(to)
        helper.setFrom("anjunar@gmx.de")
        helper.setSubject("Registrierung bei technologyspeaks.com")
        helper.setText(renderedTemplate, true)

        mailSender.send(mimeMessage)
    }

}