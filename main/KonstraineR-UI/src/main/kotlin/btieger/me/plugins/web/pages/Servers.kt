package btieger.me.plugins.web.pages

import kotlinx.css.div
import kotlinx.html.*

fun MAIN.servers() {
    div("row") {
        div("col-sm-4") {
            form(action = "/api/v1/dsl-upload", method = FormMethod.post) {
                div("input-group") {
                    div("custom-file") {
                        input (classes="custom-file-input", type=InputType.file, name="filename") {
                            required=true
                            id = "fileInput"
                        }
                        label("custom-file-label") {
                            htmlFor = "fileInput"
                            text("Choose file...")
                        }
                    }
                    div("input-group-append") {
                        button (classes = "btn btn-primary", type=ButtonType.submit) {text("Submit")}
                    }
                }
            }
        }
        div("col-sm-8") {
            +"x"
        }
    }
    script {
        unsafe {
            +"""
                document.querySelector('.custom-file-input').addEventListener('change',function(e){
                  var fileName = document.getElementById("fileInput").files[0].name;
                  var nextSibling = e.target.nextElementSibling
                  nextSibling.innerText = fileName
                })
            """.trimIndent()
        }
    }
}