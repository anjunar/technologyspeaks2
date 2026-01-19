package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.hibernate.EntityContext
import jakarta.persistence.Entity

@Entity
class PasswordCredential(var password: String) : Credential(), EntityContext<PasswordCredential>