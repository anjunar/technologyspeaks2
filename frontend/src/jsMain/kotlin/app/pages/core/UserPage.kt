package app.pages.core

import app.domain.core.Address
import app.domain.core.Data
import app.domain.core.User
import app.domain.core.UserInfo
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.codegen.JfxComponentBuilder
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.onClick
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.rendering.observeRender
import jFx2.core.template
import jFx2.forms.NotBlankValidator
import jFx2.forms.form
import jFx2.forms.imageCropper
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.forms.subForm
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.layout.vbox
import jFx2.router.PageInfo
import jFx2.router.renderByRel
import jFx2.state.JobRegistry
import jFx2.state.Property
import org.w3c.dom.HTMLDivElement

@JfxComponentBuilder(classes = ["user-page"])
class UserPage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {
    override val name: String = "User"
    override val width: Int = -1
    override val height: Int = -1
    override val resizable: Boolean = false
    override var close: () -> Unit = {}

    val model = Property(Data(User()))
    val infoDisabled = Property(true)
    val addressDisabled = Property(true)

    fun model(data : Data<User>) {
        model.set(data)
        if (data.data.info != null) infoDisabled.set(false)
        if (data.data.address != null) addressDisabled.set(false)
    }

    context(scope: NodeScope)
    fun afterBuild() {
        template {
            form(model = model.get().data, clazz = User::class) {

                disabled = model.links.find { it.rel == "update" } == null

                onSubmit {

                    this@form.model.update()

                }

                vbox {
                    hbox {

                        imageCropper("image") {

                            style {
                                width = "512px"
                                height = "512px"
                            }

                            aspectRatio = 1.0
                            outputType = "image/jpeg"
                            outputQuality = 0.92
                            outputMaxWidth = 512
                            outputMaxHeight = 512

                            validatorsProperty.add(NotBlankValidator())

                            subscribeBidirectional(this@form.model.image, valueProperty)
                        }

                        div {

                            style {
                                width = "300px"
                            }

                            inputContainer("Nick Name") {
                                input("nickName") {
                                    subscribeBidirectional(this@form.model.nickName, valueProperty)
                                }
                            }

                            hbox {

                                style {
                                    alignItems = "flex-start"
                                }

                                observeRender(infoDisabled) { value ->
                                    subForm("info", model = this@form.model.info, clazz = UserInfo::class) {

                                        style {
                                            flex = "1"
                                        }

                                        disabled = value

                                        inputContainer("Vorname") {
                                            input("firstName") {
                                                subscribeBidirectional(this@subForm.model.firstName, valueProperty)
                                            }
                                        }

                                        inputContainer("Nachame") {
                                            input("lastName") {
                                                subscribeBidirectional(this@subForm.model.lastName, valueProperty)
                                            }
                                        }

                                        inputContainer("Geburtsdatum") {
                                            input("birthdate", "date") {
                                                subscribeBidirectional(this@subForm.model.birthDate, valueProperty)
                                            }
                                        }
                                    }
                                }

                                renderByRel("update", this@UserPage.model.get().data.links) {
                                    button {
                                        name("close")
                                        type("button")
                                        className { "material-icons" }
                                        onClick {
                                            if (infoDisabled.get()) {
                                                this@form.model.info = this@form.subForms["info"]!!.model as UserInfo
                                                infoDisabled.set(false)
                                            } else {
                                                this@form.model.info = null
                                                infoDisabled.set(true)
                                            }
                                        }
                                    }
                                }

                            }

                            hbox {
                                style {
                                    alignItems = "flex-start"
                                }

                                observeRender(addressDisabled) { value ->
                                    subForm("address", model = this@form.model.address, clazz = Address::class) {

                                        style {
                                            flex = "1"
                                        }

                                        disabled = value

                                        inputContainer("Strasse") {
                                            input("street") {
                                                subscribeBidirectional(this@subForm.model.street, valueProperty)
                                            }
                                        }
                                        inputContainer("Hausnummer") {
                                            input("number") {
                                                subscribeBidirectional(this@subForm.model.number, valueProperty)
                                            }
                                        }
                                        inputContainer("Postleitzahl") {
                                            input("zipCode") {
                                                subscribeBidirectional(this@subForm.model.zipCode, valueProperty)
                                            }
                                        }
                                        inputContainer("Land") {
                                            input("country") {
                                                subscribeBidirectional(this@subForm.model.country, valueProperty)
                                            }
                                        }
                                    }
                                }

                                renderByRel("update", this@UserPage.model.get().data.links) {
                                    button {
                                        name("close")
                                        type("button")
                                        className { "material-icons" }
                                        onClick {
                                            if (addressDisabled.get()) {
                                                this@form.model.address = this@form.subForms["address"]!!.model as Address
                                                addressDisabled.set(false)
                                            } else {
                                                this@form.model.address = null
                                                addressDisabled.set(true)
                                            }
                                        }
                                    }
                                }
                            }

                        }

                    }

                    renderByRel("update", model.links) {
                        hbox {

                            style {
                                justifyContent = "flex-end"
                            }

                            button {
                                name("Speichern")
                            }

                        }
                    }

                }
            }
        }
    }
}