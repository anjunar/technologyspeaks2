package com.anjunar.technologyspeaks.rest.types

class DTOList<T>(items: Collection<T> = emptyList()) : ArrayList<T>(items), DTO

