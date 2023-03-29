import core from '@actions/core'
import fetch from 'node-fetch'
import fs from 'fs/promises'
import path from 'path'
import toml from '@iarna/toml'

const MODS_DIR = 'run/mods'
let GAME_VERSION = core.getInput('game-version')
const MODS = core.getMultilineInput('mods').map(mod => mod.split('=')).map(([id, version]) => ({ id, version }))
const LOADER = core.getInput('loader') || 'fabric'

async function findVersion(id, version) {
    const params = new URLSearchParams({
        game_versions: `["${GAME_VERSION}"]`,
        loaders: `["${LOADER}"]`,
    })
    const response = await fetch(`https://api.modrinth.com/v2/project/${id}/version?${params}`)
    if (!response.ok) throw new Error(`Failed to find mod ${id}`)
    let mods = await response.json()
    if (version !== undefined) {
        mods = mods.filter(mod => mod.version_number === version)
    }
    if (mods.length === 0) throw new Error(`Failed to find a valid version of mod ${id}`)
    return {modid: id, ...mods[0]}
}

;(async () => {
    if (!GAME_VERSION) {
        const libs = toml.parse(await fs.readFile('libs.versions.toml', 'utf8'))
        GAME_VERSION = libs.versions.minecraft
    }
    core.startGroup('Setting up mods/ directory')
    const createdDir = await fs.mkdir(MODS_DIR, { recursive: true })
    if (createdDir) {
        core.info(`Created ${MODS_DIR}`)
    } else {
        core.info('Directory exists')
    }
    if (core.getBooleanInput('replace')) {
        for (const file of await fs.readdir(MODS_DIR)) {
            if (!file.endsWith('.jar')) continue
            core.info(`Deleting ${file}`)
            await fs.unlink(path.join(MODS_DIR, file))
        }
    }
    core.endGroup()
    core.startGroup('Fetching mod versions')
    const versions = await Promise.all(MODS.map(({ id, version }) => findVersion(id, version)))
    for (const version of versions) {
        core.info(`Using ${version.modid} ${version.version_number}`)
    }
    const files = versions.flatMap(version => version.files.filter(file => file.primary))
    core.endGroup()
    core.startGroup('Downloading mods')
    for (const file of files) {
        const response = await fetch(file.url)
        if (!response.ok) throw new Error(`Failed to download ${file.url}`)
        core.info(`Downloading ${file.filename}`)
        await fs.writeFile(path.join(MODS_DIR, file.filename), response.body)
    }
    core.endGroup()
})().catch(error => {
    core.setFailed(error)
})