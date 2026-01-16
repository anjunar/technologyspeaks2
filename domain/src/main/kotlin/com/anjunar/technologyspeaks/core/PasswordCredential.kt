package com.anjunar.technologyspeaks.core

import jakarta.persistence.Entity

@Entity
class PasswordCredential(var password: String) : Credential()