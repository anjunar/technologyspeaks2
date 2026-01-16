package com.anjunar.technologyspeaks

import com.anjunar.technologyspeaks.rest.types.LinksContainer
import com.anjunar.technologyspeaks.core.User
import jakarta.json.bind.annotation.JsonbProperty

class Application(@JsonbProperty val user : User) : LinksContainer.Interface by LinksContainer.Trait()