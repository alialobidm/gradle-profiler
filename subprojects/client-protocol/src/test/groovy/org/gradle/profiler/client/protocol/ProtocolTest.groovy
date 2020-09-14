package org.gradle.profiler.client.protocol

import spock.lang.Specification

import java.time.Duration

class ProtocolTest extends Specification {
    def "can send events between client and server"() {
        when:
        def server = new Server("some client")
        def timeout = Duration.ofSeconds(20)
        def client = Client.INSTANCE
        client.connect(server.port)
        def serverConnection = server.waitForIncoming(timeout)

        client.send(new SyncStarted(1))
        def m1 = serverConnection.receiveSyncStarted(timeout)

        serverConnection.send(new SyncParameters(["gradle-arg"], ["jvm-arg"]))
        def m2 = client.receiveParameters(timeout)

        client.send(new SyncCompleted(1, 123))
        def m3 = serverConnection.receiveSyncCompeted(timeout)

        then:
        m1.id == 1
        m2.gradleArgs == ["gradle-arg"]
        m2.jvmArgs == ["jvm-arg"]
        m3.id == 1
        m3.durationMillis == 123

        cleanup:
        client?.disconnect()
        server?.close()
    }
}
