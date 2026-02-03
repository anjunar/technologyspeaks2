package app.pages.core

import app.domain.core.Data
import app.domain.core.User
import jFx2.client.JsonClient
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.template
import jFx2.forms.NotBlankValidator
import jFx2.forms.form
import jFx2.forms.imageCropper
import jFx2.forms.input
import jFx2.forms.inputContainer
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

        val model = Property(Data(User()))

        fun model(data : Data<User>) {
            model.set(data)
        }

        context(scope: NodeScope)
        fun afterBuild() {
            template {
                form {

                    onSubmit {

                        JobRegistry.instance.launch("Save User") {
                            JsonClient.put("/service/core/users/user", model.get().data)
                        }

                    }

                    imageCropper("image") {
                        aspectRatio = 1.0
                        outputType = "image/jpeg"
                        outputQuality = 0.92
                        outputMaxWidth = 512
                        outputMaxHeight = 512

                        validatorsProperty.add(NotBlankValidator())

                        subscribeBidirectional(model.get().data.image, valueProperty)
                    }

                    inputContainer("Nick Name") {
                        input("nickName") {
                            subscribeBidirectional(model.get().data.nickName, valueProperty)
                        }
                    }

                    inputContainer("First Name") {
                        input("firstName") {
                            subscribeBidirectional(model.get().data.info.firstName, valueProperty)
                        }
                    }

                    inputContainer("Last Name") {
                        input("lastName") {
                            subscribeBidirectional(model.get().data.info.lastName, valueProperty)
                        }
                    }

                    inputContainer("Birthdate") {
                        input("birthdate", "date") {
                            subscribeBidirectional(model.get().data.info.birthDate, valueProperty)
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

