package jFx2.forms

import jFx2.controls.ArrayForm

sealed interface FormContext
class NamedFormContext(val form: Formular) : FormContext
class ArrayFormContext(val form: ArrayForm) : FormContext
class RootContext() : FormContext