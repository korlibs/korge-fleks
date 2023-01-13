import com.soywiz.korge.gradle.*

plugins {
    id("com.soywiz.korge") version "3.4.0"
}

korge {
    id = "com.sample.fleks"

// To enable all targets at once

    //targetAll()

// To enable targets based on properties/environment variables
    //targetDefault()

// To selectively enable targets

    targetJvm()
    targetJs()
    targetDesktop()
    //targetDesktopCross()
    //targetIos()
    //targetAndroidIndirect() // targetAndroidDirect()
    serializationJson()
    //targetAndroidDirect()
}

dependencies {
    add("commonMainApi", project(":deps"))
}

