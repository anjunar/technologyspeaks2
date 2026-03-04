package jFx2.forms

enum class Status { valid, invalid, dirty, empty, focus }

interface Validator {
    fun validate(value: String): Boolean
    fun message(): String
}

class SizeValidator(val min: Int, val max: Int) : Validator {
    override fun validate(value: String): Boolean = value.length in min..max
    override fun message(): String = "must be between $min and $max characters"
}

class PatternValidator(val pattern: String) : Validator {
    override fun validate(value: String): Boolean = value.matches(pattern.toRegex())
    override fun message(): String = "must match pattern '$pattern'"
}

class NotBlankValidator : Validator {
    override fun validate(value: String): Boolean = value.isNotBlank()
    override fun message(): String = "must not be blank"
}

class EmailValidator : Validator {
    override fun validate(value: String): Boolean = value.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
    override fun message(): String = "must be a valid email address"
}
