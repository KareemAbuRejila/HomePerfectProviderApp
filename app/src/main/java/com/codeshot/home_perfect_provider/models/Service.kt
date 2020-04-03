package com.codeshot.home_perfect_provider.models

class Service {
    var id:String?=""
    var name:String?=""
    var image:String?=""
    val providers:List<String>?=ArrayList()


    constructor()

    constructor(id: String?, name: String?, image: String?) {
        this.id = id
        this.name = name
        this.image = image
    }

    constructor(id: String?, name: String?) {
        this.id = id
        this.name = name
    }


    override fun toString(): String {
        return name!!
    }

}