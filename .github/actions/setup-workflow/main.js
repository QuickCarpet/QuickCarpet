import core from '@actions/core'
import fs from 'fs/promises'
import toml from '@iarna/toml'

async function getGradleVersion() {
    const gradleWrapperProperties = await fs.readFile('gradle/wrapper/gradle-wrapper.properties', 'utf8')
    const match = gradleWrapperProperties.match(/^distributionUrl=.*-(.*)-bin\.zip$/m)
    if (!match) throw new Error('Failed to find gradle version')
    return match[1]
}

;(async () => {
    const gradleVersion = await getGradleVersion()
    const libs = toml.parse(await fs.readFile('libs.versions.toml', 'utf8'))
    const javaVersion = libs.versions.java
    const lithiumVersion = libs.versions.lithium
    core.setOutput('matrix-default', JSON.stringify({
        gradle: [gradleVersion],
        java: [javaVersion],
    }))
    core.setOutput('matrix-mods', JSON.stringify({
        gradle: [gradleVersion],
        java: [javaVersion],
        lithium: [lithiumVersion],
    }))
})().catch(error => {
    core.setFailed(error.message)
})