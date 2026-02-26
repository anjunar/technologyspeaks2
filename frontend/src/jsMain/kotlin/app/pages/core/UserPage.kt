package app.pages.core

import app.domain.core.Address
import app.domain.core.Data
import app.domain.core.User
import app.domain.core.UserInfo
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.onClick
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.rendering.condition
import jFx2.core.rendering.observeRender
import jFx2.core.template
import jFx2.forms.Form
import jFx2.forms.NotBlankValidator
import jFx2.forms.form
import jFx2.forms.imageCropper
import jFx2.forms.input
import jFx2.forms.inputContainer
import jFx2.forms.subForm
import jFx2.layout.hbox
import jFx2.router.PageInfo
import jFx2.state.JobRegistry
import jFx2.state.Property
import org.w3c.dom.HTMLDivElement

object UserPage {

    class Page(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {
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

                    onSubmit {

                        JobRegistry.instance.launch("Save User") {
                            JsonClient.put("/service/core/users/user", this@form.model)
                        }

                    }

                    imageCropper("image") {
                        aspectRatio = 1.0
                        outputType = "image/jpeg"
                        outputQuality = 0.92
                        outputMaxWidth = 512
                        outputMaxHeight = 512

                        validatorsProperty.add(NotBlankValidator())

                        subscribeBidirectional(this@form.model.image, valueProperty)
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

                                inputContainer("First Name") {
                                    input("firstName") {
                                        subscribeBidirectional(this@subForm.model.firstName, valueProperty)
                                    }
                                }

                                inputContainer("Last Name") {
                                    input("lastName") {
                                        subscribeBidirectional(this@subForm.model.lastName, valueProperty)
                                    }
                                }

                                inputContainer("Birthdate") {
                                    input("birthdate", "date") {
                                        subscribeBidirectional(this@subForm.model.birthDate, valueProperty)
                                    }
                                }
                            }
                        }

                        button("close") {
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

                                inputContainer("Street") {
                                    input("street") {
                                        subscribeBidirectional(this@subForm.model.street, valueProperty)
                                    }
                                }
                                inputContainer("Number") {
                                    input("number") {
                                        subscribeBidirectional(this@subForm.model.number, valueProperty)
                                    }
                                }
                                inputContainer("Zip Code") {
                                    input("zipCode") {
                                        subscribeBidirectional(this@subForm.model.zipCode, valueProperty)
                                    }
                                }
                                inputContainer("Country") {
                                    input("country") {
                                        subscribeBidirectional(this@subForm.model.country, valueProperty)
                                    }
                                }
                            }
                        }

                        button("close") {
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


                    hbox {

                        style {
                            justifyContent = "flex-end"
                        }

                        button("Save") {

                        }

                    }


                }
            }
        }
    }

    context(scope: NodeScope)
    fun page(block: context(NodeScope) Page.() -> Unit = {}): Page {
        val el = scope.create<HTMLDivElement>("div")
        el.classList.add("user-page")
        val c = Page(el)
        scope.attach(c)

        val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

        block(childScope, c)

        with(childScope) {
            scope.ui.build.afterBuild { c.afterBuild() }
        }

        return c
    }

}

