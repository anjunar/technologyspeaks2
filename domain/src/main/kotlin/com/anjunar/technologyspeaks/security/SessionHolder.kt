package com.anjunar.technologyspeaks.security

import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.SessionScope
import java.util.*

@Service
@SessionScope
class SessionHolder {

    var user: UUID? = null

    var credentials: UUID? = null

}