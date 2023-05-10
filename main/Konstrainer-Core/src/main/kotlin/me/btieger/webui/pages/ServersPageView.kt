package me.btieger.webui.pages

import kotlinx.html.*
import me.btieger.persistance.tables.ServerStatus

fun DIV.serversView(model: ServersPageModel) {
    div {
        div {
            form(action = SERVERS_PATH, method = FormMethod.post, encType=FormEncType.multipartFormData) {
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
        div("mt-2") {
            div("row") {
                for (item in model.dsls) {
                    div("card") {
                        div("card-header") {
                            +item.name
                        }
                        div("card-body") {
                            +"Build status: ${item.buildStatus}"
                            br()
                            +"Server status: ${item.serverStatus}"
                        }
                        div("btn-group") {
                            role = "group"
                            button (classes="btn btn-secondary") {
                                onClick = "window.location='$SERVERS_PATH/${item.id}'"
                                type=ButtonType.submit
                                +"View Details"
                            }
                            button (classes="btn btn-danger") {
                                onClick = """
                                    $.ajax({  
                                        url: '/api/v1/dsls/${item.id}',
                                        type: 'DELETE',
                                        success: function(result) {
                                            window.location='$SERVERS_PATH'
                                        }
                                    });
                                """.trimIndent()
                                type=ButtonType.button
                                +"Delete"
                            }
                            if (item.serverStatus == ServerStatus.Up.name) {
                                button (classes="btn btn-primary") {
                                    onClick = """
                                        $.ajax({  
                                            url: '/api/v1/servers/${item.id}/stop',
                                            type: 'POST',
                                            success: function(result) {
                                                window.location='$SERVERS_PATH'
                                            }
                                        });
                                    """.trimIndent()
                                    type=ButtonType.button
                                    +"Stop Server"
                                }
                            }
                            if (item.serverStatus == ServerStatus.Down.name || item.serverStatus == ServerStatus.Error.name) {
                                button (classes="btn btn-primary") {
                                    onClick = """
                                        $.ajax({  
                                            url: '/api/v1/servers/${item.id}/start',
                                            type: 'POST',
                                            success: function(result) {
                                                window.location='$SERVERS_PATH'
                                            }
                                        });
                                    """.trimIndent()
                                    type=ButtonType.button
                                    +"Start Server"
                                }
                            }
                        }
                    }
                }
            }
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