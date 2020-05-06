package com.codeshot.home_perfect_provider.models

class Notification {
    var id: String? = null
    var customerId: String? = null
    var providerId: String? = null
    var requestId: String? = null
    var notiType: String? = null
    var time: String? = null

    var msgContent: MutableMap<String, String> = HashMap()


    constructor()
    constructor(customerId: String?, providerId: String?, requestId: String?, notiType: String?) {
        this.customerId = customerId
        this.providerId = providerId
        this.requestId = requestId
        this.notiType = notiType
    }

}